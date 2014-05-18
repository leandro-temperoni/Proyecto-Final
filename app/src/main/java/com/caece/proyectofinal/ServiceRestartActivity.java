package com.caece.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by pedja on 14.4.14..
 */
public class ServiceRestartActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //moveTaskToBack(true);
        super.onCreate(savedInstanceState);
        startService(new Intent(this, EventService.class));
        finish();
    }
}
