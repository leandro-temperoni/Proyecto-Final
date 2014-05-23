package com.caece.proyectofinal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.caece.proyectofinal.Utils.MyLog;

import java.util.Date;
import java.util.Locale;

public class EventReceiver extends BroadcastReceiver {

    public static void sendToReceiver(Event event)
    {
        Log.i("pepe", event.getDescription());
        MyLog.write(event.getDescription(), "Eventos");
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null /*|| intent.getExtras() == null*/)
        {
            return;
        }
        //TODO only if battery save isn't enabled
        context.startService(new Intent(context, EventService.class));
        Bundle extras = intent.getExtras();
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            /*networkinfo(NetworkInfo), bssid(string), linkProperties(LinkProperties)*/
            /*wifiInfo=SSID: WiredSSID, BSSID: 01:80:c2:00:00:03, MAC: 08:00:27:64:07:04, Supplicant state: COMPLETED, RSSI: -65, Link speed: 0, Net ID: 0, Metered hint: false*/
            NetworkInfo networkInfo = extras != null ? (NetworkInfo) extras.getParcelable("networkInfo") : null;
            if(networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED && networkInfo.getState() != NetworkInfo.State.DISCONNECTED) return;
            WifiInfo wifiInfo = extras.getParcelable("wifiInfo");
            if(networkInfo.getState() == NetworkInfo.State.CONNECTED && wifiInfo == null)return;

