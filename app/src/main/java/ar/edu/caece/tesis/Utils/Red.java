package ar.edu.caece.tesis.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lea on 6/09/13.
 */

public class Red {

    public static Boolean conectadoAWifi(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if(activeNetwork != null)
            isConnected = activeNetwork.isConnectedOrConnecting();

        boolean isWiFi = false;

        if(isConnected)
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        return isWiFi;

    }

    public static Boolean conectadoADatos(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if(activeNetwork != null)
            isConnected = activeNetwork.isConnectedOrConnecting();

        boolean is3G = false;

        if(isConnected)
            is3G = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        return is3G;

    }

}
