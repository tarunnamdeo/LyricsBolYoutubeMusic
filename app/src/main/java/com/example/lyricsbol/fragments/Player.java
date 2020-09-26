package com.example.lyricsbol.fragments;


import android.app.DownloadManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lyricsbol.R;
import com.example.lyricsbol.Utile;

import java.io.IOException;


public class Player extends Fragment {
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    long currentlength;
    Button playpause;
    TextView stime, etime;
    private Button download;

    private final static String TAG_FRAGMENT = "Player_Song_FRAGMENT";

    public Player() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_player, container, false);
        insitalize(view);
        Bundle bundle = this.getArguments();
        String myValue = bundle.getString("urlmy");
        String myTitle = bundle.getString("title");
        Log.d("sonng", myTitle);
        Uri uri = Uri.parse(myValue);
        playermethod(uri);
        downloadplaysong(uri,myTitle);


        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("backtst", "keyCode: " + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i("backtst", "onKey Back listener is working!!!");
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void downloadplaysong(Uri uri, String myTitle) {
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle(myTitle);

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, myTitle);

                DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                Toast.makeText(getActivity(), "Download Start Showing in Notification or Downloads", Toast.LENGTH_SHORT).show();
                manager.enqueue(request);
            }
        });
    }

    private void insitalize(View view) {
        seekBar = view.findViewById(R.id.musicseek);
        playpause = view.findViewById(R.id.playpause);
        stime = view.findViewById(R.id.starttime);
        etime = view.findViewById(R.id.endtime);
        download=view.findViewById(R.id.download_click);

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


        currentlength = mediaPlayer.getDuration();
        stime.setText(Utile.convertDuration(currentlength));
        Log.d("length", String.valueOf(currentlength));
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        double seekbarsize = seekBar.getMax();
                        double remain = seekbarsize / 100.0;
                        double val = remain * percent;
                        if (percent < currentlength) {
                            seekBar.setSecondaryProgress((int) val);
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
                    try {
                        seekBar.setMax((int) currentlength / 1000);
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        int time = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
                        etime.setText(Utile.convertDuration(time));
                        handler.postDelayed(this, 1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }



                }
            });
        }
    }




}
