package com.lhl.media.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


import com.lhl.media.IMedia;
import com.lhl.media.MediaListener;
import com.lhl.media.R;

import org.json.JSONObject;


public class VideoView extends SurfaceView implements SurfaceHolder.Callback, MediaListener {
    private static final String TAG = "VideoView";
    private static final int ANDROID = 0;
    private static final int IJK = 1;
    private static final int GOOGLE_EXO = 2;
    private SurfaceHolder mSurfaceHolder;
    private IMedia mMediaPlayer;
    private boolean isPlay;
    private IMedia.MediaType mediaType = IMedia.MediaType.ANDROID;
    private MediaListener listener;
    private String path;
    private boolean prepared = false;
    private long playTime = 0;

    public VideoView(Context context) {
        super(context);
        init(null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    private void init(AttributeSet attrs) {
        if (getContext() instanceof Activity)
            ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (attrs != null) {
            TypedArray a = getResources().obtainAttributes(attrs, R.styleable.VideoView);
            int type = a.getInt(R.styleable.VideoView_video_type, ANDROID);
            a.recycle();
            switch (type) {
                case IJK:
                    mediaType = IMedia.MediaType.IJK;
                    break;
                case GOOGLE_EXO:
                    mediaType = IMedia.MediaType.GOOGLE_EXO;
                    break;
                default:
                    mediaType = IMedia.MediaType.ANDROID;
                    break;
            }
        } else
            mediaType = IMedia.MediaType.ANDROID;

        getHolder().addCallback(this);
        mMediaPlayer = new IMedia.Build().setContext(getContext()).setMediaType(mediaType).build();
        mMediaPlayer.setMedialistener(this);
    }


    public void changePlayer(IMedia.MediaType mediaType) {
        if (mediaType == null || mediaType == this.mediaType)
            return;
        this.path = null;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlay())
                mMediaPlayer.stop();
            mMediaPlayer.destroy();
        }
        this.mediaType = mediaType;
        mMediaPlayer = new IMedia.Build().setContext(getContext()).setMediaType(this.mediaType).build();
        mMediaPlayer.setMedialistener(this);
        if (mSurfaceHolder != null)
            mMediaPlayer.setDisplay(mSurfaceHolder);
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekTo(long seek) {
        mMediaPlayer.seekTo(seek);
    }

    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    public void setMediaListener(MediaListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public synchronized void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, String.format("surfaceCreated:prepared:%s,isPlay:%s,%s", prepared, isPlay, mMediaPlayer.isPlay()));
        if (mSurfaceHolder != null)
            return;
        mSurfaceHolder = surfaceHolder;
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediaPlayer.setDisplay(mSurfaceHolder);
        if (prepared && isPlay && !mMediaPlayer.isPlay()) {
            if (mediaType == IMedia.MediaType.GOOGLE_EXO)
                mMediaPlayer.setPath(path);
            mMediaPlayer.play();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        mSurfaceHolder.setFixedSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        mSurfaceHolder = null;
        if (mMediaPlayer.isPlay())
            mMediaPlayer.pause();
        if (mediaType == IMedia.MediaType.GOOGLE_EXO)
            playTime = mMediaPlayer.getCurrentPosition();
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
    }

    public synchronized void setPath(final String path) {
        if (path == null || path.isEmpty() || path.equals(this.path))
            return;
        if (mMediaPlayer.isPlay())
            mMediaPlayer.pause();
        this.path = path;
        prepared = false;
        playTime = 0;
        mMediaPlayer.setPath(path);
    }


    public synchronized void play(String path) {
        if (path == null)
            return;
        setPath(path);
        isPlay = true;
    }

    public synchronized void pause() {
        isPlay = false;
        mMediaPlayer.pause();
    }

    public synchronized void resume() {
        if (prepared && isPlay)
            play();

    }

    public synchronized void play() {
        if (mMediaPlayer.isPlay())
            return;
        isPlay = true;
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (prepared && mSurfaceHolder != null && !mMediaPlayer.isPlay())
            mMediaPlayer.play();
    }

    @Override
    public void onPrepared() {
        Log.d(TAG, "????????????");
        prepared = true;
        if (isPlay && mSurfaceHolder != null && !mMediaPlayer.isPlay())
            mMediaPlayer.play();
    }

    @Override
    public void onCompletion() {
        Log.d(TAG, "????????????");
//        String path = this.path;
//        this.path = "";
//        play(path);
        if (listener != null)
            listener.onCompletion();
    }

    @Override
    public void onError(int what, int extra) {
        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.d(TAG, "????????????");
        } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.d(TAG, "???????????????");
        } else
            Log.d(TAG, String.format("??????what???%s", what));
        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
            Log.d(TAG, "?????????????????????????????????");
        } else if (extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            Log.d(TAG, "??????????????????????????????????????????????????????");
        } else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            Log.d(TAG, "????????????????????????");
        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            Log.d(TAG, "??????????????????");
        } else {
            Log.d(TAG, String.format("??????extra???%s", extra));
            if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                Log.d(TAG, "????????????");
                String path = this.path;
                this.path = "";
                setPath(path);
                return;
            }
        }

        if (listener != null)
            listener.onError(what, extra);
    }

    public void destroy() {
        if (mMediaPlayer != null)
            mMediaPlayer.destroy();
    }

    @Override
    public boolean onInfo(int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_UNKNOWN)
            Log.d(TAG, "onInfo???????????????");
        else if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            Log.d(TAG, "onInfo????????????????????????");
            if (playTime > 0) {
                mMediaPlayer.seekTo(playTime);
                playTime = 0;
            }
        } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING)
            Log.d(TAG, "onInfo?????????????????????????????????");
        else if (what == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING)
            Log.d(TAG, "onInfo???????????????");
        else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE)
            Log.d(TAG, "onInfo????????????????????????");
        else if (what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING || what == MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING) {
            Log.d(TAG, String.format("onInfo???%s????????????", what == MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING ? "??????" : "??????"));
//            String path = this.path;
//            this.path = "";
//            setPath(path);
        } else if (what == MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT)
            Log.d(TAG, "onInfo??????????????????????????????");
        else if (what == MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE)
            Log.d(TAG, "onInfo??????????????????");
        else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            Log.d(TAG, "onInfo???????????????????????????????????????");
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            Log.d(TAG, "onInfo?????????????????????????????????????????????");
        }
//        else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
//            Log.d(TAG, "onInfo?????????????????????????????????");
//        } else if (what == MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING || what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING) {
//            Log.d(TAG, String.format("onInfo???%s???????????????????????????", what == MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING ? "??????" : "??????"));
////            String path = this.path;
////            this.path = "";
////            setPath(path);
//        }
        else
            Log.d(TAG, "onInfo???????????????");
        if (listener != null)
            return listener.onInfo(what, extra);
        return true;
    }

    @Override
    public void onBufferingUpdate(int percent) {
        Log.d(TAG, String.format("onBufferingUpdate,percent:%d", percent));
        if (listener != null)
            listener.onBufferingUpdate(percent);
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, String.format("onVideoSizeChanged,width:%d,height:%d", width, height));
    }
}
