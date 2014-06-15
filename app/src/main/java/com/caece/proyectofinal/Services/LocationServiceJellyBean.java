package com.caece.proyectofinal.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.caece.proyectofinal.Utils.MyLog;
import com.caece.proyectofinal.Utils.Notificacion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.Calendar;

public class LocationServiceJellyBean extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient locationClient;
    private LocationListener locationListener;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationClient
                = new LocationClient(this, this, this);
        locationClient.connect();

    }

    private void detenerYProgramar(){

        locationClient.removeLocationUpdates(locationListener);
        locationClient.disconnect();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 12);
        PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, LocationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);     //Programo ejecutar el servicio de localizacion dentro de 12 hs

        stopSelf();

    }

    private boolean googlePlayServicesInstalled() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }

    }

    private void analizeLocation(Location location){

        if(location != null) {
            MyLog.write(location.getLongitude() + "-" + location.getLatitude(), "Coordenadas", true);
            Notificacion.mostrar(this, "Localizacion", location.getLongitude() + "-" + location.getLatitude());
            detenerYProgramar();
        }
        else Notificacion.mostrar(this, "Localizacion", "Error al medir");

    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status

        if(googlePlayServicesInstalled()) {

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    analizeLocation(location);
                }
            };

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationClient.requestLocationUpdates(locationRequest, locationListener);

        }

    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
