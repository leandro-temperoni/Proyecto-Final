package ar.edu.caece.tesis.Services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ar.edu.caece.tesis.Activities.ServiceRestartActivity;
import ar.edu.caece.tesis.Utils.Battery;
import ar.edu.caece.tesis.Utils.Device;
import ar.edu.caece.tesis.Utils.MemoryStatus;
import ar.edu.caece.tesis.Utils.MyLog;
import ar.edu.caece.tesis.Utils.Notificacion;
import ar.edu.caece.tesis.Utils.OSOperations;
import ar.edu.caece.tesis.Utils.Preferencias;

public class EventService extends Service {

    private EventReceiver manualRegisterReceiver;
    private String lastActiveApp = "";
    private List<ActivityManager.RunningTaskInfo> tasksAnteriores;
    private Handler handlerOneSecond;
    private Handler handlerLogWatcher;
    private Handler handlerTenMinutesChecker;
    private OneSecondChecker oneSecondChecker = new OneSecondChecker();
    private LogWatcher logWatcher = new LogWatcher();
    private TenMinutesChecker tenMinutesChecker = new TenMinutesChecker();

    private int ONE_SECOND_CHECK_INTERVAL = 1000;
    private int SPACE_CHECK_INTERVAL = 600000;

    int dia;

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

