package com.example.luokaixuan.android2droid201806050939.other;

/**
 * Created by luokaixuan
 * Created Date 2018/6/5.
 * Description TODO
 */
public class VideoEncoder implements VideoCodec {

//    private Worker mWorker;
//    private MediaProjection mMediaProjection;
//    private VirtualDisplay mVirtualDisplay;
//    private Client mClient;
//    //写入本地的流，在调试的时候使用
//    private DataOutputStream mOutput;
//    private final boolean isDebug = true;
//    private final String TAG = "VideoEncoder";
//    private byte[] mFrameByte;
//
//    public VideoEncoder(MediaProjection mediaProjection, Client client) {
//        mClient = client;
//        mMediaProjection = mediaProjection;
//        if (isDebug) {
//            try {
//                mOutput = new DataOutputStream(new FileOutputStream(new File("/sdcard/h264encode")));
//                ;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    protected void onSurfaceCreated(Surface surface, int mWidth, int mHeight) {
//        //将屏幕数据与surface进行关联
//        mVirtualDisplay = mMediaProjection.createVirtualDisplay("-display",
//                mWidth, mHeight, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//                surface, null, null);
//
//    }
//
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    protected void onSurfaceDestroyed(Surface surface) {
//        mVirtualDisplay.release();
//        surface.release();
//    }
//
//
//    public void start() {
//        if (mWorker == null) {
//            mWorker = new Worker();
//            mWorker.setRunning(true);
//            mWorker.start();
//        }
//    }
//
//    public void stop() {
//        if (mWorker != null) {
//            mWorker.setRunning(false);
//            mWorker = null;
//        }
////        if (mClient != null) {
////            if (!mClient.hasRelease()) {
////                mClient.release();
////            }
////        }
//    }
//
//
//    private class Worker extends Thread {
//        private MediaCodec.BufferInfo mBufferInfo;
//        private MediaCodec mCodec;
//        private volatile boolean isRunning;
//        private Surface mSurface;
//        private final long mTimeoutUsec;
//        private int mWidth;
//        private int mHeight;
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        public Worker() {
//            mBufferInfo = new MediaCodec.BufferInfo();
//            mTimeoutUsec = 10000l;
//        }
//
//        public void setRunning(boolean running) {
//            isRunning = running;
//        }
//
//        protected void onEncodedSample(MediaCodec.BufferInfo info, ByteBuffer data) {
//            if (mFrameByte == null || mFrameByte.length < info.size) {
//                mFrameByte = new byte[info.size];
//            }
//            data.get(mFrameByte, 0, info.size);
////            boolean isSuccess1 = mClient.sendInt(info.size);
//            boolean isSuccess2 = mClient.send(mFrameByte, 0, info.size);
////            Log.d(TAG, "sending success:" + isSuccess1 + "  " + isSuccess2);
////            if (!(isSuccess1 && isSuccess2)) {
////                isRunning = false;
////                mClient.release();
////            }
//            //在debug时在本地写一份
////            if (isDebug) {
////                try {
////                    mOutput.writeInt(info.size);
////                    mOutput.write(mFrameByte, 0, info.size);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
//        }
//
//        @Override
//        public void run() {
//            if (!prepare()) {
//                isRunning = false;
//            }
//            while (isRunning) {
//                encode();
//            }
//            release();
//        }
//
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//        void encode() {
//            if (!isRunning) {
//                //编码结束，发送结束信号，让surface不在提供数据
//                mCodec.signalEndOfInputStream();
//            }
//            int status = mCodec.dequeueOutputBuffer(mBufferInfo, mTimeoutUsec);
//            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                return;
//            } else if (status >= 0) {
//                ByteBuffer data = mCodec.getOutputBuffer(status);
//                if (data != null) {
//                    final int endOfStream = mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM;
//                    //传递编码数据
//                    if (endOfStream == 0) {
//                        onEncodedSample(mBufferInfo, data);
//                    }
//                    // 一定要记得释放
//                    mCodec.releaseOutputBuffer(status, false);
//                    if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
//                        return;
//                    }
//                }
//            }
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        private void release() {
//            onSurfaceDestroyed(mSurface);
//            if (mCodec != null) {
//                mCodec.stop();
//                mCodec.release();
//            }
//            if (mOutput != null) {
//                try {
//                    mOutput.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//        private boolean prepare() {
//            // configure video output
////            mWidth= SpUtils.readInt(Constans.KEY_DEVICE_WIDTH,-1);
////            mHeight=SpUtils.readInt(Constans.KEY_DEVICE_HEIGHT,-1);
////            if(mWidth==-1||mHeight==-1){
////                return false;
////            }
//            mClient.connectToServer();
//            //发送宽高
////            boolean isSuccess1=mClient.sendInt(mWidth);
////            boolean isSuccess2=mClient.sendInt(mHeight);
////            if(!(isSuccess1&&isSuccess2)){
////                isRunning=false;
////                mClient.release();
////            }
//            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
//            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//            format.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE);
//            format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_PER_SECOND);
//            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL);
//            format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 40);
//            try {
//                mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//
//            }
//            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            //创建关联的输入surface
//            mSurface = mCodec.createInputSurface();
//            mCodec.start();
//            onSurfaceCreated(mSurface, mWidth, mHeight);
//            return true;
//        }
//    }
}