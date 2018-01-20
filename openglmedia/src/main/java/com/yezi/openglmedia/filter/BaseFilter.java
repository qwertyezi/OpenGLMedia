package com.yezi.openglmedia.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.yezi.openglmedia.R;
import com.yezi.openglmedia.utils.GL2Utils;
import com.yezi.openglmedia.utils.ShaderUtils;
import com.yezi.openglmedia.utils.enums.FilterType;
import com.yezi.openglmedia.utils.enums.ScaleType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public abstract class BaseFilter implements IRendererCallback {

    public static final int NO_FILTER = -1;

    private Context mContext;

    private int mProgram;
    private int glPosition;
    private int glTexture;
    private int glCoordinate;
    private int glMatrix;
    protected int glSTMatrix;

    private FloatBuffer mPos;
    protected FloatBuffer mCoord;

    protected int mDataWidth = 0;
    protected int mDataHeight = 0;
    protected int mViewWidth;
    protected int mViewHeight;

    private int mTextureId = NO_FILTER;
    private int mVertex;
    private int mFragment;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    protected float[] mTransformMatrix = new float[16];
    protected ScaleType mScaleType = ScaleType.CENTER_CROP;
    protected FilterType mFilterType = FilterType.IMAGE;
    protected int TEXTURE_2D_BINDABLE;

    public BaseFilter() {
        this(0, 0);
    }

    public BaseFilter(int vertexRes, int fragmentRes) {
        mVertex = vertexRes == 0 ? R.raw.default_vertex : vertexRes;
        mFragment = fragmentRes == 0 ? R.raw.default_fragment : fragmentRes;
    }

    private void initBuffer() {
        mPos = ByteBuffer
                .allocateDirect(GL2Utils.VERTEX_POSITION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(GL2Utils.VERTEX_POSITION);
        mPos.position(0);

        initTextureBuffer();
    }

    public void initTextureBuffer() {
        float[] position = mFilterType == FilterType.IMAGE ?
                GL2Utils.FRAGMENT_POSITION_IMAGE : GL2Utils.FRAGMENT_POSITION_VIDEO;
        mCoord = ByteBuffer
                .allocateDirect(position.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(position);
        mCoord.position(0);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setTextureId(int textureId) {
        mTextureId = textureId;
    }

    public int getTextureId() {
        return mTextureId;
    }

    public int getViewWidth() {
        return mViewWidth;
    }

    public int getViewHeight() {
        return mViewHeight;
    }

    public int getDataWidth() {
        return mDataWidth;
    }

    public int getDataHeight() {
        return mDataHeight;
    }

    public int getProgram() {
        return mProgram;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public FilterType getFilterType() {
        return mFilterType;
    }

    public float[] getMVPMatrix() {
        return Arrays.copyOf(mMVPMatrix, mMVPMatrix.length);
    }

    public void setMVPMatrix(float[] MVPMatrix) {
        mMVPMatrix = MVPMatrix;
    }

    public void setTransformMatrix(float[] transformMatrix) {
        mTransformMatrix = transformMatrix;
    }

    public void setDataSize(int width, int height) {
        mDataWidth = width;
        mDataHeight = height;

        if (mViewWidth != 0 && mViewHeight != 0) {
            onSurfaceChanged(mViewWidth, mViewHeight);
        }
    }

    public void release() {
        if (mTextureId != BaseFilter.NO_FILTER) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = BaseFilter.NO_FILTER;
        }
        GLES20.glDeleteProgram(mProgram);
        if (mCoord != null) {
            mCoord.clear();
        }
        if (mPos != null) {
            mPos.clear();
        }
    }

    public BaseFilter setScaleType(ScaleType type) {
        mScaleType = type;

        if (mViewWidth != 0 && mViewHeight != 0) {
            onSurfaceChanged(mViewWidth, mViewHeight);
        }
        return this;
    }

    public BaseFilter setFilterType(FilterType type) {
        mFilterType = type;
        return this;
    }

    @Override
    public void onSurfaceCreated() {
        initBuffer();

        TEXTURE_2D_BINDABLE = mFilterType == FilterType.VIDEO ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;

        mProgram = ShaderUtils.createProgram(mContext, mFilterType, mVertex, mFragment);

        glPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        glCoordinate = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        glTexture = GLES20.glGetUniformLocation(mProgram, "uTexture");
        glMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        glSTMatrix = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");

        onCreated(mProgram);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        int w = mDataWidth == 0 ? width : mDataWidth;
        int h = mDataHeight == 0 ? height : mDataHeight;
        float s1 = w / (float) width;
        float s2 = h / (float) height;
        float ratio = height / (float) width;
        switch (mScaleType) {
            case CENTER_CROP:
                if (s1 > s2) {
                    //w剪裁
                    Matrix.orthoM(mProjectMatrix, 0, -h / (float) w / ratio, h / (float) w / ratio, -1, 1, -1, 1);
                } else {
                    //h剪裁
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -w / (float) h * ratio, w / (float) h * ratio, -1, 1);
                }
                break;
            case CENTER_INSIDE:
                if (s1 > s2) {
                    //w满屏
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -w / (float) h * ratio, w / (float) h * ratio, -1, 1);
                } else {
                    //h满屏
                    Matrix.orthoM(mProjectMatrix, 0, -h / (float) w / ratio, h / (float) w / ratio, -1, 1, -1, 1);
                }
                break;
            case FIT_XY:
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, -1, 1);
                break;
            default:
        }

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        onChanged(width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        onDraw();

        GLES20.glUniformMatrix4fv(glMatrix, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(glSTMatrix, 1, false, mTransformMatrix, 0);

        GLES20.glEnableVertexAttribArray(glPosition);
        GLES20.glEnableVertexAttribArray(glCoordinate);
        mPos.position(0);
        mCoord.position(0);
        GLES20.glVertexAttribPointer(glPosition, 2, GLES20.GL_FLOAT, false, 0, mPos);
        GLES20.glVertexAttribPointer(glCoordinate, 2, GLES20.GL_FLOAT, false, 0, mCoord);

        if (mTextureId != NO_FILTER) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(TEXTURE_2D_BINDABLE, mTextureId);
            GLES20.glUniform1i(glTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(glPosition);
        GLES20.glDisableVertexAttribArray(glCoordinate);
        GLES20.glBindTexture(TEXTURE_2D_BINDABLE, 0);
    }

    public abstract void onDraw();

    public abstract void onCreated(int mProgram);

    public abstract void onChanged(int width, int height);
}
