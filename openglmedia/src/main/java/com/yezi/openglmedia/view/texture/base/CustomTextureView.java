package com.yezi.openglmedia.view.texture.base;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.yezi.openglmedia.BuildConfig;
import com.yezi.openglmedia.gles.EglCore;

public class CustomTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "CustomTextureView";
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private TextureRenderer mTextureRenderer;
    private GLSurfaceView.Renderer mRenderer;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    public CustomTextureView(Context context) {
        super(context);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;
    }

    public synchronized void requestRender() {
        if (mTextureRenderer != null) {
            mTextureRenderer.requestRender();
        }
    }

    public void onPause() {
        if (mTextureRenderer == null || mTextureRenderer.getHandler() == null) {
            return;
        }
        mTextureRenderer.getHandler().sendShutdown();
        mTextureRenderer = null;
    }

    public void onResume() {
        mTextureRenderer = new TextureRenderer();
        mTextureRenderer.start();
        mTextureRenderer.waitUntilReady();
        mTextureRenderer.setRenderer(mRenderer);
        if (getSurfaceTexture() != null) {
            mTextureRenderer.getHandler().sendSurfaceAvailable(getSurfaceTexture(), mSurfaceWidth, mSurfaceHeight);
        }
    }

    public synchronized void queueEvent(Runnable runnable) {
        if (mTextureRenderer != null) {
            mTextureRenderer.getHandler().queueEvent(runnable);
        }
    }

    public EglCore getEGLCore() {
        if (mTextureRenderer != null) {
            return mTextureRenderer.getEGLCore();
        }
        return null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mTextureRenderer != null) {
            mTextureRenderer.getHandler().sendSurfaceAvailable(surface, width, height);
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mTextureRenderer != null) {
            mTextureRenderer.getHandler().sendSurfaceChanged(width, height);
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (DEBUG) Log.e(TAG, "onSurfaceTextureDestroyed");

        if (mTextureRenderer != null) {
            mTextureRenderer.getHandler().sendSurfaceDestroyed();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
