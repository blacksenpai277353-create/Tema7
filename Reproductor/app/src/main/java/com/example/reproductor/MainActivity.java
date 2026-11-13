package com.example.reproductor;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public ExoPlayer player;
    public Button play;
    public SeekBar progreso;
    public TextView tiempoActual;
    public TextView tiempoTotal;
    private boolean arrastrando;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateProgressTask = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying() && !arrastrando) {
                long currentPos = player.getCurrentPosition();
                progreso.setProgress((int) currentPos);
                tiempoActual.setText(formatear(currentPos));
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        play = findViewById(R.id.Botonpausar);
        progreso = findViewById(R.id.seekBar);
        tiempoActual = findViewById(R.id.tvTiempoActual);
        tiempoTotal = findViewById(R.id.tvTiempoTotal);

        player = new ExoPlayer.Builder(this).build();
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.manigga);
        player.setMediaItem(MediaItem.fromUri(uri));

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        progreso.setMax((int) duration);
                        tiempoTotal.setText(formatear(duration));
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    play.setText(R.string.play);
                    player.seekTo(0);
                    progreso.setProgress(0);
                    tiempoActual.setText(formatear(0));
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    play.setText(R.string.pausa);
                    handler.post(updateProgressTask);
                } else {
                    play.setText(R.string.play);
                    handler.removeCallbacks(updateProgressTask);
                }
            }
        });

        player.prepare();

        play.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        progreso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar barra) {
                arrastrando = true;
            }

            @Override
            public void onProgressChanged(SeekBar barra, int progress, boolean delUsuario) {
                if (delUsuario) {
                    tiempoActual.setText(formatear(progress));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar barra) {
                if (player != null) {
                    player.seekTo(barra.getProgress());
                }
                arrastrando = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateProgressTask);
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private String formatear(long ms) {
        long safeMs = Math.max(0, ms);
        long segundos = safeMs / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", segundos / 60, segundos % 60);
    }
}