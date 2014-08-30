package ar.edu.caece.tesis.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import java.util.Calendar;

import ar.edu.caece.tesis.Utils.MyLog;
import ar.edu.caece.tesis.Utils.Preferencias;

public class LocationService extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    public void onCreate() {

        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        startListening();

    }

    private void analizeLocation(Location location){

        if(location != null) {
            MyLog.write(location.getLongitude() + "-" + location.getLatitude(), "Coordenadas", true);
            detenerYProgramar();
        }

    }

    public void startListening() {

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                analizeLocation(location);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public void stopListening() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationManager = null;
            locationListener = null;
        }
    }

    private void detenerYProgramar(){

        stopListening();

        Calendar calendar = Calendar.getInstance();

        if(Preferencias.getTimePrimeraCorrida(this) + 259200000 > calendar.getTimeInMillis()) {      //Si no llevo 3 dias midiendo, sigo

            calendar.add(Calendar.HOUR, 1);
            PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, LocationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);     //Programo ejecutar el servicio de localizacion dentro de 1 hora

        }
        else { //Dejo de medir y envio al servidor

            Preferencias.cancelarLocalizacion(this);
            MyLog.subirAlServidor("Coordenadas.txt", this);

        }

        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
