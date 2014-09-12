package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by lea on 6/09/13.
 */
public class Preferencias {

    public static int getNextIdMedicion1Min(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        int ret = sharedPrefs.getInt("idMedicionPeriodica1Min", 0) + 1;

        sharedPrefs.edit().putInt("idMedicionPeriodica1Min", ret).commit();

        return ret;

    }

    public static int getNextIdMedicion10Min(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        int ret = sharedPrefs.getInt("idMedicionPeriodica10Min", 0) + 1;

        sharedPrefs.edit().putInt("idMedicionPeriodica10Min", ret).commit();

        return ret;

    }

    public static Boolean DatosHabilitadas(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(sharedPrefs.getString("internet_list", "1").equals("0"))
            return true;
        else return false;

    }

    public static Boolean notificacionesHabilitadas(Context context){

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", false);

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
        return sharedPrefs.getBoolean("yaLocalize", false);

    }

    public static void cancelarLocalizacion(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("yaLocalize", true).commit();

    }

    public static Boolean yaAviseDatosDiarios(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("yaAviseDiarios", false);

    }

    public static void setAviseDatosDiarios(Context context, Boolean valor){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("yaAviseDiarios", valor).commit();

    }

    public static Boolean yaAviseDatosMensuales(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("yaAviseMensuales", false);

    }

    public static void setAviseDatosMensuales(Context context, Boolean valor){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean("yaAviseMensuales", valor).commit();

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
        if(valor.equals("")){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("memint_list", "70");
            editor.commit();
            valor = sharedPrefs.getString("memint_list", "");
        }

        if(Integer.parseInt(valor) < romLevel)
            return true;
        else return false;

    }

    public static Boolean superaLimiteMemoriaExterna(Context context, int sdLevel){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String valor = sharedPrefs.getString("memext_list", "");
        if(valor.equals("")){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("memext_list", "80");
            editor.commit();
            valor = sharedPrefs.getString("memext_list", "");
        }

        if(Integer.parseInt(valor) < sdLevel)
            return true;
        else return false;

    }

    public static Boolean superaLimiteDatos(Context context, long cantidad, String tipo, String tipo2){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String valor = sharedPrefs.getString(tipo, "");
        if(valor.equals("")){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            if(tipo.equals("datos_diarios_list"))
                editor.putString(tipo, "5");
            else editor.putString(tipo, "200");
            editor.commit();
            valor = sharedPrefs.getString(tipo, "");
        }

        String extra = sharedPrefs.getString(tipo2, "");
        if(extra.equals(""))
            extra = "0";
        cantidad += Long.parseLong(extra);

        cantidad = cantidad/1000000;        //paso de bytes a Mbytes
        if(Long.parseLong(valor) < cantidad)
            return true;
        else return false;

    }

    public static long getTimePrimeraCorrida(Context context){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getLong("timePrimeraCorrida", 0);

    }

    public static void sumarDatosMoviles(Context context, long cantidad, String tipo){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String actual = sharedPrefs.getString(tipo, "");
        if(actual.equals("")) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(tipo, "0");
            editor.commit();
            actual = sharedPrefs.getString(tipo, "");
        }
        sharedPrefs.edit().putLong(tipo, cantidad + Long.parseLong(actual)).commit();

    }

    public static void limpiarDatosMoviles(Context context, String tipo){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putString(tipo, "0").commit();

    }

    public static int getDiaCambioMes(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String valor = sharedPrefs.getString("dia_datos_diarios_list", "");
        if(valor.equals("")){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("dia_datos_diarios_list", "1");
            editor.commit();
            valor = sharedPrefs.getString("dia_datos_diarios_list", "");
        }
        return Integer.parseInt(valor);

    }
}
