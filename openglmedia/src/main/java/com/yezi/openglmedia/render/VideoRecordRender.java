package com.yezi.openglmedia.render;

import android.opengl.EGLSurface;
import android.util.Log;

import com.yezi.openglmedia.BuildConfig;
import com.yezi.openglmedia.gles.EglCore;
import com.yezi.openglmedia.recorder.MediaAudioEncoder;
import com.yezi.openglmedia.recorder.MediaEncoder;
import com.yezi.openglmedia.recorder.MediaMuxerWrapper;
import com.yezi.openglmedia.recorder.MediaVideoEncoder;
import com.yezi.openglmedia.recorder.RecordRenderer;
import com.yezi.openglmedia.render.listener.onStartRecordListener;
import com.yezi.openglmedia.render.listener.onStopRecordListener;
import com.yezi.openglmedia.utils.Constant;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class VideoRecordRender extends VideoRender {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VideoRecordRender";

    private MediaMuxerWrapper mMuxer;
    private boolean mIsRecording = false;
    private RecordRenderer mRecordRenderer;

    public VideoRecordRender() {
        super();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        if (mIsRecording && mRecordRenderer != null) {
            mRecordRenderer.getHandler().sendRedraw(mTransformMatrix);
            mMuxer.frameAvailableSoon();
        }
    }

    public void startRecording(String filePath, EglCore eglCore, onStartRecordListener listener) {
        if (mIsRecording) {
            return;
        }
        if (DEBUG) Log.v(TAG, "startRecording:");
        try {
            mMuxer = new MediaMuxerWrapper(filePath);
            MediaVideoEncoder videoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
                    Constant.VIDEO_ENCODER_WIDTH, Constant.VIDEO_ENCODER_HEIGHT);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            mMuxer.prepare();
            EglCore newEglCore = new EglCore(eglCore.mEGLContext, EglCore.FLAG_TRY_GLES3);
            EGLSurface eglSurface = newEglCore.createWindowSurface(videoEncoder.getSurface());
            mRecordRenderer = new RecordRenderer(mFilter, newEglCore, eglSurface);
            mRecordRenderer.waitUntilReady();
            mMuxer.startRecording();
            mIsRecording = true;
            if (listener != null) {
                listener.onStartRecord();
            }
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    public void stopRecording(onStopRecordListener listener) {
        if (!mIsRecording) {
            if (listener != null) {
                listener.onStopRecord();
            }
            return;
        }
        if (DEBUG) Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }
        if (mRecordRenderer != null) {
            mRecordRenderer.getHandler().stopRecord();
            mRecordRenderer = null;
        }
        if (listener != null) {
            listener.onStopRecord();
        }
        mIsRecording = false;
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
        }
    };
}
