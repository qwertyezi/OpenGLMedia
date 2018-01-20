package com.yezi.openglmedia.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.render.ImageRender;
import com.yezi.openglmedia.utils.enums.ScaleType;


public class ImageGLSurfaceView extends BaseGLSurfaceView {

    public ImageGLSurfaceView(Context context) {
        super(context);
    }

    public ImageGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBitmap(final Bitmap bitmap) {
        if(bitmap==null) {
            return;
        }
        ((ImageRender) mBaseRender).setBitmap(bitmap);
        requestRender();
    }

    @Override
    public void setFilter(final BaseFilter filter) {
        if (filter == null) {
            return;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBaseRender.setFilter(filter);
                requestRender();
            }
        });
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(scaleType);
        requestRender();
    }

    @Override
    protected void init() {
        mBaseRender = new ImageRender();
        mBaseRender.setContext(getContext());
        mBaseRender.setScaleType(ScaleType.CENTER_CROP);
        setRenderer(mBaseRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void destroy() {
        ((ImageRender) mBaseRender).destroy();
    }
}
