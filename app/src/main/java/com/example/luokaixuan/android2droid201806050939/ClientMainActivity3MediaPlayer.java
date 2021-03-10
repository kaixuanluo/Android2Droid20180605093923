package com.example.luokaixuan.android2droid201806050939;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceView;

import com.example.luokaixuan.android2droid201806050939.constants.Constants;

import org.videolan.libvlc.media.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;

import java.io.IOException;

/**
 * Created by luokaixuan
 * Created Date 2018/6/19.
 * Description TODO
 */
public class ClientMainActivity3MediaPlayer extends ScreenOnActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_client);

        SurfaceView surface = findViewById(R.id.mSv);

        MediaPlayer2 mediaPlayer = new MediaPlayer2(this);
        mediaPlayer.setDisplay(surface.getHolder());

        try {
            mediaPlayer.setDataSource(this, Uri.parse("tcp://"+getIntent().getStringExtra("IP") + ":" + Constants.PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }


}
