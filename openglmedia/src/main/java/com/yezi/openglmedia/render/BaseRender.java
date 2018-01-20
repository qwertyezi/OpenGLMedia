package com.yezi.openglmedia.render;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.filter.NoFilter;
import com.yezi.openglmedia.render.listener.onDrawFrameListener;
import com.yezi.openglmedia.utils.enums.FilterType;
import com.yezi.openglmedia.utils.enums.ScaleType;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BaseRender implements GLSurfaceView.Renderer {

    protected BaseFilter mFilter;
    protected int mTextureId;
    protected int TEXTURE_2D_BINDABLE;
    protected onDrawFrameListener mDrawFrameListener;

    public BaseRender() {
        this(new NoFilter());
    }

    public BaseRender(BaseFilter filter) {
        mFilter = filter;
    }

    public void setContext(Context context) {
        mFilter.setContext(context);
    }

    public void setDrawFrameListener(onDrawFrameListener listener) {
        mDrawFrameListener = listener;
    }

    private void initTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(TEXTURE_2D_BINDABLE, textures[0]);

        GLES20.glTexParameteri(TEXTURE_2D_BINDABLE, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(TEXTURE_2D_BINDABLE, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(TEXTURE_2D_BINDABLE, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(TEXTURE_2D_BINDABLE, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        mTextureId = textures[0];
    }

    public void setFilter(BaseFilter filter) {
        filter.setContext(mFilter.getContext());
        filter.setScaleType(mFilter.getScaleType());
        filter.setFilterType(mFilter.getFilterType());
        filter.setTextureId(mFilter.getTextureId());
        filter.setDataSize(mFilter.getDataWidth(), mFilter.getDataHeight());

        filter.onSurfaceCreated();
        filter.onSurfaceChanged(mFilter.getViewWidth(), mFilter.getViewHeight());

        GLES20.glDeleteProgram(mFilter.getProgram());
        mFilter = filter;
    }

    public void setDataSize(int width, int height) {
        mFilter.setDataSize(width, height);
    }

    public void setScaleType(ScaleType scaleType) {
        mFilter.setScaleType(scaleType);
    }

    public ScaleType getScaleType() {
        return mFilter.getScaleType();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        TEXTURE_2D_BINDABLE = mFilter.getFilterType() == FilterType.VIDEO ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;

        initTexture();
        mFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mFilter.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mFilter.onDrawFrame();
    }

    public void release() {
        mFilter.release();
    }
}
