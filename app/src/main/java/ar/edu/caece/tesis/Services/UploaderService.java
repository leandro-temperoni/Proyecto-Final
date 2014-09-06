package ar.edu.caece.tesis.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

import ar.edu.caece.tesis.Utils.DataSender;
import ar.edu.caece.tesis.Utils.MyLog;

public class UploaderService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        MyLog.subirAlServidor("Mediciones.txt", this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
