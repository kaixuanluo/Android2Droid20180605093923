package com.example.luokaixuan.android2droid201806050939;

import com.example.luokaixuan.android2droid201806050939.Util.SpUtil;
import com.example.luokaixuan.android2droid201806050939.constants.Constants;

/**
 * @author created by luokaixuan
 * @date 2019/6/10
 * 这个类是用来干嘛的
 */
public class ClientMainActivity11UdpIjkplayer extends ClientMainActivity10TcpIjkplayer {

    @Override
    public void setVideoPath() {
        mPath = "udp://" + SpUtil.getIp(ClientMainActivity11UdpIjkplayer.this) + ":" + Constants.PORT;
    }

}
