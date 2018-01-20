package com.yezi.openglmedia.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.yezi.openglmedia.utils.GL2Utils;
import com.yezi.openglmedia.utils.enums.ScaleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterGroup extends BaseFilter {

    protected int[] mFrameBuffers;
    protected int[] mTextures;
    protected float[] mFlipMatrix = new float[16];
    private ScaleType mGroupScaleType = ScaleType.CENTER_INSIDE;
    protected List<BaseFilter> mFilterList = new ArrayList<>();

    public FilterGroup() {

    }

    public FilterGroup(BaseFilter... filters) {
        initFilters(filters);
    }

    public void addFilters(BaseFilter... filters) {
        initFilters(filters);
    }

    public void clearFilter() {
        mFilterList.clear();
    }

    protected void initFilters(BaseFilter... filters) {
        Collections.addAll(mFilterList, filters);
        if (mDataWidth != 0 && mDataHeight != 0) {
            setDataSize(mDataWidth, mDataHeight);
            setScaleType(mGroupScaleType);
        }
    }

    @Override
    public void setDataSize(int width, int height) {
        super.setDataSize(width, height);
        for (int i = 0; i < mFilterList.size(); i++) {
            mFilterList.get(i).setDataSize(width, height);
        }
    }

    @Override
    public BaseFilter setScaleType(ScaleType type) {
        mGroupScaleType = type;
        super.setScaleType(ScaleType.FIT_XY);
        int size = mFilterList.size();
        for (int i = 0; i < size; i++) {
            mFilterList.get(i).setScaleType(i == size - 1 ? mGroupScaleType : ScaleType.FIT_XY);
        }
        return this;
    }

    @Override
    public ScaleType getScaleType() {
        return mGroupScaleType;
    }

    @Override
    public void onSurfaceCreated() {
        super.onSurfaceCreated();

        for (BaseFilter filter : mFilterList) {
            filter.setContext(getContext());
            filter.onSurfaceCreated();
        }
        initFrameBuffer();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);

        int size = mFilterList.size();
        for (int i = 0; i < size; i++) {
            BaseFilter filter = mFilterList.get(i);
            filter.onSurfaceChanged(width, height);

            if (i == size - 1 && size % 2 == 1) {
                Matrix.multiplyMM(mFlipMatrix, 0, filter.getMVPMatrix(), 0,
                        GL2Utils.flip(GL2Utils.getOriginalMatrix(), false, true), 0);
                filter.setMVPMatrix(mFlipMatrix);
            }
        }
    }

    @Override
    public void onDrawFrame() {
        if (mFrameBuffers == null || mTextures == null) {
            return;
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glViewport(0, 0, mDataWidth, mDataHeight);
        super.onDrawFrame();

        int previousTexture = mTextures[0];

        if (mFilterList != null) {
            int size = mFilterList.size();
            for (int i = 0; i < size - 1; i++) {
                BaseFilter filter = mFilterList.get(i);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i + 1]);
                filter.setTextureId(previousTexture);
                GLES20.glViewport(0, 0, mDataWidth, mDataHeight);
                filter.onDrawFrame();
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                previousTexture = mTextures[i + 1];
            }

            BaseFilter filter = mFilterList.get(size - 1);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            filter.setTextureId(previousTexture);
            GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
            filter.onDrawFrame();
        }

    }

    protected void initFrameBuffer() {
        if (mFilterList != null && mFilterList.size() > 0) {
            int size = mFilterList.size();
            mFrameBuffers = new int[size];
            mTextures = new int[size];

            for (int i = 0; i < size; i++) {
                GLES20.glGenFramebuffers(1, mFrameBuffers, i);
                GLES20.glGenTextures(1, mTextures, i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mDataWidth, mDataHeight, 0,
                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i]);
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                        GLES20.GL_TEXTURE_2D, mTextures[i], 0);

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }

    @Override
    public void onChanged(int width, int height) {

    }

    @Override
    public void onDraw() {

    }

    @Override
    public void onCreated(int mProgram) {

    }
}
