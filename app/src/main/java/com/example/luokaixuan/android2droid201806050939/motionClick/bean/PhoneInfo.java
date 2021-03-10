package com.example.luokaixuan.android2droid201806050939.motionClick.bean;

import android.content.Context;

public class PhoneInfo {

    public static String getImei(Context context) {
//        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        return "";
    }

    public static String getPhoneName() {
        return android.os.Build.MODEL;
    }

    public static String getVersionName() {
        return android.os.Build.VERSION.RELEASE ;
    }
}
