package ar.edu.caece.tesis.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ar.edu.caece.tesis.Services.EventService;

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
