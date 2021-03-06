package com.example.luokaixuan.android2droid201806050939.motionClick;

/**
 * Created by 90678 on 2017/7/24.
 */

import android.content.Context;

import com.example.luokaixuan.android2droid201806050939.Util.L;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Utils {
    private static Utils instance;
    private static Context mContext;

    private Utils(Context context) {
        super();
        // TODO Auto-generated constructor stub
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Utils getInstance(Context context) {
        mContext = context;
        if (instance == null) instance = new Utils(context);
        return instance;
    }

//    public void screenshot() {
//        exec("chmod 777 /dev/graphics/fb0 \n cat /dev/graphics/fb0 > /mnt/sdcard/fb0",
//                null);
//    }
//
//    public void sendClick(float x, float y) {
//        String[] orders = {
//                "sendevent /dev/input/event4 0 0 0",
//                "sendevent /dev/input/event4 1 330 1",
//                "sendevent /dev/input/event4 3 53 " + x,
//                "sendevent /dev/input/event4 3 54 " + y,
//                "sendevent /dev/input/event4 0 0 0",
//                "sendevent /dev/input/event4 1 330 0",
//                "sendevent /dev/input/event4 0 0 0",
//                "sendevent /dev/input/event4 0 0 0" };
//        exec("su", orders);
//    }
//
//    public void sendHome() {
//        String[] orders = {
//                "sendevent /dev/input/event1 0 0 0",
//                "sendevent /dev/input/event1 1 102 1",
//                "sendevent /dev/input/event1 0 0 0",
//                "sendevent /dev/input/event1 1 102 0",
//                "sendevent /dev/input/event1 0 0 0",
//                "sendevent /dev/input/event1 0 0 0" };
//        exec("su", orders);
//    }
//
//    private void exec(String cmd, String[] orders) {
//        try {
//            Process process = Runtime.getRuntime().exec(cmd);
//            DataOutputStream dataOut = new DataOutputStream(
//                    process.getOutputStream());
//            if (orders != null) {
//                for (String order : orders)
//                    dataOut.writeBytes(order + ";");
//            }
//            dataOut.flush();
//            dataOut.close();
//            process.waitFor();
//            InputStream in = process.getInputStream();
//            BufferedReader bufferReader = new BufferedReader(
//                    new InputStreamReader(in));
//            BufferedReader err = new BufferedReader(new InputStreamReader(
//                    process.getErrorStream()));
//            String line = null;
//            while ((line = err.readLine()) != null)
//                L.d("1.>>>" + line);
//            while ((line = bufferReader.readLine()) != null)
//                L.d("2.>>>" + line);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//    }

//    execShellCmd("getevent -p");
//    execShellCmd("sendevent /dev/input/event0 1 158 1");
//    execShellCmd("sendevent /dev/input/event0 1 158 0");
//    execShellCmd("input keyevent 3");//home
//    execShellCmd("input text  'helloworld!' ");
//    execShellCmd("input tap 168 252");
//    execShellCmd("input swipe 100 250 200 280");

    /**
     * ??????shell??????
     *
     * @param cmd
     */
//    http://blog.csdn.net/mad1989/article/details/38109689/
    public void execShellCmd(String cmd) {
        L.d("???????????? " + cmd);
        try {
            // ????????????root???????????????????????????????????????????????????
            Process process = Runtime.getRuntime().exec("su");
            // ???????????????
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void execShellCmd(int keyCode) {
        execShellCmd("input keyevent " + keyCode);
    }

    public void execShellClickCmd(float x, float y) {
//        execShellCmd("input tap 168 252");
        execShellCmd("input tap " + x + " " + y);
    }

    public void execShellSwipeCmd(float x1, float y1, float x2, float y2) {
//        execShellCmd("input swipe 100 250 200 280");
        execShellCmd("input swipe " + x1 + " " + y1 + " " + x2 + " " + y2);
    }

    public void execShellTouchCmd2(float x, float y) {
        execShellCmd("sendevent  /dev/input/event3 3 50 5 " + "sendevent  /dev/input/event3 3 53 "
                + x + "sendevent  /dev/input/event3 3 54 " + y);
    }

    public void execShellTouchCmd(float x, float y) {
//        execShellCmd("input touchscreen "+x+" "+y);
        execShellCmd("sendevent  /dev/input/event3 3 50 5 " + "sendevent  /dev/input/event3 3 53 "
                + x + "sendevent  /dev/input/event3 3 54 " + y + "sendevent  /dev/input/event3 0 "
                + "2 0 "//    ?????????????????????????????????
                + "sendevent  /dev/input/event3 0 0 0 " + "sendevent  /dev/input/event3 0 2 0 "); //    ??????????????????????????????

    }

//    http://www.jb51.net/article/88649.htm
//    http://blog.csdn.net/wzystal/article/details/26088987

    public void reboot() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"}); //??????
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() {
        try {
//Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","shutdown"}); //??????
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"}); //??????
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void reboot () {
//        PowerManager pManager=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE); 
// ?????????fastboot??????
//        pManager.reboot("");
//    }
//
//    public void shutDown () {
//        try {
////??????ServiceManager???
//            Class ServiceManager = Class
//                    .forName("android.os.ServiceManager");
////??????ServiceManager???getService??????
//            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
////??????getService??????RemoteService
//            Object oRemoteService = getService.invoke(null,Context.POWER_SERVICE);
////??????IPowerManager.Stub???
//            Class cStub = Class
//                    .forName("android.os.IPowerManager$Stub");
////??????asInterface??????
//            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
////??????asInterface????????????IPowerManager??????
//            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
////??????shutdown()??????
//            Method shutdown = oIPowerManager.getClass().getMethod("shutdown",boolean.class,
//            boolean.class);
////??????shutdown()??????
//            shutdown.invoke(oIPowerManager,false,true);
//        } catch (Exception e) {
//            Log.e(TAG, e.toString(), e);
//        }
//    }
}