package ar.edu.caece.tesis.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import ar.edu.caece.tesis.R;

/**
 * Created by COCO on 27/05/2014.
 */
public class Notificacion {

    public static void mostrar(Context context, String title, String text) {

        if(Preferencias.notificacionesHabilitadas(context)) {

            PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder;
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(text);

            //Si quiero que vibre
            if(Preferencias.vibracionHabilitada(context))
                mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

            //Si quiero que tenga funcionalidad
            /*Intent resultIntent = new Intent(this, ResultActivity.class);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);*/

            //Si quiero que suene al notificar
            Uri alarmSound = Preferencias.getRingtone(context);
            if (alarmSound != null)
                mBuilder.setSound(alarmSound);

            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

        }

    }

}
