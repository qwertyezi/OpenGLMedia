package com.yezi.openglmedia.render;

import android.graphics.SurfaceTexture;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.filter.NoFilter;
import com.yezi.openglmedia.utils.enums.FilterType;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRender extends BaseRender {

    protected SurfaceTexture mSurfaceTexture;
    protected float[] mTransformMatrix = new float[16];
    protected boolean mHasFirstDrawFrame = false;
    private int mDrawFrameCount = 0;

    public VideoRender() {
        super(new NoFilter().setFilterType(FilterType.VIDEO));
    }

    public VideoRender(BaseFilter filter) {
        super(filter);
    }

    public interface onSurfaceCreatedListener {
        void onSurfaceCreated();
    }

    private onSurfaceCreatedListener mListener;

    public void setOnSurfaceCreatedListener(onSurfaceCreatedListener listener) {
        mListener = listener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);

        mFilter.setTextureId(mTextureId);
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        if (mListener != null) {
            mListener.onSurfaceCreated();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture == null) {
            return;
        }
        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        mFilter.setTransformMatrix(mTransformMatrix);

        super.onDrawFrame(gl);

        if (!mHasFirstDrawFrame && mDrawFrameListener != null) {
            if (mDrawFrameCount == 1) {
                mHasFirstDrawFrame = true;
                mDrawFrameListener.onDrawFrame();
            }
            ++mDrawFrameCount;
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void release() {
        super.release();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mHasFirstDrawFrame = false;
        mDrawFrameCount = 0;
    }

}
