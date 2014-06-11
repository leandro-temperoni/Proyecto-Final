package com.caece.proyectofinal.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.caece.proyectofinal.R;
import com.caece.proyectofinal.Services.EventService;
import com.caece.proyectofinal.Utils.Device;
import com.caece.proyectofinal.Utils.MyLog;
import com.caece.proyectofinal.Utils.Notificacion;
import com.caece.proyectofinal.Utils.Preferencias;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent oldService = new Intent(this, EventService.class);       //Detenemos el servicio, si es que estaba corriendo
        stopService(oldService);

        if(!ActivityManager.isUserAMonkey()) {                          //Si el usuario no es un mono, iniciamos el servicio
            Intent newService = new Intent(this, EventService.class);
            startService(newService);
        }

        if(Preferencias.primeraCorrida(this)) {                           //Si es la primera corrida, se da de alta el dispositivo

            //Generar archivo con datos y enviarlo
            collectData();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    private void collectData(){

        String id = Device.getDeviceId(this);
        String model = Device.getDeviceName();
        int api = Build.VERSION.SDK_INT;
        MyLog.write(id+ ":" + model + ":" + api, "Datos", true);

    }

}
