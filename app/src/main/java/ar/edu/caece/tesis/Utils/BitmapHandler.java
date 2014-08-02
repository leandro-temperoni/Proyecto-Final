package ar.edu.caece.tesis.Utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by lea on 22/06/13.
 */
public class BitmapHandler {

    public static Drawable resize(Bitmap ori, int size){

        Bitmap bitmap = Bitmap.createScaledBitmap(ori, size, size, true);

        return new BitmapDrawable(bitmap);

    }

}
