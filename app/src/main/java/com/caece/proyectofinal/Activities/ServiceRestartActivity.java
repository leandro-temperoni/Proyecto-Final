package com.caece.proyectofinal.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.caece.proyectofinal.Services.EventService;

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
