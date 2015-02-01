package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;

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

                handler.postDelayed(espera, 300000);        //re intento subir dentro de 5 mins
                Log.i("pepe", "reintento");

            }
        }
    }

    private void cancelar(){

        handler.removeCallbacks(espera);

    }

}
