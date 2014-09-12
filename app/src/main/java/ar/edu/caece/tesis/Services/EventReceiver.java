package ar.edu.caece.tesis.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import ar.edu.caece.tesis.R;
import ar.edu.caece.tesis.Utils.Battery;
import ar.edu.caece.tesis.Utils.MyLog;
import ar.edu.caece.tesis.Utils.OSOperations;
import ar.edu.caece.tesis.Utils.Preferencias;

public class EventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null /*|| intent.getExtras() == null*/)
        {
            return;
        }

        Bundle extras = intent.getExtras();
        //Log.i("pepe", "El action es: " + intent.getAction());
        context.startService(new Intent(context, EventService.class));
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo networkInfo = extras != null ? (NetworkInfo) extras.getParcelable("networkInfo") : null;
            if(networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED && networkInfo.getState() != NetworkInfo.State.DISCONNECTED) return;
            WifiInfo wifiInfo = extras.getParcelable("wifiInfo");
            if(networkInfo.getState() == NetworkInfo.State.CONNECTED && wifiInfo == null)return;

            if(networkInfo.isConnected())
            {
                Log.i("pepe", "Conectado a wifi");
                //MyLog.write("Conectado a wifi", "Mediciones", false);
            }
            else
            {
                Log.i("pepe", "Desconectado a wifi");
                //MyLog.write("Desconectado a wifi", "Mediciones", false);
            }

        }
        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
        {
            /*wifi_state(int), previous_wifi_state(int)*/
            int wifiState = extras != null ? extras.getInt("wifi_state") : -1;
            if (wifiState != WifiManager.WIFI_STATE_DISABLED && wifiState != WifiManager.WIFI_STATE_ENABLED)
                return;
            switch (wifiState)
            {
                case WifiManager.WIFI_STATE_DISABLED:
                    MyLog.write("WifiOFF:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    MyLog.write("WifiON:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
                    break;
            }

        }
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
        {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if(state != BluetoothAdapter.STATE_OFF && state != BluetoothAdapter.STATE_ON
                    && state != BluetoothAdapter.STATE_CONNECTED && state != BluetoothAdapter.STATE_DISCONNECTED) return;
            switch (state)
            {
                case BluetoothAdapter.STATE_OFF:
                    MyLog.write("B/TOFF:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
                    break;
                case BluetoothAdapter.STATE_ON:
                    MyLog.write("B/TON:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    //MyLog.write("B/T Conectado", "Mediciones", false);
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    //MyLog.write("B/T Desconectado", "Mediciones", false);
                    break;
            }

        }
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
        {

        }
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
        {

        }
        if(intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        {
            MyLog.write("SMSR:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))
        {

            MyLog.write("BC:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        {
            boolean isEnabled = extras != null && extras.getBoolean("state");
            if(isEnabled)
                MyLog.write("AvionON:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
            else MyLog.write("AvionOFF:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsEnabled)
                MyLog.write("GPSON:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
            else MyLog.write("GPSOFF:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION"))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
        {
            MyLog.write("IntLL:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        {
            if(extras != null && !TelephonyManager.EXTRA_STATE_RINGING.equals(extras.getString(TelephonyManager.EXTRA_STATE)))
            {
                return;
            }

            MyLog.write("RecLL:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);

        }
        if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
        {
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if(landscape)
                MyLog.write("LSC:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
            else MyLog.write("PRT:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_REBOOT))
        {
            MyLog.write("RB:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
            //Sumar y guardar los datos utilizados en las preferencias
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesDiarios");
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesMensuales");
            abortBroadcast(); //esto es para que cancelar la recepcion de datos
        }
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN))
        {
            MyLog.write("SH:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
            //Sumar y guardar los datos utilizados en las preferencias
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesDiarios");
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesMensuales");
            abortBroadcast(); //esto es para que cancelar la recepcion de datos
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            MyLog.write("PC:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            MyLog.write("PD:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
        {
            String packageName = intent.getData().toString().replace("package:", "");
            String app = OSOperations.getAppNameFromPackageName(context.getPackageManager(), packageName);
            MyLog.write("AI:" + packageName + ":" + app, "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_DATA_CLEARED))
        {
            String packageName = intent.getData().toString().replace("package:", "");
            MyLog.write("ADC:" + packageName, "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
        {
            String packageName = intent.getData().toString().replace("package:", "");
            MyLog.write("AU:" + packageName, "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
        {
            if(extras != null)
            {
                int state = extras.getInt(BatteryManager.EXTRA_STATUS);
                int level = extras.getInt(BatteryManager.EXTRA_LEVEL);
                switch (state)
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        //MyLog.write("BatC:" + level, "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        //MyLog.write("BatD:" + level, "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        MyLog.write("FB:" + level, "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        //MyLog.write("No se esta cargando", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        //MyLog.write("No se sabe el estado de la bateria", "Mediciones", false);
                        break;
                }
            }

        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            Log.i("pepe", "LB:" + Battery.getLevel(context.getApplicationContext()));
            MyLog.write("LB", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED))
        {

        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            MyLog.write("ScOFF:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            MyLog.write("ScON:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            MyLog.write("UP:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW))
        {
            MyLog.write("SL:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_OK))
        {
            MyLog.write("SOK:" + Battery.getLevel(context.getApplicationContext()), "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED))
        {
            //MyLog.write("Media scanner started", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED))
        {
            //MyLog.write("Media scanner finished", "Mediciones", false);
        }
    }
}