        //configuration
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        //storage
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);

        //packages
        //intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        //intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        //intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        //intentFilter.addDataScheme("package");

        //power
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        if(!Preferencias.yaLocalize(this)) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        }

        SMSObserver smsObserver = new SMSObserver(new Handler(), this);
        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver);

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

        Calendar calendar = Calendar.getInstance();
        dia = calendar.get(Calendar.DAY_OF_YEAR);

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

    private int getNextId1Min(){ return Preferencias.getNextIdMedicion1Min(this); }
    private int getNextId10Min(){ return Preferencias.getNextIdMedicion10Min(this); }

    private class OneSecondChecker implements Runnable
    {
        @Override
        public void run()
        {
            long time = Calendar.getInstance().getTimeInMillis();
            int id = getNextId1Min();

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

                            MyLog.write("FA:" + pn, "Mediciones", time, id);

                        }
                    }
                }
            }

            //Memoria saturada
            if(memoriaSaturada())
                MyLog.write("MS", "Mediciones", time, id);

            getCPUPerApp(time, id);

            //Si se abrio una nueva app o se cerro, se registra el grado de multiprogramacion y la memoria por app
            if(nuevaApp) {
                List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
                MyLog.write("MP:" + processes.size(), "Mediciones",time, id);
                getMemPerApp(processes, time, id);
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
                            if (lastActiveApp.equals("com.sec.android.app.controlpanel") || lastActiveApp.equals("com.android.systemui"))
                                MyLog.write("CBU:" + name, "Mediciones", time, id);
                            else if (memoriaSaturada())
                                    MyLog.write("CBA:" + name, "Mediciones", time, id);
                                else MyLog.write("CBUN:" + name, "Mediciones", time, id);
                        }
                    }

                }

                tasksAnteriores = tasks;

            }

            handlerOneSecond.postDelayed(oneSecondChecker, ONE_SECOND_CHECK_INTERVAL);
        }
    }

    private void getMemPerApp(List<ActivityManager.RunningAppProcessInfo> processes, long time, int id){

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
                data += OSOperations.getAppNameFromPackageName(getPackageManager(), pns.get(i)) + ":" + pids[i] + ":" + info.getTotalPss() + "-";
            i++;

        }

        if(!data.equals("M:")) {
            MyLog.write(data.substring(0, data.length() - 1), "Mediciones", time, id);
            //Log.i("pepe", data);
        }

    }

    private Boolean memoriaSaturada(){ return Device.getCurrentRAMState(this); }

    private class TenMinutesChecker implements Runnable {
        @Override
        public void run() {

            long time = Calendar.getInstance().getTimeInMillis();
            int id = getNextId10Min();

            long sdAvailable = MemoryStatus.getAvailableExternalMemorySize();
            long sdTotal = MemoryStatus.getTotalExternalMemorySize();
            long sdLevel = 100 - (sdAvailable * 100) / sdTotal;

            long romAvailable = MemoryStatus.getAvailableInternalMemorySize();
            long romTotal = MemoryStatus.getTotalInternalMemorySize();
            long romLevel = 100 - (romAvailable * 100) / romTotal;

            MyLog.write("MI:" + String.valueOf(romLevel), "Mediciones", time, id);
            MyLog.write("ME:" + String.valueOf(sdLevel), "Mediciones", time, id);
            MyLog.write("BL:" + String.valueOf(nivelBateria()), "Mediciones", time, id);
            MyLog.write("DR:" + String.valueOf(TrafficStats.getTotalRxBytes()), "Mediciones", time, id);
            MyLog.write("DS:" + String.valueOf(TrafficStats.getTotalTxBytes()), "Mediciones", time, id);

            chequearLimites((int)romLevel, (int)sdLevel, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()));

            handlerTenMinutesChecker.postDelayed(tenMinutesChecker, SPACE_CHECK_INTERVAL);

        }

    }

    private void chequearLimites(int romLevel, int sdLevel, long datosMoviles){

        if(Preferencias.superaLimiteMemoriaInterna(this, romLevel))
            Notificacion.mostrar(this, "Memoria interna", "Has superado el limite", Notificacion.ID_MEMORIA_INTERNA);

        if(Preferencias.superaLimiteMemoriaExterna(this, sdLevel))
            Notificacion.mostrar(this, "Memoria externa", "Has superado el limite", Notificacion.ID_MEMORIA_EXTERNA);

        if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != dia){            //Si cambio el dia, reseteo el contador para el
                                                                                //dia siguiente
            Preferencias.limpiarDatosMoviles(this, "datosMovilesDiarios");
            dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            Preferencias.setAviseDatosDiarios(this, false);
            MyLog.subirAlServidor("Mediciones.txt", this);       //y subo los datos al servidor

        }
        else if(Preferencias.superaLimiteDatos(this, datosMoviles, "datos_diarios_list", "datosMovilesDiarios") && !Preferencias.yaAviseDatosDiarios(this)) {
            Notificacion.mostrar(this, "Datos moviles diarios", "Has superado el limite", Notificacion.ID_DATOS_DIARIOS);
            Preferencias.setAviseDatosDiarios(this, true);
        }

        if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == Preferencias.getDiaCambioMes(this)){ //Si es el dia de cambio, reseteo el contador para el
                                                                                                    //mes siguiente
            Preferencias.limpiarDatosMoviles(this, "datosMovilesMensuales");
            Preferencias.setAviseDatosMensuales(this, false);

        }
        else if(Preferencias.superaLimiteDatos(this, datosMoviles, "datos_mensuales_list", "datosMovilesMensuales") && !Preferencias.yaAviseDatosMensuales(this)){
                Notificacion.mostrar(this, "Datos moviles mensuales", "Has superado el limite", Notificacion.ID_DATOS_MENSUALES);
                Preferencias.setAviseDatosMensuales(this, true);
            }

    }

    private void getCPUPerApp(long time, int id){

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
                MyLog.write(datos.substring(0, datos.length() - 1), "Mediciones", time, id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int nivelBateria(){ return Battery.getLevel(this); }

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
                if(nextLine.contains("Displayed")) {
                    String line = nextLine.substring(25).replace("Displayed", "RT:").replace(" ", "");
                    String pn = line.substring(3, line.indexOf("/"));
                    //line = line.replace(pn, OSOperations.getAppNameFromPackageName(getPackageManager(), pn));
                    MyLog.write(line, "Mediciones", true);
                }
            }

        }

    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        if(handlerOneSecond != null)
            handlerOneSecond.removeCallbacks(oneSecondChecker);
        if(handlerLogWatcher != null)
            handlerLogWatcher.removeCallbacks(logWatcher);
        if(handlerTenMinutesChecker != null)
            handlerTenMinutesChecker.removeCallbacks(tenMinutesChecker);
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
