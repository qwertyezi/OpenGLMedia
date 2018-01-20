package com.yezi.openglmedia.view.texture.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.render.BaseRender;
import com.yezi.openglmedia.render.listener.onDrawFrameListener;
import com.yezi.openglmedia.utils.BitmapUtils;
import com.yezi.openglmedia.utils.GL2Utils;
import com.yezi.openglmedia.utils.enums.ScaleType;

import java.nio.IntBuffer;

public class BaseTextureView extends CustomTextureView {

    protected BaseRender mBaseRender;

    public BaseTextureView(Context context) {
        this(context, null);
    }

    public BaseTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    protected void init() {

    }

    public void setDrawFrameListener(onDrawFrameListener listener) {
        mBaseRender.setDrawFrameListener(listener);
    }

    public interface onTakePictureListener {
        void onTakePicture();
    }

    public synchronized void takePicture(final String filePath, final onTakePictureListener listener) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                int imageWidth = getMeasuredWidth();
                int imageHeight = getMeasuredHeight();
                IntBuffer intBuffer = IntBuffer.allocate(imageWidth * imageHeight);
                GLES20.glReadPixels(0, 0, imageWidth, imageHeight, GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE, intBuffer);
                Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(GL2Utils.convertMirroredImage(intBuffer, imageWidth, imageHeight));
                BitmapUtils.saveBitmap(bitmap, filePath);
                if (listener != null) {
                    listener.onTakePicture();
                }
                intBuffer.clear();
            }
        });
    }

    public void setFilter(final BaseFilter filter) {
        if (filter == null) {
            return;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBaseRender.setFilter(filter);
            }
        });
    }

    public void setScaleType(ScaleType scaleType) {
        mBaseRender.setScaleType(scaleType);
    }

    public ScaleType getScaleType() {
        return mBaseRender.getScaleType();
    }

    public void release() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBaseRender.release();
            }
        });
    }
}
