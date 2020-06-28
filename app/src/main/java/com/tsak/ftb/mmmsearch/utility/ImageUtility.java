package com.tsak.ftb.mmmsearch.utility;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtility {

    private ImageUtility() {
        throw new AssertionError();
    }

    public static Drawable resize(Resources resources, Drawable drawable, int maxWidth, int maxHeight) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        int resizedWidth = 0;
        int resizedHeight = 0;

        if (maxWidth >= bitmap.getWidth() && maxHeight >= bitmap.getHeight()) {
            return new BitmapDrawable(resources, bitmap);
        }
        if (bitmap.getWidth() > bitmap.getHeight()) {
            resizedWidth = maxWidth;
            resizedHeight = resizedWidth * bitmap.getHeight() / bitmap.getWidth();
        } else {
            resizedHeight = maxHeight;
            resizedWidth = resizedHeight * bitmap.getWidth() / bitmap.getHeight();
        }
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
        return new BitmapDrawable(resources, resized);
    }
}
