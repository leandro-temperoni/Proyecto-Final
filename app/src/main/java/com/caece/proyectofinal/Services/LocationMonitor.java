package com.caece.proyectofinal.Services;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.caece.proyectofinal.Utils.MyLog;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by COCO on 06/06/2014.
 */
public class LocationMonitor {

    private final Context context;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public LocationMonitor(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startListening() {

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                if(location != null)
                    MyLog.write(location.getLongitude() + "-" + location.getLatitude(), "Coordenadas", true);

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

}
