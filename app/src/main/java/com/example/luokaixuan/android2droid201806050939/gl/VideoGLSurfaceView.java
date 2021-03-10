package com.example.luokaixuan.android2droid201806050939.gl;

/**
 * @author created by luokaixuan
 * @date 2019/5/21
 * 这个类是用来干嘛的
 */
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

public class VideoGLSurfaceView extends GLSurfaceView {

    private VideoRender render;
    private RenderSurfaceCreatedListener mRenderSurfaceCreatedListener;

    public VideoGLSurfaceView(Context context) {
        this(context, null);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        render = new VideoRender(context);
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        render.setOnRenderListener(new VideoRender.OnRenderListener() {
            @Override
            public void onRender() {
                requestRender();
            }
        });

        render.setOnSurfaceCreateListener(new VideoRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface surface) {
                if (mRenderSurfaceCreatedListener != null) {
                    mRenderSurfaceCreatedListener.onRenderSurfaceCreated(surface);
                }
            }
        });
    }

    public VideoRender getWlRender() {
        return render;
    }

    public interface RenderSurfaceCreatedListener {
        void onRenderSurfaceCreated(Surface surface);
    }

    public void setRenderSurfaceCreatedListener(RenderSurfaceCreatedListener renderSurfaceCreatedListener) {
        this.mRenderSurfaceCreatedListener = renderSurfaceCreatedListener;
    }
}