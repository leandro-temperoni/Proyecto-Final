package com.caece.proyectofinal.Utils;

import android.content.Context;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by lea on 18/06/13.
 */
public class Root {

    public static Boolean pedirPermisoSuperUsuario(){

        Process p;

        try {
            p = Runtime.getRuntime().exec("su");

        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        os.writeBytes("echo \"Do I have root?\" >/system/sd/temporary.txt\n");

        os.writeBytes("exit\n");
        os.flush();
        try {
            p.waitFor();
            if (p.exitValue() != 255) {
                return true;
            }
            else {
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }
    } catch (IOException e) {
        return false;
    }

    }

    public static void mount(Context context){

        Process p;
        try {

            String cmd = "mount -o rw,remount -t ext3 /dev/block/mmcblk1p21 /";
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.flush();

        } catch (IOException e) {

        }

    }

}
