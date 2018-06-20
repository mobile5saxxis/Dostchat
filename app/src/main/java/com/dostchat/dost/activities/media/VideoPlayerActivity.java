package com.dostchat.dost.activities.media;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dostchat.dost.R;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.Files.FilesManager;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 10/4/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {

    MediaPlayer player;
    VideoControllerView controller;
    private File videoID = null;
    @BindView(R.id.progress_bar_video)
    ProgressBar mProgressBar;
    @BindView(R.id.videoHolder)
    ImageView imageHolder;
    @BindView(R.id.videoSurface)
    SurfaceView videoSurface;
    @BindView(R.id.videoSurfaceContainer)
    FrameLayout videoSurfaceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHelper.isAndroid5())
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorBlack));
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("Identifier")) {
            boolean isSent = getIntent().getExtras().getBoolean("isSent");
            if (isSent) {
                videoID = FilesManager.getFileVideoSent(this,getIntent().getExtras().getString("Identifier"));
            } else {
                videoID = FilesManager.getFileVideo(this,getIntent().getExtras().getString("Identifier"));
            }

        }
        setupProgressBar();
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);


        player = new MediaPlayer();
        controller = new VideoControllerView(this);
        if (videoID != null) {
            imageHolder.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            videoSurface.setVisibility(View.VISIBLE);
            try {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(this, Uri.fromFile(videoID));
                player.setOnPreparedListener(this);
            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        } else {
            imageHolder.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            videoSurface.setVisibility(View.GONE);
        }

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.backBtn)
    void back() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.shareBtn)
    void ShareContent() {
        AppHelper.shareIntent(videoID, this, null, AppConstants.SENT_VIDEOS);
        finish();
    }

    /**
     * method to setup the progress bar
     */
    public void setupProgressBar() {
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView(videoSurfaceContainer);
        player.start();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
