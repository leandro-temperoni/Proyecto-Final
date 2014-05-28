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
import android.util.Log;

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

import static android.os.Debug.getMemoryInfo;

public class EventService extends Service {

    private EventReceiver manualRegisterReceiver;
    private String lastActiveApp = "";
    private int intervalos;
    private Boolean estoyMidiendo;
    private List<ActivityManager.RunningTaskInfo> tasksAnteriores;
    Handler handler;
    Handler handler3;
    Handler handler4;
    Handler handler5;
    private OneSecondChecker oneSecondChecker = new OneSecondChecker();
    private LogWatcher logWatcher = new LogWatcher();
    private SpaceChecker spaceChecker = new SpaceChecker();
    private ResponseTimeChecker responseTimeChecker = new ResponseTimeChecker();

    private int ONE_SECOND_CHECK_INTERVAL = 1000;
    private int SPACE_CHECK_INTERVAL = 600000;
    private int RESPONSE_TIME_CHECK_INTERVAL = 200;

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

        intervalos = 0;
        estoyMidiendo = false;

        manualRegisterReceiver = new EventReceiver();
        registerReceiver(manualRegisterReceiver, intentFilter);

        HandlerThread thread = new HandlerThread("OneSecondCheckerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(oneSecondChecker, ONE_SECOND_CHECK_INTERVAL);

        /*if(Build.VERSION.SDK_INT < 16) {        //SI ES MENOR A JELLY BEAN (4.1)

            HandlerThread thread3 = new HandlerThread("LogWatcher");
            thread3.start();
            handler3 = new Handler(thread3.getLooper());
            handler3.postDelayed(logWatcher, 1000);

        }*/

        HandlerThread thread4 = new HandlerThread("SpaceCheckerThread");
        thread4.start();
        handler4 = new Handler(thread4.getLooper());
        handler4.postDelayed(spaceChecker, SPACE_CHECK_INTERVAL);

        HandlerThread thread5 = new HandlerThread("ResponseTimeCheckerThread");
        thread5.start();
        handler5 = new Handler(thread5.getLooper());
        handler5.postDelayed(responseTimeChecker, RESPONSE_TIME_CHECK_INTERVAL);

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
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
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

            if(memoriaSaturada())                                                       //Memoria saturada
                MyLog.write("MS", "Mediciones", false);

            List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            MyLog.write("MP:" + processes.size(), "Mediciones",false);

            getCPUPerApp();

            handler.postDelayed(oneSecondChecker, ONE_SECOND_CHECK_INTERVAL);
        }
    }

    private Boolean memoriaSaturada(){ return Device.getCurrentRAMState(this); }

    private class ResponseTimeChecker implements Runnable {
        @Override
        public void run() {

            if(tasksAnteriores == null){
                tasksAnteriores = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(10); Log.i("pepe", "cargo");}
            else{

                List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(10);

                if(tasksAnteriores.get(0).id != tasks.get(0).id || estoyMidiendo) {      //CAMBIO EL TOPE O ESTOY MIDIENDO

                    if (tasksAnteriores.size() == 1 || (tasksAnteriores.get(1).id != tasks.get(0).id && !estoyMidiendo)) //SI NO ESTABA 2DA, OSEA QUE ES NUEVA
                        if (tasks.get(0).numRunning == 0 || tasks.get(0).baseActivity == null) {     //SI NO TIENE ACTIVIDADES SUMO UN INTERVALO
                            intervalos++;
                            estoyMidiendo = true;
                            Log.i("pepe", "intervalos en " + intervalos);
                        } else Log.i("pepe", "tiene actividad");

                    else {                                       //SIGO MIDIENDO INTERVALOS

                        if (tasks.get(0).numRunning == 0) { //SI NO TIENE ACTIVIDADES SUMO UN INTERVALO
                            intervalos++;
                            Log.i("pepe", "intervalos en " + intervalos);
                        } else {
                            if (intervalos > 0) {            //SI TIENE INTERVALOS, RESETEO LA VARIABLE Y ESCRIBO EL TIEMPO
                                estoyMidiendo = false;
                                intervalos++;
                                Log.i("pepe", "reseteo y escribo " + intervalos);
                                MyLog.write(tasks.get(0).topActivity.getPackageName() + ":" + intervalos, "Mediciones", false);
                                intervalos = 0;
                            }
                        }

                    }

                }

                //Ver quien mato a roger rabbit

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

            handler5.postDelayed(responseTimeChecker, RESPONSE_TIME_CHECK_INTERVAL);

        }

    }

    private class SpaceChecker implements Runnable {
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

            //Esto desp se borra, es para probar nomas
            if(MyLog.superolos5MB() && !aviso) {
                notificacion();
                aviso = true;
            }

            handler4.postDelayed(spaceChecker, SPACE_CHECK_INTERVAL);

        }

    }

    private void getCPUPerApp(){

        try {
            // -m 10, how many entries you want, -d 1, delay by how much, -n 1,
            // number of iterations
            Process p = Runtime.getRuntime().exec("top -m 5 -d 0 -n 1");

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            int j = 0;
            String datos = "C:";
            while (line != null) {
                line = reader.readLine();
                if(line != null) {
                    String[] split = line.split(" ");
                    String s = "";

                    for (int i = 0; i < split.length; i++) {
                        if (!split[i].equals(""))
                            s += "_" + split[i];

                    }

                    if (s.contains("%") && j > 1) {
                        String[] split2 = s.split("_");
                        if (split2.length == 11)
                            if (!split2[2].replace("%", "").equals("0") && !split2[10].equals("top"))
                                //Log.i("pepe", split2[10] + ":" + split2[1] + ":" + split2[2]);
                                datos += split2[10] + ":" + split2[1] + ":" + split2[2] + "-";

                    }
                }
                j++;
            }

            if(!datos.equals("C:"))
                //Log.i("pepe", datos.substring(0, datos.length() - 1));
                MyLog.write(datos.substring(0, datos.length() - 1), "Mediciones", false);

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
                    MyLog.write(nextLine.substring(25), "Tiempos", false);
            }
            // Process line
        }

    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        handler.removeCallbacks(oneSecondChecker);
        //handler3.removeCallbacks(logWatcher);
        handler4.removeCallbacks(spaceChecker);
        handler5.removeCallbacks(responseTimeChecker);
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
