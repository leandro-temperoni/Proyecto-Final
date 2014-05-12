package com.caece.proyectofinal.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lea on 18/06/13.
 */
public class Archivo {

    public static void compartir(File file, Context context){

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
        String type = mime.getMimeTypeFromExtension(ext.toLowerCase());

        intent.setDataAndType(Uri.fromFile(file),type);

        context.startActivity(Intent.createChooser(intent, "Compartir a"));

    }

    public static void abrirArchivo(File file, Context context) {

        try {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
            String type = mime.getMimeTypeFromExtension(ext.toLowerCase());

            intent.setDataAndType(Uri.fromFile(file),type);

            context.startActivity(intent);

        }
        catch (Exception e){

            Toast.makeText(context, "Ninguna aplicación instalada puede abrir el archivo", 500).show();

        }

    }

    public static void renombrar(String path, File file, String nombre){

        file.renameTo(new File(path + "/" + nombre));

    }

    public static void copiarArchivo(File file, String path, Boolean mover){

        try{

            File f2 = new File(path + "/" + file.getName());
            InputStream in = null;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        if(mover)
            file.delete();

    }

}
