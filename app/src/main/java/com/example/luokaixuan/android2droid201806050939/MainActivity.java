package com.example.luokaixuan.android2droid201806050939;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.luokaixuan.android2droid201806050939.Util.SpUtil;

import java.util.Arrays;

public class MainActivity extends ScreenOnActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initConstants();

        final EditText etIp = (EditText) findViewById(R.id.main_server_ip_et);

        findViewById(R.id.main_client_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientMainActivity1TcpSurface.class));
            }
        });

        findViewById(R.id.main_client_bt_save_2_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientMainActivity1TcpSave2File.class));
            }
        });

        findViewById(R.id.main_client_bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientMainActivity2TcpTexture.class));
            }
        });

        findViewById(R.id.main_client_bt3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientMainActivity2TcpTexture.class));
            }
        });

        findViewById(R.id.main_client_bt5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity5UdpSurface.class);
                intent.putExtra("IP", etIp.getText().toString().trim());
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity6TcpTexture.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity7UdpTexture.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity8UdpGlSurfaceView.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity9UdpOpenGLEs.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity10TcpIjkplayer.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_client_bt11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientMainActivity11UdpIjkplayer.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.main_server_bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServiceMainActivity1Tcp.class));
            }
        });

        findViewById(R.id.main_server_bt3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServiceMainActivity2Udp.class));
            }
        });

        findViewById(R.id.main_server_bt5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServiceMainActivity3Tcp.class));
            }
        });

        etIp.setText(SpUtil.getIp(this));
        String ipStr = etIp.getText().toString().trim();
        etIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SpUtil.setIp(MainActivity.this, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        startActivity(new Intent(MainActivity.this, ServiceMainActivity.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getSupportColorFormat();
        }

        Intent intent = new Intent(this, ForegroundService.class);
        this.startService(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private int getSupportColorFormat() {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    System.out.println("found");
                    found = true;
                }
            }
            if (!found)
                continue;
            codecInfo = info;
        }

        Log.e("AvcEncoder", "Found " + codecInfo.getName() + " supporting " + "video/avc");

        // Find a color profile that the codec supports
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        Log.e("AvcEncoder",
                "length-" + capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));

        for (int i = 0; i < capabilities.colorFormats.length; i++) {

            switch (capabilities.colorFormats[i]) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                    Log.e("AvcEncoder", "supported color format::" + capabilities.colorFormats[i]);
                    break;

                default:
                    Log.e("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                    break;
            }
        }
        //return capabilities.colorFormats[i];
        return 0;
    }

//    private void initConstants() {
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        int heightPixels = dm.heightPixels;
//        Constants.PHONE_HEIGHT = heightPixels;
//        System.out.println("MainActivity heigth : " + heightPixels);
//        int widthPixels = dm.widthPixels;
//        Constants.PHONE_WIDTH = widthPixels;
//        System.out.println("MainActivity width : " + widthPixels);
//    }

}
