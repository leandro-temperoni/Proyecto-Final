package com.caece.proyectofinal.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import java.util.ArrayList;

/**
 * Created by lea on 19/06/13.
 */
public class OSOperations {

    public static final int USER_APPS = 0;
    public static final int SYSTEM_APPS = 1;

    public static void desinstalarPaquete(Context context, String packageString){

        Uri packageURI = Uri.parse("package:" + packageString);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(intent);

    }

    public static String getAppNameFromPackageName(PackageManager pm,String packageName){

        String appName;
        try
        {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            appName = (String) pm.getApplicationLabel(ai);
        }
        catch (Exception e)
        {
            appName = "[" + "Unknowed app" + "]";
        }

        return appName;

    }

    public static ArrayList<ApplicationInfo> getInstalledApps(Context context, int filter){

        final PackageManager pm = context.getPackageManager();

        ArrayList<ApplicationInfo> packages = (ArrayList<ApplicationInfo>) pm.getInstalledApplications(PackageManager.GET_META_DATA);

        ArrayList<ApplicationInfo> apps =  new ArrayList<ApplicationInfo>();

        for(ApplicationInfo applicationInfo : packages){

            if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == filter){

                apps.add(applicationInfo);

            }

        }

        return apps;

    }

    public static ArrayList<ActivityManager.RunningAppProcessInfo> getRunningApps(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return (ArrayList<ActivityManager.RunningAppProcessInfo>) manager.getRunningAppProcesses();

    }

    public static ArrayList<ActivityManager.RunningServiceInfo> getRunningServices(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return (ArrayList<ActivityManager.RunningServiceInfo>) manager.getRunningServices(100);

    }

    public static void abrirDetallesApp(Context context, String packageString){

        Uri packageURI = Uri.parse("package:" + packageString);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        context.startActivity(intent);

    }

}
