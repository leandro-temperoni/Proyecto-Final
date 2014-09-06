package ar.edu.caece.tesis.Utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import ar.edu.caece.tesis.Services.UploaderService;

/**
 * Created by COCO on 02/09/2014.
 */
public class DataSender {

    private Handler handler;
    private ElQueEsperaDesespera espera = new ElQueEsperaDesespera();
    private File file;
    private String id;
    private String fecha;
    private Context context;

    public DataSender(File file, String id, String fecha, Context context){

        this.file = file;
        this.id = id;
        this.fecha = fecha;
        this.context = context;

    }

    public void send(){

        HandlerThread thread = new HandlerThread("espera");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.post(espera);

    }

    private class ElQueEsperaDesespera implements Runnable
    {
        @Override
        public void run()
        {

            Boolean result = false;
            if(Preferencias.DatosHabilitadas(context)){
                if(Red.conectadoADatos(context) || Red.conectadoAWifi(context))
                    result = HttpFileUploader.uploadFile(file, id, fecha);
            }
            else if(Red.conectadoAWifi(context))
                result = HttpFileUploader.uploadFile(file, id, fecha);


            if(result) {     //envie datos al servidor y no hubo error  (ya sea error del server o que no hay conexion a internet)
                file.delete();
                Log.i("barcelona", "envie piola");
                cancelar();        //borro el archivo y cancelo el timer
            }
            else {

                handler.postDelayed(espera, 3000);        //re intento subir dentro de 5 mins
                Log.i("pepe", "reintento");

            }
        }
    }

    private void cancelar(){

        if(file.getName().contains("Mediciones")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Intent myIntent = new Intent(context, UploaderService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }

        handler.removeCallbacks(espera);

    }

}
