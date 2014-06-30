package com.caece.proyectofinal.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

/**
 * Created by lea on 22/06/13.
 */
public class BitmapHandler {

    public static Drawable resize(Bitmap ori, int size){

        Bitmap bitmap = Bitmap.createScaledBitmap(ori, size, size, true);

        return new BitmapDrawable(bitmap);

    }

}
