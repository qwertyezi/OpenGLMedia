package com.yezi.openglmedia.recorder;

import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.yezi.openglmedia.filter.BaseFilter;
import com.yezi.openglmedia.filter.BeautyFilterLow;
import com.yezi.openglmedia.gles.EglCore;
import com.yezi.openglmedia.utils.enums.ScaleType;

import java.lang.ref.WeakReference;

public class RecordRenderer extends Thread {

    private EglCore mEglCore;
    private BaseFilter mVideoFilter;
    private BaseFilter mInputVideoFilter;
    private EGLSurface mEglSurface;
    private RecordHandler mRecordHandler;
    private Object mStartLock = new Object();
    private boolean mReady = false;

    public RecordRenderer(BaseFilter videoFilter, EglCore eglCore, EGLSurface eglSurface) {
        mInputVideoFilter = videoFilter;
        mEglCore = eglCore;
        mEglSurface = eglSurface;

        start();
    }

    private synchronized void draw(float[] matrix) {
        if (mVideoFilter == null || mEglCore == null || mEglSurface == null) {
            return;
        }
        mVideoFilter.setTransformMatrix(matrix);
        mVideoFilter.onDrawFrame();
        mEglCore.swapBuffers(mEglSurface);
    }

    private void stopRecord() {
        if (mVideoFilter != null) {
            mVideoFilter.release();
            mVideoFilter = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        if (mEglSurface != null) {
            mEglSurface = null;
        }
        Looper.myLooper().quitSafely();
    }

    @Override
    public void run() {
        Looper.prepare();

        mEglCore.makeCurrent(mEglSurface);
        initFilter();
        mRecordHandler = new RecordHandler(this);
        synchronized (mStartLock) {
            mReady = true;
            mStartLock.notify();
        }

        Looper.loop();

        synchronized (mStartLock) {
            mReady = false;
        }
    }

    private void initFilter() {
        mVideoFilter = new BeautyFilterLow();
        ((BeautyFilterLow) mVideoFilter).setBeautyLevel(5);
        mVideoFilter.setContext(mInputVideoFilter.getContext());
        mVideoFilter.setScaleType(ScaleType.CENTER_CROP);
        mVideoFilter.setFilterType(mInputVideoFilter.getFilterType());
        mVideoFilter.setTextureId(mInputVideoFilter.getTextureId());
        mVideoFilter.setDataSize(mInputVideoFilter.getDataWidth(), mInputVideoFilter.getDataHeight());
        mVideoFilter.onSurfaceCreated();
        mVideoFilter.onSurfaceChanged(mInputVideoFilter.getViewWidth(), mInputVideoFilter.getViewHeight());
    }

    public void waitUntilReady() {
        synchronized (mStartLock) {
            while (!mReady) {
                try {
                    mStartLock.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    public RecordHandler getHandler() {
        return mRecordHandler;
    }

    public static class RecordHandler extends Handler {
        private static final int MSG_REDRAW = 0;
        private static final int MSG_STOP = 1;

        private WeakReference<RecordRenderer> mWeakRecordThread;

        public RecordHandler(RecordRenderer rt) {
            mWeakRecordThread = new WeakReference<>(rt);
        }

        public void sendRedraw(float[] matrix) {
            sendMessage(obtainMessage(MSG_REDRAW, matrix));
        }

        public void stopRecord() {
            sendMessage(obtainMessage(MSG_STOP));
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RecordRenderer recordThread = mWeakRecordThread.get();
            if (recordThread == null) {
                return;
            }

            switch (what) {
                case MSG_REDRAW:
                    recordThread.draw((float[]) msg.obj);
                    break;
                case MSG_STOP:
                    recordThread.stopRecord();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }


}
