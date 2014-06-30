package com.caece.proyectofinal.Activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.caece.proyectofinal.R;
import com.caece.proyectofinal.Services.EventService;
import com.caece.proyectofinal.Utils.Device;
import com.caece.proyectofinal.Utils.MemoryStatus;
import com.caece.proyectofinal.Utils.MyLog;
import com.caece.proyectofinal.Utils.Preferencias;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends SherlockActivity {

    ProgressBar pbRam;
    ProgressBar pbMI;
    ProgressBar pbME;
    TextView textViewRamValor;
    TextView textViewMIValor;
    TextView textViewMEValor;
    long totalRAM;
    long currentRAM;
    int RAMLevel;
    int MILevel;
    int MELevel;
    Button liberar;
    Button apps;

    private Handler mHandler = new Handler();

    private Timer mTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        setUI();

        Intent oldService = new Intent(this, EventService.class);       //Detenemos el servicio, si es que estaba corriendo
        stopService(oldService);

        if(Preferencias.primeraCorrida(this)) {                           //Si es la primera corrida, se da de alta el dispositivo

            //Generar archivo con datos y enviarlo
            collectData();
            MyLog.subirAlServidor("Datos");

        }

        if(!ActivityManager.isUserAMonkey()) {                          //Si el usuario no es un mono, iniciamos el servicio
            Intent newService = new Intent(this, EventService.class);
            startService(newService);
        }

        getDatos();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, 50);

    }

    private void setUI() {

        pbRam = (ProgressBar)findViewById(R.id.progressBarRam);
        pbMI = (ProgressBar)findViewById(R.id.progressBarMI);
        pbME = (ProgressBar)findViewById(R.id.progressBarME);
        textViewMEValor = (TextView) findViewById(R.id.textViewMEValor);
        textViewRamValor = (TextView) findViewById(R.id.textViewRamValor);
        textViewMIValor = (TextView) findViewById(R.id.textViewMIValor);
        liberar = (Button) findViewById(R.id.buttonLiberar);
        apps = (Button) findViewById(R.id.buttonApps);

        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirApps();
            }
        });

        liberar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                liberar();
            }
        });

    }

    private void liberar(){

        Device.liberarMemoria(this);
        Toast.makeText(this, "Memoria liberada", 500).show();
        totalRAM = Device.getTotalMemory();
        currentRAM = Device.getCurrentRAM(this);
        RAMLevel = (int) (100 - (currentRAM*100) / totalRAM);
        pbRam.setProgress(RAMLevel);
        textViewRamValor.setText(pbRam.getProgress() + "%");

    }

    private void abrirApps(){

        startActivity(new Intent(this, AppsActivity.class));

    }

    private class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if(actualizarDatos())
                        mTimer.cancel();

                }

            });
        }

    }

    private Boolean actualizarDatos(){

        Boolean esta = true;
        if(pbRam.getProgress() != RAMLevel){

            pbRam.setProgress(pbRam.getProgress() + 1);
            textViewRamValor.setText(pbRam.getProgress() + "%");
            esta = false;

        }

        if(pbMI.getProgress() != MILevel){

            pbMI.setProgress(pbMI.getProgress() + 1);
            textViewMIValor.setText(pbMI.getProgress() + "%");
            esta = false;

        }

        if(pbME.getProgress() != MELevel){

            pbME.setProgress(pbRam.getProgress() + 1);
            textViewMEValor.setText(pbME.getProgress() + "%");
            esta = false;

        }

        return esta;

    }

    private void getDatos() {

        long sdAvailable = MemoryStatus.getAvailableExternalMemorySize();
        long sdTotal = MemoryStatus.getTotalExternalMemorySize();
        MELevel = (int) (100 - (sdAvailable * 100) / sdTotal);
        long romAvailable = MemoryStatus.getAvailableInternalMemorySize();
        long romTotal = MemoryStatus.getTotalInternalMemorySize();
        MILevel = (int) (100 - (romAvailable * 100) / romTotal);

        totalRAM = Device.getTotalMemory();
        currentRAM = Device.getCurrentRAM(this);
        RAMLevel = (int) (100 - (currentRAM*100) / totalRAM);

    }

    @Override
    protected void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_settings)
            preferencias();

        return super.onOptionsItemSelected(item);
    }

    private void preferencias(){

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    private void collectData(){

        String id = Device.getDeviceId(this);
        String model = Device.getDeviceName();
        int api = Build.VERSION.SDK_INT;
        MyLog.write(id+ ":" + model + ":" + api, "Datos", true);

    }

}
