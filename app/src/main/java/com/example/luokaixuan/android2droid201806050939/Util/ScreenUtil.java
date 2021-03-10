package com.example.luokaixuan.android2droid201806050939.Util;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Method;

/**
 * @author created by luokaixuan
 * @date 2019/6/21
 * 这个类是用来干嘛的
 */
public class ScreenUtil {

    private static final String TAG = ScreenUtil.class.getSimpleName();

    public static Point getRealScreenMetrics(Activity context) {
        int realWidth = 0, realHeight = 0;
        try {
            Display display = context.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1
                    && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, " realWidth, realHeight " + realWidth + " " + realHeight);
        return new Point(realWidth, realHeight);
    }

    public static int getRealScreenWidth(Activity context) {
        return getRealScreenMetrics(context).x;
    }

    public static int getRealScreenHeight(Activity context) {
        return getRealScreenMetrics(context).y;
    }
}
