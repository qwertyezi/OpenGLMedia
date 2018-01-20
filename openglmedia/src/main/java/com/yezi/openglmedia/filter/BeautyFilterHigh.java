package com.yezi.openglmedia.filter;

import android.opengl.GLES20;

import com.yezi.openglmedia.R;
import com.yezi.openglmedia.utils.enums.FilterType;

import java.nio.FloatBuffer;

public class BeautyFilterHigh extends BaseFilter {

    private int mWidth, mHeight;
    private float[] mLocation;

    private int mSingleStepOffsetLocation;
    private int mParams;

    public BeautyFilterHigh() {
        this(FilterType.IMAGE);
    }

    public BeautyFilterHigh(FilterType filterType) {
        super(0, R.raw.beauty_fragment_high_optimization);
        setFilterType(filterType);
        setBeautyLevel(5);
    }

    public BeautyFilterHigh setBeautyLevel(int level) {
        switch (level) {
            case 1:
                mLocation = new float[]{1.0f, 1.0f, 0.15f, 0.15f};
                break;
            case 2:
                mLocation = new float[]{0.8f, 0.9f, 0.2f, 0.2f};
                break;
            case 3:
                mLocation = new float[]{0.6f, 0.8f, 0.25f, 0.25f};
                break;
            case 4:
                mLocation = new float[]{0.4f, 0.7f, 0.38f, 0.3f};
                break;
            case 5:
                mLocation = new float[]{0.33f, 0.63f, 0.4f, 0.35f};
                break;
            default:
                break;
        }
        return this;
    }

    @Override
    public void onDraw() {
        GLES20.glUniform2fv(mSingleStepOffsetLocation, 1, FloatBuffer.wrap(new float[]{2.0f / mWidth, 2.0f / mHeight}));
        GLES20.glUniform4fv(mParams, 1, FloatBuffer.wrap(mLocation));
    }

    @Override
    public void onCreated(int mProgram) {
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgram, "singleStepOffset");
        mParams = GLES20.glGetUniformLocation(mProgram, "params");
    }

    @Override
    public void onChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
}
