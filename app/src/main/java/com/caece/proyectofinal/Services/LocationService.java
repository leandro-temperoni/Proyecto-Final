package com.caece.proyectofinal.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.caece.proyectofinal.Utils.Device;
import com.caece.proyectofinal.Utils.MyLog;
import com.caece.proyectofinal.Utils.Notificacion;

import java.util.Calendar;

public class LocationService extends Service {

    @Override
    public void onCreate() {

        super.onCreate();

        Location location = Device.getCurrentLocation(this);
        if(location != null) {
            MyLog.write(location.getLongitude() + "-" + location.getLatitude(), "Coordenadas", true);
            Notificacion.mostrar(this, "Localizacion", "Medi a las " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        }
        else Notificacion.mostrar(this, "Localizacion", "Error al medir");

        long time = Calendar.getInstance().getTimeInMillis() + 43200000;
        PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, LocationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, pi);     //Programo ejecutar el servicio de localizacion dentro de 12 hs

        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
