package ar.edu.caece.tesis.Services;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import ar.edu.caece.tesis.Utils.MyLog;

/**
 * Created by COCO on 06/06/2014.
 */
public class SMSObserver extends ContentObserver {

    private static final int MESSAGE_TYPE_SENT = 2;
    private static final String CONTENT_SMS = "content://sms";

    Context context;

    public SMSObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Uri uriSMSURI = Uri.parse(CONTENT_SMS);
        Cursor cur = context.getContentResolver().query(uriSMSURI, null, null, null, null);
        if(cur != null){
            cur.moveToNext();

            int type = cur.getInt(cur.getColumnIndex("type"));
            if (isOutgoingMessage(type)) {

                MyLog.write("SMSS", "Mediciones", true);

            }
        }
    }

    private boolean isOutgoingMessage(int type) {
        return type == MESSAGE_TYPE_SENT;
    }

}
