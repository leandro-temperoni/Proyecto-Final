package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;

/**
 * Created by COCO on 18/05/2014.
 */
public class MyLog {

    public static void write(String text, String name, Boolean timestamp){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name + ".txt");
        Calendar calendar = Calendar.getInstance();

        Log.i("pepe", text);

        try
        {
            long fileLength = myFile.length();
            RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
            raf.seek(fileLength);
            if(timestamp)
                raf.writeBytes(text + " - " + calendar.getTimeInMillis() + "\n");
            else raf.writeBytes(text + "\n");
            raf.close();
        } catch(Exception e)
        {
            Log.i("pepe", e.getMessage());
        }

    }

    public static void write(String text, String name, long timestamp, int id){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name + ".txt");

        Log.i("pepe", text);

        try
        {
            long fileLength = myFile.length();
            RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
            raf.seek(fileLength);
            raf.writeBytes(text + " - " + timestamp + " - " + id + "\n");
            raf.close();
        } catch(Exception e)
        {
            Log.i("pepe", e.getMessage());
        }

    }

    public static void subirAlServidor(String name, Context context){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name);

        Calendar calendar = Calendar.getInstance();
        String fecha = calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR);
        Log.i("barcelona", fecha);
        String id = Device.getDeviceId(context);
        Log.i("barcelona", id);
        Log.i("barcelona", myFile.getName());

        DataSender dataSender = new DataSender(myFile, id, fecha, context);
        dataSender.send();

    }

}
