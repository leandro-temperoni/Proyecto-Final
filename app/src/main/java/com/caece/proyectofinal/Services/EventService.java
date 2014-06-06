package com.caece.proyectofinal.Services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.caece.proyectofinal.Activities.ServiceRestartActivity;
import com.caece.proyectofinal.Utils.Battery;
import com.caece.proyectofinal.Utils.Device;
import com.caece.proyectofinal.Utils.MemoryStatus;
import com.caece.proyectofinal.Utils.MyLog;
import com.caece.proyectofinal.Utils.Notificacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EventService extends Service {

    private EventReceiver manualRegisterReceiver;
    private String lastActiveApp = "";
    private LocationMonitor locationMonitor;
    private List<ActivityManager.RunningTaskInfo> tasksAnteriores;
    private Handler handlerOneSecond;
    private Handler handlerLogWatcher;
    private Handler handlerTenMinutesChecker;
    private OneSecondChecker oneSecondChecker = new OneSecondChecker();
    private LogWatcher logWatcher = new LogWatcher();
    private TenMinutesChecker tenMinutesChecker = new TenMinutesChecker();

    private int ONE_SECOND_CHECK_INTERVAL = 1000;
    private int SPACE_CHECK_INTERVAL = 600000;

    Boolean aviso = false;

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
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);

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

        //packages
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");

        //power
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        SMSObserver smsObserver = new SMSObserver(new Handler(), this);
        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),true, smsObserver);

        locationMonitor = new LocationMonitor(this);
        locationMonitor.startListening();

        manualRegisterReceiver = new EventReceiver();
        registerReceiver(manualRegisterReceiver, intentFilter);

        HandlerThread thread = new HandlerThread("OneSecondCheckerThread");
        thread.start();
        handlerOneSecond = new Handler(thread.getLooper());
        handlerOneSecond.postDelayed(oneSecondChecker, ONE_SECOND_CHECK_INTERVAL);

        if(Build.VERSION.SDK_INT < 16) {        //SI ES MENOR A JELLY BEAN (4.1)

            HandlerThread thread3 = new HandlerThread("LogWatcher");
            thread3.start();
            handlerLogWatcher = new Handler(thread3.getLooper());
            handlerLogWatcher.postDelayed(logWatcher, 1000);

        }

        HandlerThread thread4 = new HandlerThread("TenMinutesCheckerThread");
        thread4.start();
        handlerTenMinutesChecker = new Handler(thread4.getLooper());
        handlerTenMinutesChecker.postDelayed(tenMinutesChecker, SPACE_CHECK_INTERVAL);

        super.onCreate();
    }

    private class LogWatcher implements Runnable
    {
        @Override
        public void run()
        {
            log();
        }
    }

    private class OneSecondChecker implements Runnable
    {
        @Override
        public void run()
        {
            Boolean nuevaApp = false;

            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(10);
            if(tasks != null && !tasks.isEmpty())
            {
                ActivityManager.RunningTaskInfo task = tasks.get(0);            //App en el frente
                if(task != null)
                {
                    ComponentName topActivity = task.topActivity;
                    if (topActivity != null)
                    {
                        String pn = topActivity.getPackageName();
                        if(pn != null && !lastActiveApp.equals(pn))
                        {
                            lastActiveApp = pn;
                            nuevaApp = true;
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

                            MyLog.write("FA:" + appName, "Mediciones", false);

                        }
                    }
                }
            }

            //Memoria saturada
            if(memoriaSaturada())
                MyLog.write("MS", "Mediciones", false);

            getCPUPerApp();

            //Si se abrio una nueva app o se cerro, se registra el grado de multiprogramacion y la memoria por app
            if(nuevaApp) {
                List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
                MyLog.write("MP:" + processes.size(), "Mediciones",false);
                getMemPerApp(processes);
            }

            //Ver quien mato a roger rabbit

            if(tasksAnteriores == null)
                tasksAnteriores = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(10);
            else {

                if(tasks.size() < tasksAnteriores.size()) {      //Si se mato alguna
                    //Log.i("pepe", "se mato alguna");

                    for (ActivityManager.RunningTaskInfo taskAnterior : tasksAnteriores) {

                        Boolean noEstaba = true;
                        String name = taskAnterior.topActivity.getPackageName();
                        for (ActivityManager.RunningTaskInfo task : tasks) {

                            if (taskAnterior.id == task.id)
                                noEstaba = false;

                        }

                        if (noEstaba) {
                            //Log.i("pepe", name);
                            if (lastActiveApp.equals("com.sec.android.app.controlpanel"))
                                MyLog.write("CBU:" + name, "Mediciones", false);
                            else if (memoriaSaturada())
                                MyLog.write("CBA:" + name, "Mediciones", false);
                        }
                    }

                }

                tasksAnteriores = tasks;

            }

            handlerOneSecond.postDelayed(oneSecondChecker, ONE_SECOND_CHECK_INTERVAL);
        }
    }

    private void getMemPerApp(List<ActivityManager.RunningAppProcessInfo> processes){

        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(10);
        ArrayList<String> pns = new ArrayList<String>();
        for(int i = 0; i < tasks.size(); i++) {
            String pn = tasks.get(i).topActivity.getPackageName();
            if(!pn.contains("launcher"))
                pns.add(pn);
        }

        int[] pids = new int[tasks.size()];
        int i = 0;
        for(ActivityManager.RunningAppProcessInfo info : processes){

            if(pns.contains(info.processName)) {
                pids[i] = info.pid;
                i++;
            }

        }

        Debug.MemoryInfo[] memoryInfos = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getProcessMemoryInfo(pids);
        String data = "M:";
        i = 0;
        for(Debug.MemoryInfo info : memoryInfos){

            if(info.getTotalPss() != 0)
                data += pns.get(i) + ":" + pids[i] + ":" + info.getTotalPss() + "-";
            i++;

        }

        if(!data.equals("M:")) {
            MyLog.write(data.substring(0, data.length() - 1), "Mediciones", true);
            //Log.i("pepe", data);
        }

    }

    private Boolean memoriaSaturada(){ return Device.getCurrentRAMState(this); }

    private class TenMinutesChecker implements Runnable {
        @Override
        public void run() {

            long sdAvailable = MemoryStatus.getAvailableExternalMemorySize();
            long sdTotal = MemoryStatus.getTotalExternalMemorySize();
            long sdLevel = 100 - (sdAvailable * 100) / sdTotal;

            long romAvailable = MemoryStatus.getAvailableInternalMemorySize();
            long romTotal = MemoryStatus.getTotalInternalMemorySize();
            long romLevel = 100 - (romAvailable * 100) / romTotal;

            MyLog.write("MI:" + String.valueOf(romLevel), "Mediciones", false);
            MyLog.write("ME:" + String.valueOf(sdLevel), "Mediciones", false);
            MyLog.write("BL:" + String.valueOf(nivelBateria()), "Mediciones", false);
            MyLog.write("DR:" + String.valueOf(TrafficStats.getTotalRxBytes()), "Mediciones", true);
            MyLog.write("DS:" + String.valueOf(TrafficStats.getTotalTxBytes()), "Mediciones", true);

            //Esto desp se borra, es para probar nomas
            if(MyLog.superolos5MB() && !aviso) {
                notificacion();
                aviso = true;
            }

            handlerTenMinutesChecker.postDelayed(tenMinutesChecker, SPACE_CHECK_INTERVAL);

        }

    }

    private void getCPUPerApp(){

        try {
            int pid = 0;
            int pn = 0;
            int cpu = 0;
            // -m 10, how many entries you want, -d 1, delay by how much, -n 1,
            // number of iterations
            Process p = Runtime.getRuntime().exec("top -m 50 -d 0 -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            String datos = "C:";
            while (line != null) {
                line = reader.readLine();
                if(line != null) {
                    String[] split = line.split(" ");
                    String s = "";

                    for (int i = 0; i < split.length; i++) {
                        if (!split[i].equals(""))
                            s += "-" + split[i];

                    }

                    if (!s.contains("PID")) {

                        //Log.i("cpu", s);
                        String[] split2 = s.split("-");
                        if (!split2[cpu].replace("%", "").equals("0") && !s.contains("top") && s.contains(".")) {
                            datos += split2[pn] + ":" + split2[pid] + ":" + split2[cpu] + "-";
                        }

                    } else {

                        String[] split2 = s.split("-");
                        for (int i = 0; i < split2.length; i++) {

                            if (split2[i].equals("PID"))
                                pid = i;
                            if (split2[i].equals("CPU%"))
                                cpu = i;
                            if (split2[i].equals("Name"))
                                pn = i;

                        }

                    }

                }

            }

            if(!datos.equals("C:")) {
                //Log.i("pepe", datos.substring(0, datos.length() - 1));
                MyLog.write(datos.substring(0, datos.length() - 1), "Mediciones", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int nivelBateria(){ return Battery.getLevel(this); }

    private void notificacion(){ Notificacion.mostrar(this, "Oh no", "El archivo supero los 5 MB!"); }

    private void log() {

        try {
            Runtime.getRuntime().exec("logcat -c").waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat ActivityManager:* *:S");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (true) {
            String nextLine = null;
            try {
                nextLine = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nextLine.contains("ActivityManager")) {
                //Log.i("pepe", nextLine.replace("ActivityManager", "AM"));
                if(nextLine.contains("Displayed"))
                    MyLog.write(nextLine.substring(25).replace("Displayed", "RT:").replace(" ", ""), "Mediciones", false);
            }

        }

    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        handlerOneSecond.removeCallbacks(oneSecondChecker);
        handlerLogWatcher.removeCallbacks(logWatcher);
        handlerTenMinutesChecker.removeCallbacks(tenMinutesChecker);
        locationMonitor.stopListening();
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

    /*float cpuUsage = Device.readCPUUsage() * 100.0f;

            long totalRAM = Device.getTotalMemory();

            long currentRAM = getRam();
            int RAMLevel = (int) ((currentRAM*100) / totalRAM);*/

    //private long getRam(){ return Device.getCurrentRAM(this); }

}
