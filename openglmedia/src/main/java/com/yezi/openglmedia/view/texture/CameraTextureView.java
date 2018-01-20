package com.yezi.openglmedia.view.texture;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import com.yezi.openglmedia.BuildConfig;
import com.yezi.openglmedia.filter.BeautyFilterLow;
import com.yezi.openglmedia.render.VideoRecordRender;
import com.yezi.openglmedia.render.VideoRender;
import com.yezi.openglmedia.render.listener.onStartRecordListener;
import com.yezi.openglmedia.render.listener.onStopRecordListener;
import com.yezi.openglmedia.utils.Constant;
import com.yezi.openglmedia.utils.camera.CameraInstance;
import com.yezi.openglmedia.utils.enums.FilterType;
import com.yezi.openglmedia.utils.enums.ScaleType;
import com.yezi.openglmedia.view.texture.base.BaseTextureView;

public class CameraTextureView extends BaseTextureView implements SurfaceTexture.OnFrameAvailableListener {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CameraTextureView";
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public CameraTextureView(Context context) {
        super(context);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startRecording(final String filePath, final onStartRecordListener listener) {
        ((VideoRecordRender) mBaseRender).startRecording(filePath, getEGLCore(), listener);
    }

    public void stopRecording(final onStopRecordListener listener) {
        ((VideoRecordRender) mBaseRender).stopRecording(listener);
    }

    @Override
    public void release() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBaseRender.release();
            }
        });
        ((VideoRecordRender) mBaseRender).stopRecording(null);
        CameraInstance.getInstance().releaseCamera();
    }

    public void destroy() {
        CameraInstance.getInstance().destroyCamera();
    }

    public void switchCamera() {
        if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        CameraInstance.getInstance().switchCamera();
    }

    public boolean isFrontCamera() {
        return CameraInstance.getInstance().isFrontCamera();
    }

    public synchronized boolean setFlashLightMode(String mode) {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, "当前设备不支持闪光灯!");
            return false;
        }
        return CameraInstance.getInstance().setFlashLightMode(mode);
    }

    //注意， focusAtPoint 会强制 focus mode 为 FOCUS_MODE_AUTO
    //如果有自定义的focus mode， 请在 AutoFocusCallback 里面重设成所需的focus mode。
    //x,y 取值范围: [0, 1]， 一般为 touchEventPosition / viewSize.
    public void focusAtPoint(float x, float y, Camera.AutoFocusCallback focusCallback) {
        CameraInstance.getInstance().focusAtPoint(y, 1.0f - x, focusCallback);
    }

    @Override
    protected void init() {
        mBaseRender = new VideoRecordRender();
        mBaseRender.setContext(getContext());
        mBaseRender.setScaleType(ScaleType.CENTER_CROP);
        mBaseRender.setFilter(new BeautyFilterLow(FilterType.VIDEO).setBeautyLevel(5));
        setRenderer(mBaseRender);

        ((VideoRecordRender) mBaseRender).setOnSurfaceCreatedListener(new VideoRender.onSurfaceCreatedListener() {
            @Override
            public void onSurfaceCreated() {
                ((VideoRecordRender) mBaseRender).getSurfaceTexture().setOnFrameAvailableListener(CameraTextureView.this);
                CameraInstance.getInstance().setCameraSize(Constant.CAMERA_SIZE_WIDTH, Constant.CAMERA_SIZE_HEIGHT,
                        false);
                CameraInstance.getInstance().openCamera(mCameraFacing);
                mBaseRender.setDataSize(CameraInstance.getInstance().getPreviewSize().height, CameraInstance.getInstance().getPreviewSize().width);
                CameraInstance.getInstance().startPreview(((VideoRecordRender) mBaseRender).getSurfaceTexture());
            }
        });
    }

    private int mFrameCount = 0;
    private long mLastTime = 0;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();

        if (DEBUG) {
            if (mLastTime == 0) {
                mLastTime = System.currentTimeMillis();
            }
            ++mFrameCount;
            if (System.currentTimeMillis() - mLastTime >= 1000) {
                Log.i(TAG, "相机帧率：" + mFrameCount);
                mFrameCount = 0;
                mLastTime = 0;
            }
        }
    }
}
