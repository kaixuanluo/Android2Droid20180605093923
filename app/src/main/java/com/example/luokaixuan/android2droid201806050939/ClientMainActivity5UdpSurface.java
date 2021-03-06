package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class ClientMainActivity5UdpSurface extends BaseUdpReceiveActivity implements SurfaceHolder.Callback {

    private static final String TAG = "ClientMainActivity";
    DatagramSocket mSocket;
    //    private OutputStream mOs;
    private InputStream mIs;

    private SurfaceView mSv;

    MediaCodec mMediaDeCodec;
    MediaCodec.BufferInfo mBufferInfo;

    MediaFormat mMediaFormat;

    ByteBuffer[] mInputBuffers;
    int mInputBufferIndex;
    private MediaExtractor mMediaExtractor;
    private ByteBuffer[] mOutputBuffers;
    private byte[] mSps;
    private byte[] mPps;

    TextView tvInputLength;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mLength;
    private DatagramPacket mDp;
    private byte[] mBuff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client);

        mSv = (SurfaceView) findViewById(R.id.mSv);

        tvInputLength = findViewById(R.id.inputLengthTv);

        WindowManager wm1 = this.getWindowManager();
        mScreenWidth = wm1.getDefaultDisplay().getWidth();
        mScreenHeight = wm1.getDefaultDisplay().getHeight();

//        mSv.setLayoutParams(new FrameLayout.LayoutParams((mScreenWidth / 35) * 9, FrameLayout
//        .LayoutParams.MATCH_PARENT));

        mSv.getHolder().addCallback(this);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(5000);
//                        orientationChangeHandler.sendEmptyMessage(0);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    Handler inputLengthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvInputLength.setText(msg.obj.toString());
        }
    };

//    Handler orientationChangeHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            changeOrientation();
//        }
//    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setData5() {

//        boolean firstFrame = getFirstFrame();

        boolean render = true;
        while (true) {
            if (mSocket != null) {
                try {

                    ByteBuffer inputBuffer = null;

                    int inputBufferIndex =
                            mMediaDeCodec.dequeueInputBuffer(Constants.DECODE_TIMEOUT_USEC);
                    if (inputBufferIndex >= 0) {
//                        inputBuffer = mMediaDeCodec.getInputBuffer(inputBufferIndex);
                        inputBuffer = mInputBuffers[inputBufferIndex];

                        mBuff = new byte[mSocket.getReceiveBufferSize()];
                        mDp = new DatagramPacket(mBuff, mBuff.length);

                        Log.d(TAG, "mSocket preper ... ");
                        mSocket.receive(mDp);
                        Log.d(TAG, "mSocket receive  ... ");

                        mLength = mDp.getLength();
                        mBuff = mDp.getData();

//                        if (mLength > 0) {
//                            String msg1 = "???????????? " + mLength;
//                            Log.d(TAG, msg1);
//                            Message msg = new Message();
//                            msg.obj = msg1;
////                            inputLengthHandler.sendMessage(msg);
//                        }

                        inputBuffer.clear();
                        inputBuffer.put(mBuff);

                        if (mLength == -1) {
                            continue;
                        }
                        mMediaDeCodec.queueInputBuffer(inputBufferIndex, 0, mLength,
                                Constants.DECODE_TIMEOUT_USEC, 0);
//                        bytesToImageFile(buff);

                    }

                    int outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(mBufferInfo,
                            Constants.DECODE_TIMEOUT_USEC);
                    switch (outputBufferIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.v(TAG, "format changed");
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.v(TAG, "?????????????????????");
                            break;
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            mOutputBuffers = mMediaDeCodec.getOutputBuffers();
                            Log.v(TAG, "output buffers changed");
                            break;
                        default:
                            //???????????????Surface???????????????outputBuffer
//                            ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];
//                            outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
//                            byte[] yuvData = new byte[outputBuffer.remaining()];
//                            outputBuffer.get(yuvData); //????????????????????????yuvData ???????????????

                            //????????????
                            //????????????????????????????????????>?????????????????????????????????????????????
//                        sleepRender(videoBufferInfo, startMs);
                            //??????
                            while (outputBufferIndex >= 0) {
                                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, render);
//                            render = !render;
                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(mBufferInfo
                                        , Constants.DECODE_TIMEOUT_USEC);
                            }
                            break;
                    }

                    if (inputBuffer != null) {
                        inputBuffer.clear();
                    }
                    mBuff = null;
//                    mMediaDeCodec.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initDecoder() {
        Log.d(TAG, "??????????????????...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            mBufferInfo = new MediaCodec.BufferInfo();
            mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(mSv.getHolder().getSurface());
            mMediaDeCodec.start();

            mInputBuffers = mMediaDeCodec.getInputBuffers();
            mOutputBuffers = mMediaDeCodec.getOutputBuffers();

            Log.d(TAG, "?????????????????????...");

        }
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "?????? Socket ...");
                    mSocket = new DatagramSocket(Constants.PORT);
                    Log.d(TAG, "Socket ???????????????...");

                    setData5();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initDecoder();
        initSocket();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

//    private boolean isLand;
//
//    public static final float SHOW_SCALE = 16 * 1.0f / 9;
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        isLand = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
//        WindowManager wm1 = this.getWindowManager();
//        mScreenWidth = wm1.getDefaultDisplay().getWidth();
//        mScreenHeight = wm1.getDefaultDisplay().getHeight();
//
//        resetSize();
//    }
//
//    public void changeOrientation() {
//        if (Configuration.ORIENTATION_LANDSCAPE == this.getResources()
//                .getConfiguration().orientation) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//
//    }
//
//    private void resetSize() {
//
//        float areaWH = 0.0f;
//        int height;
//
//        if (!isLand) {
//            // ??????16:9
//            height = (int) (mScreenWidth / SHOW_SCALE);
//            areaWH = SHOW_SCALE;
//        } else {
//            //??????????????????????????????????????????
//            height = mScreenHeight;
//            areaWH = mScreenWidth / mScreenHeight;
//        }
//
//        RelativeLayout.LayoutParams layoutParams =
//                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//        mSv.setLayoutParams(layoutParams);
//
//        int mediaWidth = Constants.PHONE_WIDTH;
//        int mediaHeight = Constants.PHONE_HEIGHT;
//
//        float mediaWH = mediaWidth * 1.0f / mediaHeight;
//
//        RelativeLayout.LayoutParams layoutParamsSV = null;
//
//
//        if (areaWH > mediaWH) {
//            //??????????????????
//            int svWidth = (int) (height * mediaWH);
//            layoutParamsSV = new RelativeLayout.LayoutParams(svWidth, height);
//            layoutParamsSV.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mSv.setLayoutParams(layoutParamsSV);
//        }
//
//        if (areaWH < mediaWH) {
//            //?????????????????????
//            int svHeight = (int) (mScreenWidth / mediaWH);
//            layoutParamsSV = new RelativeLayout.LayoutParams(mScreenWidth, svHeight);
//            layoutParamsSV.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mSv.setLayoutParams(layoutParamsSV);
//        }
//
//    }
}
