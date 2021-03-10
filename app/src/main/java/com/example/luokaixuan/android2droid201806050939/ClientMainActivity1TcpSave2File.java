package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.luokaixuan.android2droid201806050939.Util.FileUtil;
import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientMainActivity1TcpSave2File extends ScreenOnActivity implements SurfaceHolder.Callback {

    private static final String TAG = "ClientMainActivity";

    Socket mSocket;

    private ServerSocket mSS;

    //    private OutputStream mOs;
    private InputStream mIs;

    private SurfaceView mSv;

    MediaCodec mMediaDeCodec;

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
    private ByteBuffer mOutputBuffer;

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

    long decodeStartTime;
    long decodeEndTime;
    int outputBufferIndex = 0;

    //    MediaCodec.BufferInfo bufferInfo;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDataTcp() {

        while (true) {
            if (mIs != null) {
                try {

                    ByteBuffer inputBuffer = null;

                    int inputBufferIndex =
                            mMediaDeCodec.dequeueInputBuffer(Constants.DECODE_TIMEOUT_USEC);
                    if (inputBufferIndex >= 0) {
//                        inputBuffer = mMediaDeCodec.getInputBuffer(inputBufferIndex);
                        inputBuffer = mInputBuffers[inputBufferIndex];

                        byte[] buff = new byte[mIs.available()];

                        int length = buff.length;
                        if (length > 0) {
                            String msg1 = "接收长度 " + length;
                            Log.d(TAG, msg1);
                            Message msg = new Message();
                            msg.obj = msg1;
                            inputLengthHandler.sendMessage(msg);
                        }

                        int le = mIs.read(buff);

                        if (le == -1) {
                            continue;
                        }

                        inputBuffer.clear();
                        inputBuffer.put(buff);
//                        fileUtil.saveH264DataToFile(buff);
                        mMediaDeCodec.queueInputBuffer(inputBufferIndex, 0, length,
                                Constants.DECODE_TIMEOUT_USEC, 0);
//                        bytesToImageFile(buff);

                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                    try {
                        outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
                                Constants.DECODE_TIMEOUT_USEC);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    saveYUV2Local(bufferInfo);
//                    render2Surface();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //        boolean firstFrame = getFirstFrame();
    boolean render = true;

    FileUtil fileUtil = new FileUtil().init();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void saveYUV2Local(MediaCodec.BufferInfo bufferInfo) {

        //渲染
        if (outputBufferIndex >= 0) {

            //直接渲染到Surface时使用不到outputBuffer
            decodeStartTime = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mOutputBuffer = mMediaDeCodec.getOutputBuffer(outputBufferIndex);
            } else {
                mOutputBuffer = mMediaDeCodec.getOutputBuffers()[outputBufferIndex];
            }

            if (mOutputBuffer == null) {
                Log.d(TAG, "mOutputBuffer == null ... ");
            } else {
                Log.d(TAG, "mOutputBuffer != null ... ");
                final byte[] b = new byte[bufferInfo.size];
                try {
                    if (bufferInfo.size != 0) {
                        mOutputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        mOutputBuffer.position(bufferInfo.offset);
                        mOutputBuffer.get(b, bufferInfo.offset,
                                bufferInfo.offset + bufferInfo.size);
                    }
                } catch (Exception e) {

                }
                fileUtil.saveH264DataToFile(b);
                Log.d(TAG, "mOutputBuffer saveH264DataToFile  ... ");
            }

            //延时操作
            //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);

            mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
                    Constants.DECODE_TIMEOUT_USEC);
            decodeEndTime = System.currentTimeMillis();
            Log.d(TAG, "解码时间 " + (decodeEndTime - decodeStartTime));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void postYUV2To(MediaCodec.BufferInfo bufferInfo) {

        //渲染
        if (outputBufferIndex >= 0) {

            //直接渲染到Surface时使用不到outputBuffer
            decodeStartTime = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mOutputBuffer = mMediaDeCodec.getOutputBuffer(outputBufferIndex);
            } else {
                mOutputBuffer = mMediaDeCodec.getOutputBuffers()[outputBufferIndex];
            }

            if (mOutputBuffer == null) {
                Log.d(TAG, "mOutputBuffer == null ... ");
            } else {
                Log.d(TAG, "mOutputBuffer != null ... ");
                final byte[] b = new byte[bufferInfo.size];
                try {
                    if (bufferInfo.size != 0) {
                        mOutputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        mOutputBuffer.position(bufferInfo.offset);
                        mOutputBuffer.get(b, bufferInfo.offset,
                                bufferInfo.offset + bufferInfo.size);
                    }
                } catch (Exception e) {

                }
                fileUtil.saveH264DataToFile(b);
                Log.d(TAG, "mOutputBuffer saveH264DataToFile  ... ");
            }

            //延时操作
            //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);

            mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
                    Constants.DECODE_TIMEOUT_USEC);
            decodeEndTime = System.currentTimeMillis();
            Log.d(TAG, "解码时间 " + (decodeEndTime - decodeStartTime));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void render2Surface() {
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
//                while (outputBufferIndex >= 0) {
                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, false);
                render = !render;
//                    outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo,
//                            Constants.DECODE_TIMEOUT_USEC);
//                }
                break;
        }
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

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private boolean getFirstFrame() {
//        try {
//            byte[] spspps = new byte[mIs.available()];
//
//            int length = spspps.length;
//            if (length > 0) {
//                Log.d(TAG, "接收第一帧长度 " + length);
//            }
//
//            int le = mIs.read(spspps);
//
//            //找到sps与pps的分隔处
//            int pos = 0;
//            if (!((pos + 3 < spspps.length) && (spspps[pos] == 0 && spspps[pos + 1] == 0 &&
//            spspps[pos + 2] == 0 && spspps[pos + 3] == 1))) {
////                return false;
//            } else {
//                //00 00 00 01开始标志后的一位
//                pos = 4;
//            }
//            while ((pos + 3 < spspps.length) && !(spspps[pos] == 0 && spspps[pos + 1] == 0 &&
//            spspps[pos + 2] == 0 && spspps[pos + 3] == 1)) {
//                pos++;
//            }
//            if (pos + 3 >= spspps.length) {
////                return false;
//            }
//
//            mSps = Arrays.copyOfRange(spspps, 0, pos);
//            mPps = Arrays.copyOfRange(spspps, pos, spspps.length);
//
//            firstFrameHandler.sendEmptyMessage(1);
//
////            try {
////                mMediaDeCodec = MediaCodec.createDecoderByType("video/avc");
////
////                mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(mSv.getHolder().getSurface(),
// format);
////
////                mMediaDeCodec.start();
////
////                mInputBuffers = mMediaDeCodec.getInputBuffers();
////                mOutputBuffers = mMediaDeCodec.getOutputBuffers();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////
////            return true;
////
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }

//    Handler firstFrameHandler = new Handler() {
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            MediaFormat format = DeEncodecCommon.getPhoneFormat();
//
//            format.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
//            format.setByteBuffer("csd-1", ByteBuffer.wrap(mPps));
//
//            mMediaDeCodec.configure(format, mSv.getHolder().getSurface(),
//                    null, 0);
//        }
//    };

    private void initDecoder() {
        Log.d(TAG, "初始化解码器...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(mSv.getHolder().getSurface());
            mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(null);
            mMediaDeCodec.start();

            mInputBuffers = mMediaDeCodec.getInputBuffers();
            mOutputBuffers = mMediaDeCodec.getOutputBuffers();

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
        initDecoder();
        initSocket();

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
        if (mSocket != null) {
            try {
                mSocket.shutdownInput();
                mSocket.shutdownOutput();
                mSocket.close();
                mSS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
