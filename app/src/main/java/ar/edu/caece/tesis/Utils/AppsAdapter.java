package ar.edu.caece.tesis.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ar.edu.caece.tesis.R;

/**
 * Created by lea on 18/06/13.
 */
public class AppsAdapter extends BaseAdapter {

    ArrayList<ApplicationInfo> apps;
    LayoutInflater inflater;
    ViewHolder holder;
    PackageManager mPackageManager;

    public AppsAdapter(Activity activity, ArrayList<ApplicationInfo> apps){

        this.apps = apps;

        mPackageManager = activity.getPackageManager();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View vi=convertView;
        if(convertView==null){

            vi = inflater.inflate(R.layout.list_row_apps, null);
            holder = new ViewHolder();

            holder.nombre = (TextView)vi.findViewById(R.id.nombreapp);

            holder.tipo =(ImageView)vi.findViewById(R.id.imagenapp);

            vi.setTag(holder);
        }
        else{

            holder = (ViewHolder)vi.getTag();
        }

        holder.nombre.setText(apps.get(position).loadLabel(mPackageManager).toString());

        holder.tipo.setImageDrawable(apps.get(position).loadIcon(mPackageManager));

        return vi;

    }

    static class ViewHolder{

        TextView nombre;
        ImageView tipo;

    }

}
