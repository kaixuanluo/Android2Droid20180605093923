package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.luokaixuan.android2droid201806050939.Util.SpUtil;
import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import static com.example.luokaixuan.android2droid201806050939.constants.Constants.PORT;

public class ClientMainActivity6TcpTexture extends ScreenOnActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = "ClientMainActivity";

    private ServerSocket mSS;

    Socket mSocket;
    //    private OutputStream mOs;
    private InputStream mIs;

    private TextureView mTtv;

    private SurfaceTexture mSt;

    MediaCodec mMediaDeCodec;

    MediaFormat mMediaFormat;

    ByteBuffer[] mInputBuffers;
    int mInputBufferIndex;
    private MediaExtractor mMediaExtractor;
    private ByteBuffer[] mOutputBuffers;
    private byte[] mSps;
    private byte[] mPps;

    TextView tvInputLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client2);

        mTtv = (TextureView) findViewById(R.id.mTtv);

        tvInputLength = findViewById(R.id.inputLengthTv);

        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        int height1 = wm1.getDefaultDisplay().getHeight();

//        mTtv.setLayoutParams(new FrameLayout.LayoutParams((width1 / 35) * 9, height1));

//        mTtv.getHolder().addCallback(this);

        mTtv.setSurfaceTextureListener(this);
    }

    Handler inputLengthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvInputLength.setText(msg.obj.toString());
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setData3() {

//        boolean firstFrame = getFirstFrame();

        while (true) {
            if (mIs != null) {
                try {

                    ByteBuffer inputBuffer = null;

                    int inputBufferIndex = mMediaDeCodec.dequeueInputBuffer(Constants.DECODE_TIMEOUT_USEC);
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

                        inputBuffer.clear();
                        inputBuffer.put(buff);

                        if (le == -1) {
                            continue;
                        }
                        mMediaDeCodec.queueInputBuffer(inputBufferIndex, 0, length, Constants.DECODE_TIMEOUT_USEC, 0);
//                        bytesToImageFile(buff);

                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                    int outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo, Constants.DECODE_TIMEOUT_USEC);
                    switch (outputBufferIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.v(TAG, "format changed");
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
//                            Log.v(TAG, "解码当前帧超时");
                            break;
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            mOutputBuffers = mMediaDeCodec.getOutputBuffers();
                            Log.v(TAG, "output buffers changed");
                            break;
                        default:
                            //直接渲染到Surface时使用不到outputBuffer
                            ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];
                            //延时操作
                            //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
//                        sleepRender(videoBufferInfo, startMs);
                            //渲染
                            while (outputBufferIndex >= 0) {
                                mMediaDeCodec.releaseOutputBuffer(outputBufferIndex, true);
                                outputBufferIndex = mMediaDeCodec.dequeueOutputBuffer(bufferInfo, Constants.DECODE_TIMEOUT_USEC);
                            }
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void bytesToImageFile(byte[] bytes) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.jpeg");
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
            SurfaceTexture surfaceTexture = mTtv.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(Constants.PHONE_WIDTH, Constants.PHONE_HEIGHT);
//            mTtv.setAlpha(1.0f);
//            mTtv.setRotation(90.0f);
            mMediaDeCodec = DeEncodecCommon.getMediaDeCodec(new Surface(surfaceTexture));
            mMediaDeCodec.start();

            mInputBuffers = mMediaDeCodec.getInputBuffers();
            mOutputBuffers = mMediaDeCodec.getOutputBuffers();

            Log.d(TAG, "解码器创建成功...");

        }
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "连接 Socket ...");
                    mSS = new ServerSocket(PORT);
                    mSocket = mSS.accept();
                    Log.d(TAG, "Socket 连接成功...");
                    mIs = mSocket.getInputStream();

                    setData3();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureAvailable ... ");
        if(mSt!=null){
            mTtv.setSurfaceTexture(mSt);
        }
        initDecoder();
        initSocket();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureSizeChanged ... ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroyed ... ");
        mSt = surfaceTexture;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureUpdated ... ");
    }
}
