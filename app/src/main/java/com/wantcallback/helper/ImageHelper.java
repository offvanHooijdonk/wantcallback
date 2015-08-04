package com.wantcallback.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.wantcallback.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by off on 01.08.2015.
 */
public class ImageHelper {
    private static final int[] PORTRAIT_COLORS = {R.color.pc_red, R.color.pc_pink, R.color.pc_purple,
            R.color.pc_deep_purple, R.color.pc_indigo, R.color.pc_blue,
            R.color.pc_cyan, R.color.pc_teal, R.color.pc_green, R.color.pc_emerald,
            R.color.pc_deep_orange, R.color.pc_orange, R.color.pc_blue_grey};
    private static final int DEFAULT_COLOR_INDEX = 5;

    private static List<Integer> colorsList;

    public static Bitmap convertTransparentToColor(Context ctx, int imageRes, int color) {
        Bitmap result;

        Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), imageRes);

        result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());  // Create another image the same size
        result.eraseColor(color);  // set its background to white, or whatever color you want
        Canvas canvas = new Canvas(result);  // create a canvas to draw on the new image
        canvas.drawBitmap(bitmap, 0f, 0f, null); // draw old image on the background
        bitmap.recycle();

        return result;
    }

    // TODO save this color to DB and do not call for each item
    public static int getMaterialColorForPhoneOrName(Context ctx, String phoneOrName) {
        int colorMaterial;
        if (phoneOrName != null) {
            int colorAssigned = phoneNumberToColor(phoneOrName);
            colorMaterial = findClosestMaterialColor(ctx, colorAssigned);
        } else {
            colorMaterial = getColorsList(ctx).get(DEFAULT_COLOR_INDEX);
        }
        return colorMaterial;
    }

    private static int findClosestMaterialColor(Context ctx, int phoneColor) {
        int r2 = (phoneColor >> 16) & 0xFF;
        int g2 = (phoneColor >> 8) & 0xFF;
        int b2 = phoneColor & 0xFF;

        int closestDist = -1;
        int closestColor = getColorsList(ctx).get(DEFAULT_COLOR_INDEX);
        for (int c : getColorsList(ctx)) {
            int r1 = (c >> 16) & 0xFF;
            int g1 = (c >> 8) & 0xFF;
            int b1 = c & 0xFF;

            int dist = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);

            if (closestDist < 0 || dist < closestDist) {
                closestDist = dist;
                closestColor = c;
            }
        }

        return closestColor;
    }

    private static int phoneNumberToColor(String phoneOrName) {
        return phoneOrName.hashCode() & 0xFFFFFF;
    }

    private static List<Integer> getColorsList(Context ctx) {
        if (colorsList == null) {
            colorsList = new ArrayList<>();
            for (int res : PORTRAIT_COLORS) {
                colorsList.add(ctx.getResources().getColor(res));
            }
        }

        return colorsList;
    }

}
