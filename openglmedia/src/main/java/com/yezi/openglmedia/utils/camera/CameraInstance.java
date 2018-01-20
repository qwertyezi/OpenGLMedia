package com.yezi.openglmedia.utils.camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;

import com.yezi.openglmedia.BuildConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraInstance {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CameraInstance";

    private Camera mCamera = null;
    private int mCameraID = 0;
    private int mRotation = 90;
    private int mWidth = 1280, mHeight = 720;
    private boolean mIsBigger;

    private static CameraInstance sInstance;
    private SurfaceTexture mSurfaceTexture;

    public synchronized static CameraInstance getInstance() {
        if (sInstance == null) {
            sInstance = new CameraInstance();
        }
        return sInstance;
    }

    public void setCameraSize(int width, int height, boolean isBigger) {
        mWidth = height;
        mHeight = width;
        mIsBigger = isBigger;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean openCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(mCameraID);
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    public boolean openCamera(int id) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(id);
                mCameraID = id;
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void destroyCamera() {
        releaseCamera();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    public void setRotation(int rotation) {
        mRotation = rotation;
    }

    public void resumeCamera() {
        openCamera();
    }

    public void resumeCamera(int id) {
        openCamera(id);
    }

    public void setParameters(Parameters parameters) {
        mCamera.setParameters(parameters);
    }

    public Parameters getParameters() {
        if (mCamera != null)
            mCamera.getParameters();
        return null;
    }

    public void switchCamera() {
        releaseCamera();
        mCameraID = mCameraID == 0 ? 1 : 0;
        openCamera(mCameraID);
        startPreview(mSurfaceTexture);
    }

    private void setDefaultParameters() {
        Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(parameters);
        setCameraSize();
        setCameraDisplayOrientation();
    }

    public synchronized void setFocusMode(String focusMode) {
        if(mCamera == null)
            return;

        Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if(focusModes.contains(focusMode)){
            parameters.setFocusMode(focusMode);
        }
        mCamera.setParameters(parameters);
    }

    public void focusAtPoint(float x, float y, final Camera.AutoFocusCallback callback) {
        focusAtPoint(x, y, 0.2f, callback);
    }

    public synchronized void focusAtPoint(float x, float y, float radius, final Camera.AutoFocusCallback callback) {
        if(mCamera == null) {
            return;
        }

        Parameters parameters = mCamera.getParameters();

        if(parameters.getMaxNumMeteringAreas() > 0) {

            int focusRadius = (int) (radius * 1000.0f);
            int left = (int) (x * 2000.0f - 1000.0f) - focusRadius;
            int top = (int) (y * 2000.0f - 1000.0f) - focusRadius;

            Rect focusArea = new Rect();
            focusArea.left = Math.max(left, -1000);
            focusArea.top = Math.max(top, -1000);
            focusArea.right = Math.min(left + focusRadius, 1000);
            focusArea.bottom = Math.min(top + focusRadius, 1000);
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(focusArea, 800));

            try {
                mCamera.cancelAutoFocus();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                parameters.setFocusAreas(meteringAreas);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(callback);
            } catch (Exception e) {
            }
        } else {
            try {
                mCamera.autoFocus(callback);
            } catch (Exception e) {
            }
        }

    }


    // 参数为
    //    Camera.Parameters.FLASH_MODE_AUTO;
    //    Camera.Parameters.FLASH_MODE_OFF;
    //    Camera.Parameters.FLASH_MODE_ON;
    //    Camera.Parameters.FLASH_MODE_RED_EYE
    //    Camera.Parameters.FLASH_MODE_TORCH 等
    public synchronized boolean setFlashLightMode(String mode) {
        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters == null || mCameraID == 1) {
            return false;
        }

        try {

            if (!parameters.getSupportedFlashModes().contains(mode)) {
                Log.e(TAG, "Invalid Flash Light Mode!!!");
                return false;
            }

            parameters.setFlashMode(mode);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Log.e(TAG, "修改闪光灯状态失败, 请检查是否正在使用前置摄像头?");
            return false;
        }

        return true;
    }

    public void setCameraSize() {
        if (mCamera == null || mWidth == 0 || mHeight == 0) {
            return;
        }

        Parameters parameters = mCamera.getParameters();

        List<Size> picSizes = parameters.getSupportedPictureSizes();
        List<Size> preSizes = parameters.getSupportedPreviewSizes();
        Size picSize = null;
        Size preSize = null;

        if (mIsBigger) {
            Collections.sort(picSizes, CameraUtils.comparatorBigger);
            for (Size sz : picSizes) {
                if (picSize == null || (sz.width >= mWidth && sz.height >= mHeight)) {
                    picSize = sz;
                }
            }

            Collections.sort(preSizes, CameraUtils.comparatorBigger);
            for (Size sz : preSizes) {
                if (preSize == null || (sz.width >= mWidth && sz.height >= mHeight)) {
                    preSize = sz;
                }
            }
        } else {
            Collections.sort(picSizes, CameraUtils.comparatorSmaller);
            for (Size sz : picSizes) {
                if (picSize == null || (sz.width <= mWidth && sz.height <= mHeight)) {
                    picSize = sz;
                }
            }

            Collections.sort(preSizes, CameraUtils.comparatorSmaller);
            for (Size sz : preSizes) {
                if (preSize == null || (sz.width <= mWidth && sz.height <= mHeight)) {
                    preSize = sz;
                }
            }
        }

        try {
            if (picSize == null) {
                parameters.setPictureSize(mWidth, mHeight);
            } else {
                parameters.setPictureSize(picSize.width, picSize.height);
            }
            if (preSize == null) {
                parameters.setPreviewSize(mWidth, mHeight);
            } else {
                parameters.setPreviewSize(preSize.width, preSize.height);
            }
            if (DEBUG) {
                Log.i(TAG, "PictureSize:" + picSize.width + "," + picSize.height);
                Log.i(TAG, "PreviewSize:" + preSize.width + "," + preSize.height);
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    public boolean isFrontCamera() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        return info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    public Size getPreviewSize() {
        return mCamera.getParameters().getPreviewSize();
    }

    private Size getPictureSize() {
        return mCamera.getParameters().getPictureSize();
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera != null)
            try {
                mCamera.setPreviewTexture(surfaceTexture);
                mSurfaceTexture = surfaceTexture;
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void startPreview() {
        if (mCamera != null)
            mCamera.startPreview();
    }

    public void stopPreview() {
        mCamera.stopPreview();
    }

    public void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawCallback,
                            Camera.PictureCallback jpegCallback) {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }
}