package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientMainActivity1TcpSurface extends ScreenOnActivity implements SurfaceHolder.Callback {

    private static final String TAG = "ClientMainActivity";
    Socket mSocket;

    private ServerSocket mSS;

    private InputStream mIs;

    private SurfaceView mSv;

    MediaCodec mMediaDeCodec;

    ByteBuffer[] mInputBuffers, mOutputBuffers;

    TextView tvInputLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client);

        mSv = (SurfaceView) findViewById(R.id.mSv);

        tvInputLength = findViewById(R.id.inputLengthTv);

//        WindowManager wm1 = this.getWindowManager();
//        mScreenWidth = wm1.getDefaultDisplay().getWidth();
//        mScreenHeight = wm1.getDefaultDisplay().getHeight();

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

    int prestationTimeUs;

    //        boolean firstFrame = getFirstFrame();
    boolean render = true;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDataTcp() {

        while (true) {

            if (mIs != null) {
                try {

                    ByteBuffer inputBuffer = null;

                    mInputBuffers = mMediaDeCodec.getInputBuffers();
                    mOutputBuffers = mMediaDeCodec.getOutputBuffers();

                    int inputBufferIndex = mMediaDeCodec.dequeueInputBuffer(Constants.DECODE_TIMEOUT_USEC);
                    if (inputBufferIndex >= 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mMediaDeCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mInputBuffers[inputBufferIndex];
                        }

                        byte[] buff = new byte[mIs.available()];

                        int length = buff.length;

                        String msg1 = "接收长度 " + length;
                        Log.d(TAG, msg1);
                        Message msg = new Message();
                        msg.obj = msg1;
                        inputLengthHandler.sendMessage(msg);

                        int le = mIs.read(buff);

                        inputBuffer.clear();
                        inputBuffer.put(buff);

                        if (le == -1) {
                            continue;
                        }
                        mMediaDeCodec.queueInputBuffer(inputBufferIndex, 0, length,
                                prestationTimeUs, 0);

                        prestationTimeUs++;

//                        bytesToImageFile(buff);

                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                    int outputBufferIndex = 0;
                    try {
                        outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
                                Constants.DECODE_TIMEOUT_USEC);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    saveYUV2Local();
//                    render2Surface(outputBufferIndex);

                    Log.d(TAG, "outputBufferIndex " + outputBufferIndex);
                    switch (outputBufferIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.v(TAG, "format changed");
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.v(TAG, "解码当前帧超时");
                            break;
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            mOutputBuffers = mMediaDeCodec.getOutputBuffers();
                            Log.v(TAG, "output buffers changed");
                            break;
                        default:
                            //直接渲染到Surface时使用不到outputBuffer
//                            ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];
                            //延时操作
                            //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);
                            //渲染
//                            while (outputBufferIndex >= 0) {
//                                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
//                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer
//                                (bufferInfo, Constants.DECODE_TIMEOUT_USEC);
//                            }

//                            while (outputBufferIndex >= 0) {
                            mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, render);
                            render = !render;
//                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
//                                        Constants.DECODE_TIMEOUT_USEC);
//                            }
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void render2Surface(int outputBufferIndex) {

    }

    private void bytesToImageFile(byte[] bytes) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/aaa.jpeg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDecoder() {
        Log.d(TAG, "初始化解码器...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(mSv.getHolder().getSurface());
            mMediaDeCodec.start();

            Log.d(TAG, "解码器创建成功...");

        }
    }

    private void initSocket() {
        new ServerSocketThread().start();
    }

    private class ServerSocketThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                Log.d(TAG, "连接 Socket ...");

                mSS = new ServerSocket(Constants.PORT);

                mSocket = mSS.accept();
                Log.d(TAG, "Socket 创建成功...");
                mIs = mSocket.getInputStream();

                setDataTcp();

                initSocket();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        initSocket();

        initDecoder();

        final SurfaceHolder surfaceHolderORG = surfaceHolder;

        Log.d(TAG, "Surface Created");

//        new Thread() {
//
//            @Override
//            public void run() {
//
//
//                while (true) {
//
//                    if (mOutputBuffer == null) {
//                        Log.d(TAG, "mOutputBuffer == null ... continue ... ");
//                        continue;
//                    }
//                    //Added code
//                    byte[] bytes = new byte[mOutputBuffer.remaining()];
//                    int offset = 0, n = 0;
//
//                    InputStream in = new ByteArrayInputStream(bytes);
//
//                    try {
//
//                        while (offset < bytes.length && (n = in.read(bytes, offset,
//                                bytes.length - offset)) >= 0) {
//                            offset += n;
//
//                        }
//                        in.close();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//
//                    // getting byte array
//                    Bitmap bmp;
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inMutable = true;
//                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
//                    Canvas canvas = surfaceHolderORG.lockCanvas();
//                    if (canvas == null) {
//                        Log.d(TAG, "canvas == null");
//                    } else {
//                        canvas.drawBitmap(bmp, 0, 0, null);
//                        Log.d(TAG, "canvas.drawBitmap(bmp, 0, 0, null);");
//                        surfaceHolderORG.unlockCanvasAndPost(canvas);
//                    }
//                }
//            }
//        }.start();

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
//            // 竖屏16:9
//            height = (int) (mScreenWidth / SHOW_SCALE);
//            areaWH = SHOW_SCALE;
//        } else {
//            //横屏按照手机屏幕宽高计算比例
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
//            //直接放会矮胖
//            int svWidth = (int) (height * mediaWH);
//            layoutParamsSV = new RelativeLayout.LayoutParams(svWidth, height);
//            layoutParamsSV.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mSv.setLayoutParams(layoutParamsSV);
//        }
//
//        if (areaWH < mediaWH) {
//            //直接放会瘦高。
//            int svHeight = (int) (mScreenWidth / mediaWH);
//            layoutParamsSV = new RelativeLayout.LayoutParams(mScreenWidth, svHeight);
//            layoutParamsSV.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mSv.setLayoutParams(layoutParamsSV);
//        }
//
//    }
}