            if(networkInfo.isConnected())
            {
                Log.i("pepe", "Conectado a wifi");
                MyLog.write("Conectado a wifi", "Eventos");
            }
            else
            {
                Log.i("pepe", "Desconectado a wifi");
                MyLog.write("Desconectado a wifi", "Eventos");
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
                    Log.i("pepe", "Wifi Deshabilitado");
                    MyLog.write("Wifi Deshabilitado", "Eventos");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.i("pepe", "Wifi Habilitado");
                    MyLog.write("Wifi Habilitado", "Eventos");
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
                    Log.i("pepe", "B/T Deshabilitado");
                    MyLog.write("B/T Deshabilitado", "Eventos");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i("pepe", "B/T Habilitado");
                    MyLog.write("B/T Habilitado", "Eventos");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.i("pepe", "B/T Conectado");
                    MyLog.write("B/T Conectado", "Eventos");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.i("pepe", "B/T Desconectado");
                    MyLog.write("B/T Desconectado", "Eventos");
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
            Log.i("pepe", "Nuevo SMS recibido");
            MyLog.write("Nuevo SMS recibido", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))
        {
            Log.i("pepe", "Boot completed");
            MyLog.write("Boot completed", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
        {
            Log.i("pepe", "Se cambio la hora");
            MyLog.write("Se cambio la hora", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
        {
            Log.i("pepe", "Se cambio la fecha");
            MyLog.write("Se cambio la fecha", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
        {
            Log.i("pepe", "Se cambio la hora local");
            MyLog.write("Se cambio la hora local", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        {
            boolean isEnabled = extras != null && extras.getBoolean("state");
            if(isEnabled) {
                Log.i("pepe", "Modo avion activado");
                MyLog.write("Modo avion activado", "Eventos");
            }
            else { Log.i("pepe", "Modo avion desactivado"); MyLog.write("Modo avion desactivado", "Eventos"); }
        }
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsEnabled) {
                Log.i("pepe", "GPS activado");
                MyLog.write("GPS activado", "Eventos");
            }
            else { Log.i("pepe", "GPS desactivado"); MyLog.write("GPS desactivado", "Eventos"); }
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
            MyLog.write("Volume changed", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
        {
            /*android.intent.extra.PHONE_NUMBER=2222*/
            String phoneNum = extras != null ? extras.getString(Intent.EXTRA_PHONE_NUMBER) : context.getString(R.string.na).toUpperCase();
            Log.i("pepe", "Nueva outgoing call");
            MyLog.write("Nueva outgoing call", "Eventos");
        }
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        {
            if(extras != null && !TelephonyManager.EXTRA_STATE_RINGING.equals(extras.getString(TelephonyManager.EXTRA_STATE)))
            {
                return;
            }
            String phoneNum = extras != null ? extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : context.getString(R.string.na).toUpperCase();

            Log.i("pepe", "Estado del telefono cambio");
            MyLog.write("Estado del telefono cambio", "Eventos");

        }
        if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
        {
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if(landscape) {
                Log.i("pepe", "Landscape");
                MyLog.write("Orientación landscape", "Eventos");
            }
            else { Log.i("pepe", "portrait"); MyLog.write("Orientación portrait", "Eventos"); }
        }
        if(intent.getAction().equals(Intent.ACTION_REBOOT))
        {
            Log.i("pepe", "Reboot");
            MyLog.write("Reboot", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN))
        {
            Log.i("pepe", "Shutdown");
            MyLog.write("Shutdown", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            Log.i("pepe", "Power connected");
            MyLog.write("Power connected", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            Log.i("pepe", "Power disconnected");
            MyLog.write("Power disconnected", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED) && false)//TODO add option to log all battery options
        {
            if(extras != null)
            {
                int state = extras.getInt(BatteryManager.EXTRA_STATUS);
                switch (state)
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        Log.i("pepe", "Cargando bateria");
                        MyLog.write("Cargando bateria", "Eventos");
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        Log.i("pepe", "Descargandose le bateria");
                        MyLog.write("Descargandose le bateria", "Eventos");
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        Log.i("pepe", "Esta llena la bateria");
                        MyLog.write("Esta llena la bateria", "Eventos");
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        Log.i("pepe", "No se esta cargando");
                        MyLog.write("No se esta cargando", "Eventos");
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        Log.i("pepe", "No se sabe el estado de la bateria");
                        MyLog.write("No se sabe el estado de la bateria", "Eventos");
                        break;
                }
                int level = extras.getInt(BatteryManager.EXTRA_LEVEL);
                int health = extras.getInt(BatteryManager.EXTRA_HEALTH);
                switch (health)
                {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        Log.i("pepe", "BATTERY_HEALTH_COLD");
                        MyLog.write("BATTERY_HEALTH_COLD", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        Log.i("pepe", "BATTERY_HEALTH_DEAD");
                        MyLog.write("BATTERY_HEALTH_DEAD", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        Log.i("pepe", "BATTERY_HEALTH_GOOD");
                        MyLog.write("BATTERY_HEALTH_GOOD", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        Log.i("pepe", "BATTERY_HEALTH_OVER_VOLTAGE");
                        MyLog.write("BATTERY_HEALTH_OVER_VOLTAGE", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        Log.i("pepe", "BATTERY_HEALTH_OVERHEAT");
                        MyLog.write("BATTERY_HEALTH_OVERHEAT", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        Log.i("pepe", "BATTERY_HEALTH_UNKNOWN");
                        MyLog.write("BATTERY_HEALTH_UNKNOWN", "Eventos");
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        Log.i("pepe", "BATTERY_HEALTH_UNSPECIFIED_FAILURE");
                        MyLog.write("BATTERY_HEALTH_UNSPECIFIED_FAILURE", "Eventos");
                        break;
                }

            }

        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            Log.i("pepe", "Aviso bateria baja");
            MyLog.write("Aviso bateria baja", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY))
        {
            Log.i("pepe", "Aviso bateria ok");
            MyLog.write("Aviso bateria ok", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED))
        {
            Log.i("pepe", "Aviso locale changed");
            MyLog.write("Aviso locale changed", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Log.i("pepe", "Screen off");
            MyLog.write("Screen off", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Log.i("pepe", "Screen on");
            MyLog.write("Screen on", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Log.i("pepe", "User present");
            MyLog.write("User present", "Eventos");
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
                MyLog.write("Enchufo headset", "Eventos");
            }
            else if(state == 0)
            {
                Log.i("pepe", "Desenchufo headset");
                MyLog.write("Desenchufo headset", "Eventos");
            }

        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED))
        {
            Log.i("pepe", "Media scanner started");
            MyLog.write("Media scanner started", "Eventos");
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED))
        {
            Log.i("pepe", "Media scanner finished");
            MyLog.write("Media scanner finished", "Eventos");
        }
    }
}