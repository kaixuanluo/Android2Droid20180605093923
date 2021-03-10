package com.example.luokaixuan.android2droid201806050939;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luokaixuan.android2droid201806050939.Util.IpUtil;
import com.example.luokaixuan.android2droid201806050939.Util.SpUtil;
import com.example.luokaixuan.android2droid201806050939.constants.Constants;
import com.example.luokaixuan.android2droid201806050939.constants.DeEncodecCommon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 90678 on 2017/8/1.
 */

public class ServiceMainActivity3Tcp extends ScreenOnActivity {

    private static final String TAG = ServiceMainActivity3Tcp.class.getSimpleName();
    private MediaCodec encoder = null;
    private VirtualDisplay virtualDisplay;
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    static MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;

    private ServerSocket mSS;
    private Socket mSocket;
    private InputStream mIs;
    private OutputStream mOs;
    private DataInputStream mDis;
    private DataOutputStream mDos;

    private List<Socket> mSocketList = new ArrayList<>();

    private EncodedListener el;

    private TextView tvIp;

    private TextView tvLength;

    Handler outPutLengthHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            tvLength.setText(msg.obj.toString());
        }
    };
    private int mScreenWidth;
    private int mScreenHeight;
    private SenderHandler mSenderHandler;
    private SenderRunnable mSenderRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_service);

        tvIp = findViewById(R.id.main_server_ip_tv);

        tvLength = findViewById(R.id.serverInputLengthTv);

        tvIp.setText("本机IP ：" + IpUtil.getIpAddress(this) + "\n\n\n" + " WIFI IP " + IpUtil.getWifiIp(this));

        initSocket();

        initSender();

    }

    private void initSocket() {
        new Thread(new SocketInitRunnable()).start();
    }

    private class SocketInitRunnable implements Runnable {

        @Override
        public void run() {
            initSocketRunnable();
        }
    }

    private void initSocketRunnable() {
        try {
            Log.d(TAG, "initSocket() start ");
            String ip = SpUtil.getIp(ServiceMainActivity3Tcp.this);
            Log.d(TAG, "ip " + ip);

            if (mSS != null) {
                mSS.close();
                mSS = null;
            }

            if (mSS == null) {
                mSS = new ServerSocket(Constants.PORT);

                try {
                    Log.d(TAG, "连接Socket 中...");
                    mSocket = mSS.accept();
                    mIs = mSocket.getInputStream();
                    mOs = mSocket.getOutputStream();
                    mDis = new DataInputStream(mIs);
                    mDos = new DataOutputStream(mOs);
                    Log.d(TAG, "连接Socket 成功... ");
                    mSocketList.add(mSocket);

                } catch (IOException e) {
                    e.printStackTrace();
                    if (mSocketList.contains(mSocket)) {
                        mSocketList.remove(mSocket);
                    }
                }

                Log.d(TAG, "initSocket() end ");

                Log.d(TAG, "startScreenCapture start ");

                startScreenCapture();

                Log.d(TAG, "startScreenCapture end ");

            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSender() {
        HandlerThread handlerThread = new HandlerThread("senderHandlerThread");
        handlerThread.start();
        mSenderHandler = new SenderHandler(handlerThread.getLooper(), new Handler.Callback(){

            @Override
            public boolean handleMessage(Message msg) {

                byte[] b = (byte[]) msg.obj;

                    try {
                        if (mDis == null) {

                        }
                        if (mDos == null) {

                        }

                        if (mSocket != null && mSocket.isConnected()) {
                            mDos.write(b);
                            mDos.flush();

                            String msg1 = "输出长度 " + b.length;
                            Log.d(TAG, msg1);

                            Message lengthMsg = new Message();
                            lengthMsg.obj = msg1;
                            outPutLengthHandler.sendMessage(lengthMsg);
                        } else {
                            Log.d(TAG, "mSocket == null || !mSocket.isConnected()");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                            }
//                        }).start();

                    if (el != null) {
                        el.encoded(b);
                    }

                return false;
            }
        });
        mSenderRunnable = new SenderRunnable();
    }

    private class SenderHandler extends Handler {

        public SenderHandler(Looper looper) {

        }

        public SenderHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    }

    private class SenderRunnable implements Runnable{


        @Override
        public void run() {

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenCapture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaProjectionManager =
                    (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }

        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "User cancelled the access", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "onActivityResult");

            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

            startDisplayManager();

            new Thread(new EncoderWorker()).start();

        }
    }

    @TargetApi(19)
    private class EncoderWorker implements Runnable {

        @Override
        public void run() {

            boolean isAccept = true;

            if (mDos == null) {
                Log.e(TAG, " mDos is null return...");
                return;
            }

            sendData1();

//                if (mSS != null) {
//                    try {
//                        mDis.close();
//                        mDos.flush();
//                        mDos.close();
////                        mSocket = null;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            initSocket();

        }
    }

    private void sendData1() {
        ByteBuffer[] encoderOutputBuffers = new ByteBuffer[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            encoderOutputBuffers = encoder.getOutputBuffers();
        }

        boolean encoderDone = false;
        MediaCodec.BufferInfo bufferInfo = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            bufferInfo = new MediaCodec.BufferInfo();
        }
        String infoString;

        while (!encoderDone) {
            int encoderStatus = 0;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    encoderStatus = encoder.dequeueOutputBuffer(bufferInfo,
                            Constants.ENCODE_TIMEOUT_USEC);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                continue;
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                //Log.d(TAG, "no output from encoder available");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    encoderOutputBuffers = encoder.getOutputBuffers();
                }
                Log.d(TAG, "encoder output buffers changed");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // not expected for an encoder
                MediaFormat newFormat = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    newFormat = encoder.getOutputFormat();


                    ByteBuffer spsb = newFormat.getByteBuffer("csd-0");
                    ByteBuffer ppsb = newFormat.getByteBuffer("csd-1");

                    if (spsb != null) {
                        byte[] sps = new byte[spsb.capacity()];
                        //spsb.position(4);
                        spsb.get(sps, 0, sps.length);

                        try {
                            mDos.write(sps);
                            //outputStream.write(pps);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    if (ppsb != null) {
                        byte[] pps = new byte[ppsb.capacity()];
                        ppsb.get(pps);

                        try {
                            mDos.write(pps);
                            //outputStream.write(pps);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }

                Log.d(TAG, "encoder output format changed: " + newFormat);
            } else if (encoderStatus < 0) {
                Log.e(TAG, "encoderStatus < 0");
                continue;
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    Log.d(TAG, "============It's NULL. BREAK!=============");
                    continue;
                }

//                        infoString = info.offset + "," + info.size + "," +
//                                info.presentationTimeUs + "," + info.flags;
//                        try {
//                            mDos.write(infoString.getBytes());
//                            Log.d(TAG, "输出 info " + infoString);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                final byte[] b = new byte[bufferInfo.size];
                try {
                    if (bufferInfo.size != 0) {
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        encodedData.position(bufferInfo.offset);
                        encodedData.get(b, bufferInfo.offset, bufferInfo.offset + bufferInfo.size);

                        Message sendMessage = new Message();
                        sendMessage.obj=b;
                        mSenderHandler.sendMessage(sendMessage);
                    }

                } catch (BufferUnderflowException e) {
                    e.printStackTrace();
                }

                encoderDone = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;

                try {
                    if (encoder == null) {
                        Log.e("ServerService ", "encoder is null");
                        continue;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        encoder.releaseOutputBuffer(encoderStatus, false);
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public interface EncodedListener {
        void encoded(byte[] bs);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void startDisplayManager() {
        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Surface encoderInputSurface = null;
        try {
            encoderInputSurface = createDisplaySurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        int densityDpi = getResources().getDisplayMetrics().densityDpi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            virtualDisplay = mDisplayManager.createVirtualDisplay("Remote Droid",
                    DeEncodecCommon.getPhoneWidth(), DeEncodecCommon.getPhoneHeight(), 1,
                    encoderInputSurface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
        } else {
            if (mMediaProjection != null) {
                virtualDisplay = mMediaProjection.createVirtualDisplay("Remote Droid",
                        DeEncodecCommon.getPhoneWidth(), DeEncodecCommon.getPhoneHeight()
                        , 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, encoderInputSurface
                        , null, null);
            } else {
                Log.e(TAG, "Something went wrong. Please restart the app.");
            }
        }

//        sendData2();
        encoder.start();
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mMediaProjection.stop();
//            }
//            startDisplayManager();
//        }
//    }

    /**
     * Create the display surface out of the encoder. The data to encoder will be fed from this
     * Surface itself.
     *
     * @return
     * @throws IOException
     */
    @TargetApi(19)
    private Surface createDisplaySurface() throws IOException {

        Log.i(TAG, "Starting encoder");
        encoder = DeEncodecCommon.getMediaEnCodec();
        Surface surface = encoder.createInputSurface();
        return surface;
    }

    public synchronized static void writeNoRoot(String command) {
        // Lets see if i need to boot daemon...
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mSS.close();
            mSS = null;

            mSocket.close();
            mSocket = null;

            mDos.close();
            mDos = null;

            mDis.close();
            mDis = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
