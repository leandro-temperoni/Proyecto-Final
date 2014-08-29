package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
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

    public static void writeHeader(String text, String name){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name + ".txt");
        Calendar calendar = Calendar.getInstance();

        try
        {
            RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
            raf.seek(0);
            raf.writeBytes(text + "-" + calendar.getTimeInMillis() + "\n");
            raf.close();
        } catch(Exception e)
        {
            Log.i("pepe", e.getMessage());
        }

    }

    public static Boolean superolos5MB(){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, "Mediciones.txt");

        if(myFile.length() > 5000000)
            return true;
        else return false;

    }

    public static void subirAlServidor(String name, Context context){

        writeHeader(Device.getDeviceId(context), name);     //antes de enviar escribe el ID del dispositivo

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/Tesis/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name + ".txt");
        Log.i("pepe", myFile.getName());

        HttpFileUploader uploader = new HttpFileUploader("http://tesis-oswebarg.rhcloud.com/upload.php", myFile.getName());
        try {
            uploader.doStart(new FileInputStream(myFile.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("pepe", "fallo el upload");
        }

    }

    public static void enviar(String url, File file){

        HttpClient http = AndroidHttpClient.newInstance("Tesis");
        HttpPost method = new HttpPost(url);

        method.setEntity(new FileEntity(file, "application/octet-stream"));

        try {
            HttpResponse response = http.execute(method);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            Log.i("pepe", responseString);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
