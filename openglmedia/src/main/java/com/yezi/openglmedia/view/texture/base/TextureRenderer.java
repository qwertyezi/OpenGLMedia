package com.yezi.openglmedia.view.texture.base;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.yezi.openglmedia.BuildConfig;
import com.yezi.openglmedia.gles.EglCore;
import com.yezi.openglmedia.gles.WindowSurface;

import java.lang.ref.WeakReference;

public class TextureRenderer extends Thread {
    private static final String TAG = "TextureRenderer";
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private EglCore mEglCore;
    private GLSurfaceView.Renderer mRenderer;
    private WindowSurface mWindowSurface;
    private volatile RenderHandler mHandler;
    private Object mStartLock = new Object();
    private boolean mReady = false;
    private boolean mAvailable = false;
    private boolean mDrawAfterAvailable = false;

    public TextureRenderer() {
        super("TextureRenderer");
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new RenderHandler(this);
        synchronized (mStartLock) {
            mReady = true;
            mStartLock.notify();
        }
        mEglCore = new EglCore(null, 0);
        Looper.loop();
        releaseGl();
        mEglCore.release();

        synchronized (mStartLock) {
            mReady = false;
        }
    }

    private void releaseGl() {
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.makeNothingCurrent();
        }
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

    public synchronized void requestRender() {
        mHandler.sendRedraw();
    }

    private synchronized void draw() {
        if (mWindowSurface == null) {
            if (!mAvailable) {
                mDrawAfterAvailable = true;
            }
            return;
        }
        mRenderer.onDrawFrame(null);
        mWindowSurface.swapBuffers();
    }

    private void frameAvailable() {

    }

    public synchronized void queueEvent(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    private void shutdown() {
        Looper.myLooper().quitSafely();
    }

    private void surfaceDestroyed() {
        releaseGl();
    }

    private void surfaceChanged(int width, int height) {
        mRenderer.onSurfaceChanged(null, width, height);
    }

    private synchronized void surfaceAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
        mWindowSurface = new WindowSurface(mEglCore, surfaceTexture);
        mWindowSurface.makeCurrent();
        mRenderer.onSurfaceCreated(null, null);
        mRenderer.onSurfaceChanged(null, width, height);
        mAvailable = true;
        if (mDrawAfterAvailable) {
            draw();
        }
    }

    public EglCore getEGLCore() {
        return mEglCore;
    }

    public RenderHandler getHandler() {
        return mHandler;
    }

    public static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_REDRAW = 9;
        private static final int MSG_QUEUE_EVENT = 5;

        private WeakReference<TextureRenderer> mWeakRenderThread;

        public RenderHandler(TextureRenderer rt) {
            mWeakRenderThread = new WeakReference<TextureRenderer>(rt);
        }

        public void sendSurfaceAvailable(SurfaceTexture st, int width, int height) {
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE, width, height, st));
        }

        public void sendSurfaceChanged(int width,
                                       int height) {
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height));
        }

        public void sendSurfaceDestroyed() {
            sendMessage(obtainMessage(MSG_SURFACE_DESTROYED));
        }

        public void sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }

        public void sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
        }

        public void sendRedraw() {
            sendMessage(obtainMessage(MSG_REDRAW));
        }

        public void queueEvent(Runnable runnable) {
            sendMessage(obtainMessage(MSG_QUEUE_EVENT, runnable));
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            TextureRenderer renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                return;
            }

            switch (what) {
                case MSG_SURFACE_AVAILABLE:
                    renderThread.surfaceAvailable((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_DESTROYED:
                    renderThread.surfaceDestroyed();
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                case MSG_FRAME_AVAILABLE:
                    renderThread.frameAvailable();
                    break;
                case MSG_REDRAW:
                    renderThread.draw();
                    break;
                case MSG_QUEUE_EVENT:
                    renderThread.queueEvent((Runnable) msg.obj);
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }
}