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
        //TODO only if battery save isn't enabled
        //context.startService(new Intent(context, EventService.class));
        Bundle extras = intent.getExtras();
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
                    Log.i("pepe", "WifiOFF");
                    MyLog.write("WifiOFF", "Mediciones", true);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.i("pepe", "WifiON");
                    MyLog.write("WifiON", "Mediciones", true);
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
                    Log.i("pepe", "B/TOFF");
                    MyLog.write("B/TOFF", "Mediciones", true);
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i("pepe", "B/TON");
                    MyLog.write("B/TON", "Mediciones", true);
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.i("pepe", "B/T Conectado");
                    //MyLog.write("B/T Conectado", "Mediciones", false);
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.i("pepe", "B/T Desconectado");
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
            SmsMessage[] messages = new SmsMessage[0];
            try
            {
                Object[] pdus = (Object[]) extras.get("pdus");
                messages = new SmsMessage[pdus.length];
                for(int i = 0; i < pdus.length; i++)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            String address = messages[0].getOriginatingAddress();
            Log.i("pepe", "SMSR");
            MyLog.write("SMSR", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))
        {
            Log.i("pepe", "BC");
            MyLog.write("BC", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
        {
            Log.i("pepe", "Se cambio la hora");
            //MyLog.write("Se cambio la hora", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
        {
            Log.i("pepe", "Se cambio la fecha");
            //MyLog.write("Se cambio la fecha", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
        {
            Log.i("pepe", "Se cambio la hora local");
            //MyLog.write("Se cambio la hora local", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        {
            boolean isEnabled = extras != null && extras.getBoolean("state");
            if(isEnabled)
                MyLog.write("AvionON", "Mediciones", true);
            else MyLog.write("AvionOFF", "Mediciones", true);
        }
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsEnabled)
                MyLog.write("GPSON", "Mediciones", true);
            else MyLog.write("GPSOFF", "Mediciones", true);
        }
        if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION"))
        {
            /*android.media.EXTRA_VOLUME_STREAM_TYPE=2*/
            /*android.media.EXTRA_PREV_VOLUME_STREAM_VALUE=6*/
            /*android.media.EXTRA_VOLUME_STREAM_VALUE=7*/
            /*2=ring, 3=media*/
            if(extras != null)
            {
                int prev = extras.getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE");
                int now = extras.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE");
                if(prev == now)return;
            }
            String prevVolume = extras != null ? "" + extras.getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE") : context.getString(R.string.na).toUpperCase();
            String newVolume = extras != null ? "" + extras.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE") : context.getString(R.string.na).toUpperCase();
            int streamType = extras != null ? extras.getInt("android.media.EXTRA_VOLUME_STREAM_TYPE") : -1;
            String type = "";
            if(streamType == 2) type = context.getString(R.string.ringer);
            else if(streamType == 3) type = context.getString(R.string.media);
            Log.i("pepe", "Volume changed");
            //MyLog.write("Volume changed", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
        {
            /*android.intent.extra.PHONE_NUMBER=2222*/
            String phoneNum = extras != null ? extras.getString(Intent.EXTRA_PHONE_NUMBER) : context.getString(R.string.na).toUpperCase();
            Log.i("pepe", "IntLL");
            MyLog.write("IntLL", "Mediciones", true);
        }
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        {
            if(extras != null && !TelephonyManager.EXTRA_STATE_RINGING.equals(extras.getString(TelephonyManager.EXTRA_STATE)))
            {
                return;
            }
            String phoneNum = extras != null ? extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : context.getString(R.string.na).toUpperCase();

            Log.i("pepe", "RecLL");
            MyLog.write("RecLL", "Mediciones", true);

        }
        if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
        {
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if(landscape)
                MyLog.write("LSC", "Mediciones", true);
            else MyLog.write("PRT", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_REBOOT))
        {
            MyLog.write("RB", "Mediciones", true);
            //Sumar y guardar los datos utilizados en las preferencias
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesDiarios");
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesMensuales");
            abortBroadcast(); //esto es para que cancelar la recepcion de datos
        }
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN))
        {
            MyLog.write("SH", "Mediciones", true);
            //Sumar y guardar los datos utilizados en las preferencias
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesDiarios");
            Preferencias.sumarDatosMoviles(context, (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()), "datosMovilesMensuales");
            abortBroadcast(); //esto es para que cancelar la recepcion de datos
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            Log.i("pepe", "PC");
            MyLog.write("PC", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            Log.i("pepe", "PD");
            MyLog.write("PD", "Mediciones", true);
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
            //String app = OSOperations.getAppNameFromPackageName(context.getPackageManager(), packageName);
            MyLog.write("ADC:" + packageName, "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
        {
            String packageName = intent.getData().toString().replace("package:", "");
            //String app = OSOperations.getAppNameFromPackageName(context.getPackageManager(), packageName);
            MyLog.write("AU:" + packageName, "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED) && false)
        {
            if(extras != null)
            {
                int state = extras.getInt(BatteryManager.EXTRA_STATUS);
                switch (state)
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        Log.i("pepe", "Cargando bateria");
                        MyLog.write("BatC", "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        Log.i("pepe", "Descargandose le bateria");
                        MyLog.write("BatD", "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        Log.i("pepe", "FB");
                        MyLog.write("FB", "Mediciones", true);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        Log.i("pepe", "No se esta cargando");
                        //MyLog.write("No se esta cargando", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        Log.i("pepe", "No se sabe el estado de la bateria");
                        //MyLog.write("No se sabe el estado de la bateria", "Mediciones", false);
                        break;
                }
                int level = extras.getInt(BatteryManager.EXTRA_LEVEL);
                int health = extras.getInt(BatteryManager.EXTRA_HEALTH);
                switch (health)
                {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        Log.i("pepe", "BATTERY_HEALTH_COLD");
                        //MyLog.write("BATTERY_HEALTH_COLD", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        Log.i("pepe", "BATTERY_HEALTH_DEAD");
                        //MyLog.write("BATTERY_HEALTH_DEAD", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        Log.i("pepe", "BATTERY_HEALTH_GOOD");
                        //MyLog.write("BATTERY_HEALTH_GOOD", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        Log.i("pepe", "BATTERY_HEALTH_OVER_VOLTAGE");
                        //MyLog.write("BATTERY_HEALTH_OVER_VOLTAGE", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        Log.i("pepe", "BATTERY_HEALTH_OVERHEAT");
                        //MyLog.write("BATTERY_HEALTH_OVERHEAT", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        Log.i("pepe", "BATTERY_HEALTH_UNKNOWN");
                        //MyLog.write("BATTERY_HEALTH_UNKNOWN", "Mediciones", false);
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        Log.i("pepe", "BATTERY_HEALTH_UNSPECIFIED_FAILURE");
                        //MyLog.write("BATTERY_HEALTH_UNSPECIFIED_FAILURE", "Mediciones", false);
                        break;
                }

            }

        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            Log.i("pepe", "LB");
            MyLog.write("LB", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY))
        {
            Log.i("pepe", "Aviso bateria ok");
            //MyLog.write("Aviso bateria ok", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED))
        {
            Log.i("pepe", "Aviso locale changed");
            //MyLog.write("Aviso locale changed", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            //Log.i("pepe", "ScOff");
            MyLog.write("ScOFF", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            //Log.i("pepe", "ScON");
            MyLog.write("ScON", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            //Log.i("pepe", "UP");
            MyLog.write("UP", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW))
        {
            //Log.i("pepe", "SL");
            MyLog.write("SL", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_OK))
        {
            //Log.i("pepe", "SOK");
            MyLog.write("SOK", "Mediciones", true);
        }
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
        {
            int state = extras != null ? extras.getInt("state") : -1;//1=plug, 0=unplug
            String name = extras != null ? extras.getString("name") : context.getString(R.string.na).toUpperCase();
            int hasMic = extras != null ? extras.getInt("microphone") : -1;
            String sHasMic = context.getString(R.string.na);
            if(state == 1)
            {
                Log.i("pepe", "Enchufo headset");
                //MyLog.write("Enchufo headset", "Mediciones", false);
            }
            else if(state == 0)
            {
                Log.i("pepe", "Desenchufo headset");
                //MyLog.write("Desenchufo headset", "Mediciones", false);
            }

        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED))
        {
            Log.i("pepe", "Media scanner started");
            //MyLog.write("Media scanner started", "Mediciones", false);
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED))
        {
            Log.i("pepe", "Media scanner finished");
            //MyLog.write("Media scanner finished", "Mediciones", false);
        }
    }
}
