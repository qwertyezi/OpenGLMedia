package com.yezi.openglmedia.view.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.render.ImageRender;
import com.yezi.openglmedia.utils.enums.ScaleType;
import com.yezi.openglmedia.view.texture.base.BaseTextureView;

public class ImageTextureView extends BaseTextureView {

    public ImageTextureView(Context context) {
        super(context);
    }

    public ImageTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBitmap(final Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                ((ImageRender) mBaseRender).setBitmap(bitmap);
            }
        });
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
    }

    public void destroy() {
        ((ImageRender) mBaseRender).destroy();
    }
}
