package com.example.luokaixuan.android2droid201806050939.constants;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;
import android.view.WindowManager;

import com.example.luokaixuan.android2droid201806050939.MyApplication;
import com.example.luokaixuan.android2droid201806050939.Util.ScreenUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.example.luokaixuan.android2droid201806050939.constants.Constants.FPS;
import static com.example.luokaixuan.android2droid201806050939.constants.Constants.KEY_I_FRAME_INTERVAL;

/**
 * Created by 90678 on 2017/8/1.
 */

public class DeEncodecCommon {

    public static final int ENCODE = 1;
    public static final int DECODE = 2;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static MediaFormat getPhoneFormat(int deCodec) {
        return initCommonFormat(deCodec,
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                        getPhoneWidth(), getPhoneHeight()));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static MediaFormat getCarPadFormat(int deCodec) {
        return initCommonFormat(deCodec,
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                        getPhoneWidth()/2, getPhoneHeight()/2));
    }

    private static boolean isUsePpsAndSps = false;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static MediaFormat initCommonFormat(int deCodec, MediaFormat mMediaFormat) {
        //获取h264中的pps及sps数据
//        if (isUsePpsAndSps) {
//            byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0,
//                    (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
//            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6,
//                    (byte) 229, 1, (byte) 151, (byte) 128};
//            mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//            mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
//        }
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constants.BITRATE);
        // 调整码率的控流模式
        //动态调整码率。
//        其实对于码率有三种模式可以控制：
//        BITRATE_MODE_CQ
//        表示不控制码率，尽最大可能保证图像质量
//                BITRATE_MODE_VBR
//        表示 MediaCodec 会根据图像内容的复杂度来动态调整输出码率，图像负责则码率高，图像简单则码率低
//                BITRATE_MODE_CBR
//        表示 MediaCodec 会把输出的码率控制为设定的大小
//        mMediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL);
        switch (deCodec) {
            case ENCODE:
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                break;
            case DECODE:
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                break;
            default:
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                break;
        }
        return mMediaFormat;
    }

    public static MediaCodec getMediaEnCodec() {
        MediaCodec encoder = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                encoder.configure(getPhoneFormat(ENCODE), null, null,
                        MediaCodec.CONFIGURE_FLAG_ENCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encoder;
    }

    public static MediaCodec getMediaDeCodec(Surface surface, MediaFormat mediaFormat) {
        MediaCodec mMediaCodec = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                mMediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

                mMediaCodec.configure(mediaFormat, surface, null, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mMediaCodec;
    }

    public static MediaCodec getMediaDeCodec(Surface surface) {
        MediaCodec mMediaCodec = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                mMediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

                mMediaCodec.configure(getCarPadFormat(DECODE), surface, null, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mMediaCodec;
    }

    public static WindowManager getWindowManager(Activity activity) {
        return activity.getWindowManager();
    }

    public static int getPhoneWidth() {
//        return getWindowManager(activity).getDefaultDisplay().getWidth() / 3;
        return Constants.PHONE_WIDTH;
//        return ScreenUtil.getRealScreenWidth(MyApplication.getInstance().getCurrentActivity());
    }

    public static int getPhoneHeight() {
//        return getWindowManager(activity).getDefaultDisplay().getHeight() / 3;
        return Constants.PHONE_HEIGHT;
//        return ScreenUtil.getRealScreenHeight(MyApplication.getInstance().getCurrentActivity());
    }
}
