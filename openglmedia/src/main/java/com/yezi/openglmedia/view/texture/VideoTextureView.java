package com.yezi.openglmedia.view.texture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.yezi.openglmedia.BuildConfig;
import com.yezi.openglmedia.render.VideoRender;
import com.yezi.openglmedia.utils.enums.ScaleType;
import com.yezi.openglmedia.view.texture.base.BaseTextureView;

public class VideoTextureView extends BaseTextureView implements SurfaceTexture.OnFrameAvailableListener {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VideoTextureView";

    private String mUri;
    private MediaPlayer mMediaPlayer;
    private boolean mLoopPlay = false;
    private float mCurrentVolume;
    private boolean mHasInit = false;
    private boolean mHasVolume = true;
    private int mPlayStatus = -1;//-1初始化，0暂停，1播放

    public VideoTextureView(Context context) {
        this(context, null);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        mBaseRender = new VideoRender();
        mBaseRender.setContext(getContext());
        mBaseRender.setScaleType(ScaleType.CENTER_CROP);
        setRenderer(mBaseRender);
        getCurrentVolume();

        ((VideoRender) mBaseRender).setOnSurfaceCreatedListener(
                new VideoRender.onSurfaceCreatedListener() {
                    @Override
                    public void onSurfaceCreated() {
                        mHasInit = true;
                        if (mUri != null) {
                            if (mPlayStatus == 1) {
                                playVideo();
                            } else if (mPlayStatus == 0) {
                                pauseVideo();
                            }
                            mPlayStatus = -1;
                        }
                    }
                });
    }

    public void setLoopPlay(boolean loopPlay) {
        mLoopPlay = loopPlay;
    }

    public void setPlayUrl(String url) {
        mUri = url;
    }

    public synchronized void playVideo() {
        if (TextUtils.isEmpty(mUri) || !mHasInit) {
            mPlayStatus = 1;
            return;
        }
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(mLoopPlay);
            ((VideoRender) mBaseRender).getSurfaceTexture().setOnFrameAvailableListener(VideoTextureView.this);
            mMediaPlayer.setSurface(new Surface(((VideoRender) mBaseRender).getSurfaceTexture()));
            try {
                mMediaPlayer.setDataSource(getContext(), Uri.parse(mUri));
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setVolume(mHasVolume ? mCurrentVolume : 0.0f);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mBaseRender.setDataSize(mp.getVideoWidth(), mp.getVideoHeight());

                    mp.start();
                }
            });
        } else {
            mMediaPlayer.start();
        }
    }

    public synchronized void pauseVideo() {
        if (TextUtils.isEmpty(mUri) || !mHasInit) {
            mPlayStatus = 0;
            return;
        }
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(mLoopPlay);
            ((VideoRender) mBaseRender).getSurfaceTexture().setOnFrameAvailableListener(VideoTextureView.this);
            mMediaPlayer.setSurface(new Surface(((VideoRender) mBaseRender).getSurfaceTexture()));
            try {
                mMediaPlayer.setDataSource(getContext(), Uri.parse(mUri));
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setVolume(mHasVolume ? mCurrentVolume : 0.0f);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mBaseRender.setDataSize(mp.getVideoWidth(), mp.getVideoHeight());

                    mp.start();
                    pauseVideo();
                }
            });
        }
    }

    private void getCurrentVolume() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = volume / (float) maxVolume;
    }

    public void setVolume(float volume) {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setVolume(volume, volume);
    }

    public void startVolume() {
        setVolume(mCurrentVolume);
        mHasVolume = true;
    }

    public void stopVolume() {
        setVolume(0.0f);
        mHasVolume = false;
    }

    @Override
    public void release() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {

                    mMediaPlayer.setSurface(null);
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                mHasInit = false;
                mBaseRender.release();
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
                Log.i(TAG, "视频帧率：" + mFrameCount);
                mFrameCount = 0;
                mLastTime = 0;
            }
        }
    }
}
