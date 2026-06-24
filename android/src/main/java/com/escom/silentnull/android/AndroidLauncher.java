package com.escom.silentnull.android;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.escom.silentnull.SilentNullGame;
import com.escom.silentnull.video.IVideoPlayer;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication implements IVideoPlayer {
    private FrameLayout layout;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new FrameLayout(this);

        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        View gameView = initializeForView(new SilentNullGame(this), configuration);
        layout.addView(gameView);

        setContentView(layout);
    }

    @Override
    public void playVideo(final String path, final Function0<Unit> onFinished) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (videoView == null) {
                        videoView = new VideoView(AndroidLauncher.this);
                        layout.addView(videoView, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                    }

                    videoView.setVisibility(View.VISIBLE);
                    videoView.bringToFront(); // Asegurar que esté encima del juego

                    String fileName = path.toLowerCase().replace(".mp4", "");
                    int resId = getResources().getIdentifier(fileName, "raw", getPackageName());

                    if (resId != 0) {
                        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + resId));
                    } else {
                        Log.e("AndroidLauncher", "Video NOT FOUND in res/raw: " + fileName);
                        finishVideo(onFinished);
                        return;
                    }

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            videoView.start();
                        }
                    });

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            finishVideo(onFinished);
                        }
                    });

                    videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Log.e("AndroidLauncher", "Video Playback Error: " + what + ", " + extra);
                            finishVideo(onFinished);
                            return true;
                        }
                    });

                } catch (Exception e) {
                    Log.e("AndroidLauncher", "Error starting video", e);
                    finishVideo(onFinished);
                }
            }
        });
    }

    private void finishVideo(final Function0<Unit> onFinished) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoView != null) {
                    videoView.stopPlayback();
                    videoView.setVisibility(View.GONE);
                }

                // Forzar el callback en el hilo de renderizado de LibGDX
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (onFinished != null) {
                            onFinished.invoke();
                        }
                    }
                });
            }
        });
    }
}
