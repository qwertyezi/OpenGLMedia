package com.yezi.openglmedia.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.yezi.openglmedia.utils.GL2Utils;
import com.yezi.openglmedia.utils.enums.FilterType;
import com.yezi.openglmedia.utils.enums.ScaleType;


public class BlurFilter extends BaseFilter {

    private int[] mTextureDownScale;
    private FrameBufferObject mFramebuffer;
    private static final int LEVEL = 30;
    private int mIntensity = 0;
    private float[] mFlipMatrix = new float[16];
    private static final int BLUR_SIZE = 512;
    private NoFilter mNoFilter;
    private ScaleType mGroupScaleType = ScaleType.CENTER_INSIDE;

    public BlurFilter() {
        this(FilterType.IMAGE);
    }

    public BlurFilter(FilterType filterType) {
        super();
        setFilterType(filterType);

        mNoFilter = new NoFilter();
        mNoFilter.setScaleType(ScaleType.FIT_XY);
        mNoFilter.setDataSize(mDataWidth, mDataHeight);
    }

    public BlurFilter setIntensity(int intensity) {
        if (intensity != mIntensity && intensity >= 0) {
            mIntensity = intensity;
            if (mIntensity > LEVEL)
                mIntensity = LEVEL;
        }
        return this;
    }

    @Override
    public void setDataSize(int width, int height) {
        super.setDataSize(width, height);
        mNoFilter.setDataSize(width, height);
    }

    @Override
    public BaseFilter setScaleType(ScaleType type) {
        mGroupScaleType = type;
        return this;
    }

    @Override
    public ScaleType getScaleType() {
        return mGroupScaleType;
    }

    private int calcMips(int len, int level) {
        return len / (level + 1);
    }


    @Override
    public void onSurfaceCreated() {
        super.onSurfaceCreated();

        mTextureDownScale = new int[mIntensity];
        GLES20.glGenTextures(mIntensity, mTextureDownScale, 0);

        for (int i = 0; i < mIntensity; ++i) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDownScale[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, calcMips(BLUR_SIZE, i + 1), calcMips(BLUR_SIZE, i + 1), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        mFramebuffer = new FrameBufferObject();

        mNoFilter.setContext(getContext());
        mNoFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);

        mNoFilter.onSurfaceChanged(width, height);
        Matrix.multiplyMM(mFlipMatrix, 0, getMVPMatrix(), 0, GL2Utils.flip(GL2Utils.getOriginalMatrix(), false, true), 0);
        setMVPMatrix(mFlipMatrix);
    }

    @Override
    public void onDrawFrame() {
        if (mIntensity == 0) {
            GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
            super.setScaleType(mGroupScaleType);
            super.onDrawFrame();
            return;
        }

        mFramebuffer.bindTexture(mTextureDownScale[0]);
        GLES20.glViewport(0, 0, calcMips(BLUR_SIZE, 1), calcMips(BLUR_SIZE, 1));
        super.setScaleType(ScaleType.FIT_XY);
        super.onDrawFrame();

        mNoFilter.setScaleType(ScaleType.FIT_XY);
        for (int i = 1; i < mIntensity; ++i) {
            mFramebuffer.bindTexture(mTextureDownScale[i]);
            mNoFilter.setTextureId(mTextureDownScale[i - 1]);
            GLES20.glViewport(0, 0, calcMips(BLUR_SIZE, i + 1), calcMips(BLUR_SIZE, i + 1));
            mNoFilter.onDrawFrame();
        }

        for (int i = mIntensity - 1; i > 0; --i) {
            mFramebuffer.bindTexture(mTextureDownScale[i - 1]);
            mNoFilter.setTextureId(mTextureDownScale[i]);
            GLES20.glViewport(0, 0, calcMips(BLUR_SIZE, i), calcMips(BLUR_SIZE, i));
            mNoFilter.onDrawFrame();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
        mNoFilter.setScaleType(mGroupScaleType);
        mNoFilter.setTextureId(mTextureDownScale[0]);
        mNoFilter.onDrawFrame();
    }

    @Override
    public void onCreated(int mProgram) {

    }

    @Override
    public void onDraw() {

    }

    @Override
    public void onChanged(int width, int height) {

    }

    private class FrameBufferObject {
        private int mFramebufferID;

        FrameBufferObject() {
            int[] buf = new int[1];
            GLES20.glGenFramebuffers(1, buf, 0);
            mFramebufferID = buf[0];
        }

        public void release() {
            GLES20.glDeleteFramebuffers(1, new int[]{mFramebufferID}, 0);
        }

        void bind() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferID);
        }

        public void bindTexture(int texID) {
            bind();
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texID, 0);
        }

    }
}
