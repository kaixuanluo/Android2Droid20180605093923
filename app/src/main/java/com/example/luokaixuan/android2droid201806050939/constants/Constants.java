package com.example.luokaixuan.android2droid201806050939.constants;

/**
 * Created by 90678 on 2017/7/30.
 */

public class Constants {

    public static final int PHONE_WIDTH = (int) (1080 / 4);

    public static final int PHONE_HEIGHT = (int) (2400 / 4);

    public static final int CAR_PAD_WIDTH = PHONE_WIDTH;

    public static final int CAR_PAD_HEIGHT = PHONE_HEIGHT;

    public static final int FPS = 30;
    //    public static final int BITRATE = 1024000 * 10;
//    1280 x 720 (总像素) x 12bit(每个像素占用12bit) x 25(一秒25帧) = 276480000 bits = 32.96  x 1024 x 1024 x8
    public static final int COLOR_BIT = 12;
    public static final int BITRATE = (int) (PHONE_WIDTH * PHONE_HEIGHT) * COLOR_BIT * FPS;

    public static final int KEY_I_FRAME_INTERVAL = 1;

    public static final int DPI = PHONE_HEIGHT / PHONE_WIDTH;

    public static final int PORT = 9005;

    public static final int PORT_CONTROL = 9006;

    public static final int PORT_HAND_SHARE = 9007;

    public static final int DECODE_TIMEOUT_USEC = 500 * 1000;

    public static final int ENCODE_TIMEOUT_USEC = DECODE_TIMEOUT_USEC;

}


//花屏需要这样解决。
//                            while (outputBufferIndex >= 0) {
//                            mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
//                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer
//                                (mBufferInfo, Constants.DECODE_TIMEOUT_USEC);
//                            }

//滑动模糊，需要很久才好，是比特率设置太低了。
