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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 90678 on 2017/8/1.
 */

public class ServiceMainActivity2Udp extends ScreenOnActivity {

    private static final String TAG = ServiceMainActivity2Udp.class.getSimpleName();
    private MediaCodec encoder = null;
    private VirtualDisplay virtualDisplay;
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    static MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;

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
    private DatagramSocket datagramSocket;
    private Handler mSocketOutputDataHandler;
    private ServerSocket mServerSocket;
    private Socket mSocket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_service);

        tvIp = findViewById(R.id.main_server_ip_tv);

        tvLength = findViewById(R.id.serverInputLengthTv);

        tvIp.setText("本机IP ：" + IpUtil.getIpAddress(this) + "\n\n\n" + " WIFI IP " + IpUtil.getWifiIp(this));

        initSocket();

        initSocketOutputDataHandler();

    }

    private void initSocketOutputDataHandler() {
        HandlerThread outputDataHandler = new HandlerThread("outputDataHandler");
        outputDataHandler.start();
        mSocketOutputDataHandler = new Handler(outputDataHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        };
    }

    private void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    mServerSocket = new ServerSocket(Constants.PORT_HAND_SHARE);
//                    mSocket = mServerSocket.accept();
//                    datagramSocket = new DatagramSocket(Constants.PORT, mSocket.getInetAddress());

                    String ip = SpUtil.getIp(ServiceMainActivity2Udp.this);
                    Log.d(TAG, "initSocket SpUtil.getIp " + ip);
                    Log.d(TAG, "Constants.PORT " + Constants.PORT);
                    datagramSocket = new DatagramSocket(Constants.PORT);
//                    SocketAddress socketAddress = new InetSocketAddress(ip, Constants.PORT);
//                    datagramSocket.bind(socketAddress);
                    startScreenCapture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

            new Thread(new EncoderWorker()).start();

        }
    }

    @TargetApi(19)
    private class EncoderWorker implements Runnable {

        @Override
        public void run() {
            sendData1();

        }
    }

    private void sendData1() {

        startDisplayManager();

        ByteBuffer[] encoderOutputBuffers = new ByteBuffer[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                encoderOutputBuffers = encoder.getOutputBuffers();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                break;
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                Log.d(TAG, "no output from encoder available");
                continue;
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

                final byte[] b = new byte[bufferInfo.size];
                try {
                    if (bufferInfo.size != 0) {
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        encodedData.position(bufferInfo.offset);
                        encodedData.get(b, bufferInfo.offset, bufferInfo.offset + bufferInfo.size);

//                        Message message = new Message();
//                        message.obj = b;
//                        mSocketOutputDataHandler.sendMessage(message);

//                        byte[] b = (byte[]) msg.obj;
                        try {

//                    datagramSocket.send(new DatagramPacket(b, b.length));

                            String ip = SpUtil.getIp(ServiceMainActivity2Udp.this);
                            Log.d(TAG, "initSocket SpUtil.getIp " + ip);

                            InetAddress inetAddress = InetAddress.getByName(ip);

                            datagramSocket.send(new DatagramPacket(b, b.length, inetAddress,
                                    Constants.PORT));

                            String msg1 = "输出长度 " + b.length;
                            Log.d(TAG, msg1);

                            Message msg2 = new Message();
                            msg2.obj = msg1;
                            outPutLengthHandler.sendMessage(msg2);

                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


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
            mServerSocket.close();
            datagramSocket.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
