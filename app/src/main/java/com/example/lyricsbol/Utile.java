package com.example.lyricsbol;


import android.content.ContentUris;
import android.net.Uri;


public class Utile {

 //-------------------------------------------------------------------------------------------------------
    public static String convertDuration(long duration){
        long minutes=(duration/1000)/60;
        long seconds=(duration/1000)%60;

        String converted= String.format("%d:%02d",minutes,seconds);//3:7
        return converted;
    }

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }
//----------------------------------------------------------



}
