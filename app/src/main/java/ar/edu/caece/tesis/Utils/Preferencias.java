package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Calendar;

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

    public static Boolean notificacionesHabilitadas(Context context){

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);

    }

    public static Uri getRingtone(Context context){

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        String strRingtonePreference = preference.getString("notifications_ringtone", "DEFAULT_SOUND");
        return Uri.parse(strRingtonePreference);

    }

    public static Boolean vibracionHabilitada(Context context){

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_vibrate", true);

    }

    public static Boolean yaLocalize(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("yaLocalize", true);

    }

    public static void cancelarLocalizacion(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("yaLocalize", true).commit();

    }

    public static Boolean primeraCorrida(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean primeraCorrida = sharedPrefs.getBoolean("primeraCorrida", true);

        if (primeraCorrida){

            sharedPrefs.edit().putBoolean("primeraCorrida", false).commit();
            sharedPrefs.edit().putLong("timePrimeraCorrida", Calendar.getInstance().getTimeInMillis()).commit();

        }


        return primeraCorrida;

    }

    public static Boolean superaLimiteMemoriaInterna(Context context, int romLevel){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String valor = sharedPrefs.getString("memint_list", "");

        if(Integer.parseInt(valor) < romLevel)
            return true;
        else return false;

    }

    public static Boolean superaLimiteMemoriaExterna(Context context, int sdLevel){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String valor = sharedPrefs.getString("memext_list", "");

        if(Integer.parseInt(valor) < sdLevel)
            return true;
        else return false;

    }

    public static Boolean superaLimiteDatos(Context context, long cantidad, String tipo, String tipo2){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        long valor = sharedPrefs.getLong(tipo, 0);

        cantidad += sharedPrefs.getLong(tipo2, 0);

        if(valor < cantidad)
            return true;
        else return false;

    }

    public static long getTimePrimeraCorrida(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getLong("timePrimeraCorrida", 0);

    }

    public static void sumarDatosMoviles(Context context, long cantidad, String tipo){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long actual = sharedPrefs.getLong(tipo, 0);
        sharedPrefs.edit().putLong(tipo, cantidad + actual).commit();

    }

    public static void limpiarDatosMoviles(Context context, String tipo){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putLong(tipo, 0).commit();

    }

}
