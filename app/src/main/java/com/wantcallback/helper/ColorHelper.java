package com.wantcallback.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;

import com.android.contacts.common.util.MaterialColorMapUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by off on 01.08.2015.
 */
public class ColorHelper {

    private static List<Integer> colorsList;

    private static MaterialColorMapUtils utils;

    public static MaterialColorMapUtils.MaterialPalette getMaterialColorForPhoneOrName(Context ctx, String phoneOrName) {
        MaterialColorMapUtils.MaterialPalette palette;
        if (phoneOrName != null) {
            int colorAssigned = getMaterialColorsUtil(ctx).pickColorOnIdentifier(phoneOrName);
            palette = getMaterialColorsUtil(ctx).calculatePrimaryAndSecondaryColor(colorAssigned);
        } else {
            palette = MaterialColorMapUtils.getDefaultPrimaryAndSecondaryColors(ctx.getResources());
        }
        return palette;
    }

    private static Integer getColorOnImage(Context ctx, Uri uri) {
        Integer color = null;
        if (uri != null) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
            } catch (IOException e) {
                color = null;
            }
            if (bitmap != null) {
                Palette palette;
                try {
                    palette = new Palette.Builder(bitmap).generate();
                } finally {
                    bitmap.recycle();
                }

                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    color = swatch.getRgb();
                } else {
                    color = 0;
                }
            }
        }

        return color;
    }

    public static MaterialColorMapUtils.MaterialPalette getPaletteOnColor(Context ctx, int color) {
        return getMaterialColorsUtil(ctx).calculatePrimaryAndSecondaryColor(color);
    }

    private static MaterialColorMapUtils getMaterialColorsUtil(Context ctx) {
        if (utils == null) {
            utils = new MaterialColorMapUtils(ctx.getResources());
        }

        return utils;
    }

    public static MaterialColorMapUtils.MaterialPalette getMaterialPalette(Context ctx, Uri uri) {
        int mainColor = getColorOnImage(ctx, uri);

        return getMaterialColorsUtil(ctx).calculatePrimaryAndSecondaryColor(mainColor);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }
}
