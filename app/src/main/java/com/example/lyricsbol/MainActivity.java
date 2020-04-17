package com.example.lyricsbol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.lyricsbol.fragments.Lyricsbol;
import com.example.lyricsbol.fragments.TarunApp;
import com.example.lyricsbol.fragments.YoutubeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        haveStoragePermission();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, new Lyricsbol()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selfragment = null;

            switch (item.getItemId()) {
                case R.id.lyrics:
                    selfragment = new Lyricsbol();
                    break;
                case R.id.youtube:
                    selfragment = new YoutubeFragment();
                    break;
                case R.id.tarunapp:
                    selfragment = new TarunApp();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, selfragment).commit();

            return true;
        }
    };


    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        boolean handled = false;

        for (Fragment f : fragmentList) {
            if (f instanceof Lyricsbol) {
                handled = ((Lyricsbol) f).onBackPressed();

                if (handled) {
                    break;
                }
            }


        }

        for (Fragment f : fragmentList) {
            if (f instanceof TarunApp) {
                handled = ((TarunApp) f).onBackPressed();

                if (handled) {
                    break;
                }
            }


        }
        if (!handled) {
                super.onBackPressed();
            }

    }



    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }

}

