package com.caece.proyectofinal.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lea on 6/09/13.
 */
public class Preferencias {

    public static Boolean DatosHabilitadas(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String pepe = sharedPrefs.getString("internet_list", "");
        if(pepe.equals("")){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("internet_list", "0");
            editor.commit();
        }

        if(sharedPrefs.getString("internet_list", "").equals("0"))
            return true;
        else return false;

    }

    public static Boolean primeraCorrida(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean primeraCorrida = sharedPrefs.getBoolean("primeraCorrida", true);

        if (primeraCorrida)
            sharedPrefs.edit().putBoolean("primeraCorrida", false).commit();

        return primeraCorrida;

    }

}
