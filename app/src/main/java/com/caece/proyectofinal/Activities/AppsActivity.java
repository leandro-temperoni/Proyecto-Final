package com.caece.proyectofinal.Activities;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.caece.proyectofinal.R;
import com.caece.proyectofinal.Utils.AppsAdapter;
import com.caece.proyectofinal.Utils.OSOperations;

import java.util.ArrayList;

public class AppsActivity extends SherlockActivity {

    ListView lista;
    ArrayList<ApplicationInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Aplicaciones");

        lista = (ListView) findViewById(R.id.listaApps);

        apps = OSOperations.getInstalledApps(this, 0);

        lista.setAdapter(new AppsAdapter(this, apps));

        registerForContextMenu(lista);

    }

    private void abrirDetalles(int i){

        OSOperations.abrirDetallesApp(this, apps.get(i).packageName);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.apps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:{
                finish();
                break;
            }

        }

        return super.onOptionsItemSelected(item);

    }

    private void recargarLista(){

        apps = OSOperations.getInstalledApps(this, 0);

        lista.setAdapter(new AppsAdapter(this, apps));

    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarLista();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pop_up_apps, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.borrar:
                OSOperations.desinstalarPaquete(this, apps.get(info.position).packageName);
                return true;
            case R.id.detalles:
                OSOperations.abrirDetallesApp(this, apps.get(info.position).packageName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}
