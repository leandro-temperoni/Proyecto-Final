package com.caece.proyectofinal.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

/**
 * Created by COCO on 18/05/2014.
 */
public class MyLog {

    public static void write(String text, String name){

        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDir.getAbsolutePath() + "/ProyectoFinal/logs");
        if(!dir.exists())
            dir.mkdirs();
        File myFile = new File(dir, name + ".txt");

        try
        {
            long fileLength = myFile.length();
            RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
            raf.seek(fileLength);
            raf.writeBytes(text + "\n");
            raf.close();
        } catch(Exception e)
        {
            Log.i("pepe", e.getMessage());
        }

    }

}
