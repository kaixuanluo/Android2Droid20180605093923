package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;
import com.example.luokaixuan.android2droid201806050939.gl.VideoGLSurfaceView;
import com.example.luokaixuan.android2droid201806050939.gl.VideoRender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class ClientMainActivity9UdpOpenGLEs extends BaseUdpReceiveActivity {

    private static final String TAG = "ClientMainActivity";
    DatagramSocket mSocket;
    //    private OutputStream mOs;
    private InputStream mIs;

    private VideoGLSurfaceView mTtv;

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
    private int mLength;
    private DatagramPacket mDp;
    private byte[] mBuff;
    MediaCodec.BufferInfo mBufferInfo;

    private SurfaceTexture mSt;
    private EGLContext mEglContext;
    private Handler mDecodeHandler;
    private Handler mReleaseHandler;
    private Handler mSocketHandler;

    private boolean mRender = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client9);

        mTtv = (VideoGLSurfaceView) findViewById(R.id.mTtv);

        tvInputLength = findViewById(R.id.inputLengthTv);

        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        int height1 = wm1.getDefaultDisplay().getHeight();

//        mTtv.setLayoutParams(new FrameLayout.LayoutParams((width1 / 35) * 9, height1));

//        mTtv.getHolder().addCallback(this);

        initSocketHandler();

        initDecoder();

        initDecodeHandler();

        initReleaseHandler();


    }

    private void initSocketHandler() {
        HandlerThread socketHandlerThread = new HandlerThread("socketHandlerThread");
        socketHandlerThread.start();
        mSocketHandler = new Handler(socketHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        };
    }

    private void initDecodeHandler() {
        HandlerThread decodeHandlerThread = new HandlerThread("decodeHandlerThread");
        decodeHandlerThread.start();
        mDecodeHandler = new Handler(decodeHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                byte[] mBuff = (byte[]) msg.obj;
                decode(mBuff);
            }
        };
    }

    private void initReleaseHandler() {
        HandlerThread releaseHandlerThread = new HandlerThread("releaseHandlerThread");
        releaseHandlerThread.start();
        mReleaseHandler = new Handler(releaseHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                int index = (int) msg.obj;
                release(index);
            }
        };
    }

    Handler inputLengthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvInputLength.setText(msg.obj.toString());
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void socketReceive() {

//        boolean firstFrame = getFirstFrame();

        while (true) {
            if (mSocket != null) {
                try {
                    mBuff = new byte[mSocket.getReceiveBufferSize()];
                    mDp = new DatagramPacket(mBuff, mBuff.length);

                    Log.d(TAG, "mSocket preper ... ");
                    mSocket.receive(mDp);
                    Log.d(TAG, "mSocket receive  ... ");

                    mLength = mDp.getLength();
                    mBuff = mDp.getData();

//                        if (mLength > 0) {
//                            String msg1 = "接收长度 " + mLength;
//                            Log.d(TAG, msg1);
//                            Message msg = new Message();
//                            msg.obj = msg1;
////                            inputLengthHandler.sendMessage(msg);
//                        }

                    Message msg = mDecodeHandler.obtainMessage();
                    msg.obj = mBuff;
                    mDecodeHandler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void decode(byte[] mBuff) {
        ByteBuffer inputBuffer = null;

        int inputBufferIndex = mMediaDeCodec.dequeueInputBuffer(Constants.DECODE_TIMEOUT_USEC);
        if (inputBufferIndex >= 0) {
//                        inputBuffer = mMediaDeCodec.getInputBuffer(inputBufferIndex);
            inputBuffer = mInputBuffers[inputBufferIndex];

            inputBuffer.clear();
            inputBuffer.put(mBuff);

            if (mLength == -1) {
                return;
            }
            mMediaDeCodec.queueInputBuffer(inputBufferIndex, 0, mLength,
                    Constants.DECODE_TIMEOUT_USEC, 0);
//                        bytesToImageFile(buff);

        }

        int outputBufferIndex = 0;
        try {
            outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(mBufferInfo,
                    Constants.DECODE_TIMEOUT_USEC);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message message = mReleaseHandler.obtainMessage();
        message.obj = outputBufferIndex;
        mReleaseHandler.sendMessage(message);

//        release(outputBufferIndex);

        if (inputBuffer != null) {
            inputBuffer.clear();
        }
        mBuff = null;
//                    mMediaDeCodec.flush();
    }

    private void release(int outputBufferIndex) {

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
//                            outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
//                            byte[] yuvData = new byte[outputBuffer.remaining()];
//                            outputBuffer.get(yuvData); //获取到了每一帧。yuvData 是每一帧。

                //延时操作
                //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);
                //渲染
//                            while (outputBufferIndex >= 0) {
//                                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
//                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(mBufferInfo
//                                        , Constants.DECODE_TIMEOUT_USEC);
//                            }

                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
//                while (outputBufferIndex >= 0) {
//                    mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
//                    outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(mBufferInfo,
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

    private void initDecoder() {
        Log.d(TAG, "初始化解码器...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

//            surfaceTexture.setDefaultBufferSize(Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT);
//            mTtv.setAlpha(1.0f);
//            mTtv.setRotation(90.0f);

            mTtv.getWlRender().setOnSurfaceCreateListener(new VideoRender.OnSurfaceCreateListener() {
                @Override
                public void onSurfaceCreate(Surface surface) {

                    mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(surface);
                    mMediaDeCodec.start();

                    mInputBuffers = mMediaDeCodec.getInputBuffers();
                    mOutputBuffers = mMediaDeCodec.getOutputBuffers();

                    Log.d(TAG, "解码器创建成功...");

                    mBufferInfo = new MediaCodec.BufferInfo();

                    initSocket();

                }
            });

        }
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "连接 Socket ...");
                    mSocket = new DatagramSocket(Constants.PORT);
                    Log.d(TAG, "Socket 连接成功...");

//                    setData5();

                    socketReceive();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
