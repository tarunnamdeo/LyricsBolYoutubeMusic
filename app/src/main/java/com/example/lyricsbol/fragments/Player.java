package com.example.lyricsbol.fragments;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.lyricsbol.R;
import com.example.lyricsbol.Utile;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class Player extends Fragment {
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    long currentlength;
    Button playpause;
    TextView stime,etime;

    public Player() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_player, container, false);
        insitalize(view);
        Bundle bundle = this.getArguments();
        String myValue = bundle.getString("urlmy");
        Log.d("sonng",myValue);
        Uri uri = Uri.parse(myValue);
        playermethod(uri);
        // Inflate the layout for this fragment
        return view;
    }

    private void insitalize(View view) {
        seekBar = view.findViewById(R.id.musicseek);
        playpause= view.findViewById(R.id.playpause);
        stime= view.findViewById(R.id.starttime);
        etime= view.findViewById(R.id.endtime);

        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playpause.setText(">");
                } else {
                    mediaPlayer.start();
                    playpause.setText("||");
                }
            }
        });

    }


    private void playermethod(Uri uri) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // mediaPlayer.start();

        currentlength = mediaPlayer.getDuration();
        stime.setText(Utile.convertDuration(currentlength));
        // mediaPlayer.reset();
        Log.d("length", String.valueOf(currentlength));
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        double seekbarsize=seekBar.getMax();
                        double remain=seekbarsize/100.0;
                        double val=remain*percent;
                        if (percent<currentlength) {
                            seekBar.setSecondaryProgress((int)val);
                        }


                    }
                });
                toggleplay(mp);


            }
        });

        handleSeekbar();
    }


    private void handleSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void toggleplay(MediaPlayer mp) {

        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        } else {
            mp.start();
            final Handler handler = new Handler();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax((int) currentlength / 1000);
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    int time=mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition();
                    etime.setText(Utile.convertDuration(time));
                    // etime.setText(Utile.convertDuration((long) mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 1000);


    }
});
        }
    }



}
