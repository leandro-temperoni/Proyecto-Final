package com.caece.proyectofinal;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.caece.proyectofinal.Utils.Device;
import com.caece.proyectofinal.Utils.MemoryStatus;
import com.caece.proyectofinal.Utils.MyLog;

import java.util.List;

public class EventService extends Service {

    private EventReceiver manualRegisterReceiver;
    private String lastActiveApp = "";
    Handler handler;
    Handler handler2;
    private AppLaunchChecker appLaunchChecker = new AppLaunchChecker();
    private FeatureChecker featuresChecker = new FeatureChecker();

    private int APP_LAUNCH_CHECK_INTERVAL = 1000;
    private int FEATURES_CHECK_INTERVAL = 2000;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate(){

        IntentFilter intentFilter = new IntentFilter();
        //screen
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        //misc
        intentFilter.addAction(Intent.ACTION_REBOOT);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);

        //package
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);

        //configuration
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        //media
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);

        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);

        //power
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        manualRegisterReceiver = new EventReceiver();
        registerReceiver(manualRegisterReceiver, intentFilter);

        HandlerThread thread = new HandlerThread("AppLaunchCheckerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(appLaunchChecker, APP_LAUNCH_CHECK_INTERVAL);

        HandlerThread thread2 = new HandlerThread("FeatureCheckerThread");
        thread2.start();
        handler2 = new Handler(thread2.getLooper());
        handler2.postDelayed(featuresChecker, FEATURES_CHECK_INTERVAL);

        super.onCreate();
    }

    private class AppLaunchChecker implements Runnable
    {
        @Override
        public void run()
        {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
            if(tasks != null && !tasks.isEmpty())
            {
                ActivityManager.RunningTaskInfo task = tasks.get(0);
                if(task != null)
                {
                    ComponentName topActivity = task.topActivity;
                    if (topActivity != null)
                    {
                        String pn = topActivity.getPackageName();
                        if(pn != null && !lastActiveApp.equals(pn))
                        {
                            lastActiveApp = pn;
                            PackageManager pm = getPackageManager();
                            String appName;
                            try
                            {
                                ApplicationInfo ai = pm.getApplicationInfo(pn, 0);
                                appName = (String) pm.getApplicationLabel(ai);
                            }
                            catch (Exception e)
                            {
                                appName = "[" + "Unknowed app" + "]";
                            }

                            Event event = new Event("App en el frente: " + appName);
                            EventReceiver.sendToReceiver(event);

                        }
                    }
                }
            }
            handler.postDelayed(appLaunchChecker, APP_LAUNCH_CHECK_INTERVAL);
        }
    }

    private class FeatureChecker implements Runnable {
        @Override
        public void run() {

            float cpuUsage = Device.readCPUUsage() * 100.0f;

            long totalRAM = Device.getTotalMemory();

            long currentRAM = getRam();
            int RAMLevel = (int) ((currentRAM*100) / totalRAM);

            long sdAvailable = MemoryStatus.getAvailableExternalMemorySize();
            long sdTotal = MemoryStatus.getTotalExternalMemorySize();
            long sdLevel = 100 - (sdAvailable * 100) / sdTotal;

            long romAvailable = MemoryStatus.getAvailableInternalMemorySize();
            long romTotal = MemoryStatus.getTotalInternalMemorySize();
            long romLevel = 100 - (romAvailable * 100) / romTotal;

            MyLog.write("RAM: " + String.valueOf(100 - RAMLevel) + "% - " + (totalRAM - currentRAM) + "MB / " + totalRAM + "MB", "Mediciones");
            MyLog.write("CPU: " + String.valueOf((int) cpuUsage) + "%", "Mediciones");
            MyLog.write("Mem Int: " + String.valueOf(romLevel) + "% - " + MemoryStatus.formatSize(romTotal - romAvailable) + " / " + MemoryStatus.formatSize(romTotal), "Mediciones");
            MyLog.write("Mem Ext: " + String.valueOf(sdLevel) + "% - " + MemoryStatus.formatSize(sdTotal - sdAvailable) + " / " + MemoryStatus.formatSize(sdTotal), "Mediciones");

            handler2.postDelayed(featuresChecker, FEATURES_CHECK_INTERVAL);

        }

    }

    private long getRam(){ return Device.getCurrentRAM(this); }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        handler.removeCallbacks(appLaunchChecker);
        handler2.removeCallbacks(featuresChecker);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        startActivity(new Intent(this, ServiceRestartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
    }
}
