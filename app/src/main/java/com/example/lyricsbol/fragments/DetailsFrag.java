package com.example.lyricsbol.fragments;


import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lyricsbol.R;
import com.example.lyricsbol.Utilitys;
import com.example.lyricsbol.VideoPlayActivity;
import com.example.lyricsbol.interfaces.YoutubeDataModel;
import com.example.lyricsbol.lib.VideoMeta;
import com.example.lyricsbol.lib.YouTubeExtractor;
import com.example.lyricsbol.lib.YouTubeFormate;
import com.example.lyricsbol.lib.YtFile;
import com.example.lyricsbol.libnav.ExtractorException;
import com.example.lyricsbol.libnav.YoutubeStreamExtractor;
import com.example.lyricsbol.model.YTMedia;
import com.example.lyricsbol.model.YTSubtitles;
import com.example.lyricsbol.model.YoutubeMeta;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFrag extends Fragment implements View.OnClickListener {

    private YoutubeDataModel youtubeDataModel = null;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;
    TextView textViewName;
    TextView textViewDes;
    TextView textViewDate;
    TextView textViewIcon;
    String youtubeLink;
    ImageView imageViewIcon;
    String filr;
    private ProgressBar mainProgressBar;
    private LinearLayout mainLayout;
    private List<YtFragmentedVideo> formatsToShowList;
    private static final int ITAG_FOR_AUDIO = 140;
    private long DownlodeID;
    private static final Pattern ARTIST_TITLE_PATTERN =
            Pattern.compile("(.+?)(\\s*?)-(\\s*?)(\"|)(\\S(.+?))\\s*?([&\\*+,-/:;<=>@_\\|]+?\\s*?|)(\\z|\"|\\(|\\[|lyric|official)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private Downloadereciver downloadereciver;


    SparseArray<YtFile> ytFileSec = new SparseArray<>();

    public DetailsFrag() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        insialize(view);

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        downloadereciver = new Downloadereciver();
        try {
            getActivity().registerReceiver(downloadereciver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }


        youtubeDataModel = getArguments().getParcelable(YoutubeDataModel.class.toString());

        youtubeLink = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());

        textViewName = view.findViewById(R.id.textViewName1);
        textViewDes = view.findViewById(R.id.textViewDes1);
        imageViewIcon = view.findViewById(R.id.imageView1);
        textViewDate = view.findViewById(R.id.textViewDate1);
        mainProgressBar = view.findViewById(R.id.prgrBar);
        mainLayout = view.findViewById(R.id.main_layout);

        textViewName.setText(youtubeDataModel.getTitle());
        textViewDes.setText(youtubeDataModel.getDescription());
        textViewDate.setText(youtubeDataModel.getPublishedAt());

        try {


            if (youtubeDataModel.getThumbnail() != null) {
                if (youtubeDataModel.getThumbnail().startsWith("http")) {
                    Picasso.get()
                            .load(youtubeDataModel.getThumbnail())
                            .into(imageViewIcon);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!checkPermissionForReadExtertalStorage()) {
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return view;
    }

    @Override
    public void onPause() {
        try {
            getActivity().unregisterReceiver(downloadereciver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    class Downloadereciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long broadcastdownid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (broadcastdownid == DownlodeID) {
                Toast.makeText(context, "ONRECEVER", Toast.LENGTH_SHORT).show();
                Bundle extras = intent.getExtras();
                DownloadManager.Query q = new DownloadManager.Query();
                long downloadId = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                q.setFilterById(downloadId);
                Cursor c = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).query(q);
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        String inPath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        String dlTitle = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        c.close();
                        DownloadStatus dlStatus = Utilitys.getMultiFileDlStatus(context, downloadId, inPath);
                        if (dlStatus != null && dlStatus.readyToMerge) {
                            if (!dlStatus.hasVideo) {
                                String artist = null;
                                String title = null;
                                Matcher mat = ARTIST_TITLE_PATTERN.matcher(dlTitle);
                                if (mat.find()) {
                                    artist = mat.group(1);
                                    title = mat.group(5);
                                }
                                Utilitys.convertM4a(inPath, title, artist);
                                Utilitys.scanFile(inPath, context);
                            } else {
                                if (inPath.endsWith(".mp4")) {
                                    Utilitys.mergeMp4(dlStatus.otherFilePath, inPath);
                                } else if (inPath.endsWith(".m4a")) {
                                    Utilitys.mergeMp4(inPath, dlStatus.otherFilePath);
                                }
                            }
                        }
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Utilitys.removeTempOnFailure(context, downloadId);
                    }
                }


                //----------------------
                if (getDownloadStatus() == DownloadManager.STATUS_SUCCESSFUL) {
                    Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                    Log.d("passs", "Downlode complete");
                } else {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void insialize(View view) {
        ImageView imageView = view.findViewById(R.id.imageplay);
        imageView.setOnClickListener(this);
        ImageView imageback = view.findViewById(R.id.img_left_header);
        imageback.setOnClickListener(this);
        TextView play = view.findViewById(R.id.plauy);
        play.setOnClickListener(this);
        TextView downlode = view.findViewById(R.id.downloade);
        downlode.setOnClickListener(this);
    }


    public void PlayAudio1(View view) {
        final String videoID = youtubeDataModel.getTitle();
        String link = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());
        mainProgressBar.setVisibility(View.VISIBLE);

        if (view == null && Intent.ACTION_SEND.equals(getActivity().getIntent().getAction())
                && getActivity().getIntent().getType() != null && "text/plain".equals(getActivity().getIntent().getType())) {

            String ytLink = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
                youtubeLink = ytLink;
                // We have a valid link
                mygetYoutubeDownloadUrl(youtubeLink);
            } else {
                Toast.makeText(getContext(), "Erorrrrr", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        } else if (view != null && youtubeLink != null) {
            mygetYoutubeDownloadUrl(youtubeLink);
        } else {
            getActivity().finish();
        }

    }

    private void mygetYoutubeDownloadUrl(String youtubeLink) {
        try {


            new YouTubeExtractor(getActivity()) {

                @Override
                public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                    mainProgressBar.setVisibility(View.GONE);

                    if (ytFiles == null) {
                        Toast.makeText(getContext(), "No Url Found Try another Video", Toast.LENGTH_SHORT).show();
                        mainProgressBar.setVisibility(View.VISIBLE);
                        secondextraction();
                        return;
                    }
                    for (int i = 0, itag; i < ytFiles.size(); i++) {
                        itag = ytFiles.keyAt(i);
                        YtFile ytFile = ytFiles.get(itag);

                        if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                            myaddButtonToMainLayout(vMeta.getTitle(), ytFile);
                        }
                    }
                }
            }.extract(youtubeLink, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void secondextraction() {

        new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner() {
            @Override
            public void onExtractionGoesWrong(ExtractorException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, List<YTSubtitles> subList, YoutubeMeta meta) {
                mainProgressBar.setVisibility(View.GONE);
                for (YTMedia media : adativeStream) {
                    if (media.isVideo()) {
                    } else {

                        YtFile newFile = new YtFile(YouTubeFormate.FORMAT_MAP.get(media.getItag()), media.getUrl());
                        ytFileSec.put(media.getItag(), newFile);


                    }
                }

                for (int i = 0, itag; i < ytFileSec.size(); i++) {
                    itag = ytFileSec.keyAt(i);
                    YtFile ytFile = ytFileSec.get(itag);

                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        myaddButtonToMainLayout(meta.getTitle(), ytFile);
                    }
                }


            }
        }).useDefaultLogin().Extract(youtubeLink);


    }

    private void myaddButtonToMainLayout(final String videoTitle, final YtFile ytfile) {

        String btnText;
        if (ytfile.getFormat().getAudioBitrate() != -1) {

            btnText = "Audio " +
                    ytfile.getFormat().getAudioBitrate() + " kbit/s";

        } else {
            btnText = "No Audio";
        }

        if (btnText.equals("No Audio")) {
            Log.d("noaudio", "noaudio");
        } else {


            Button btn = new Button(getContext());
            btn.setText(btnText);
            Log.d("btnn", btnText);

            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String filename;
                    if (videoTitle.length() > 55) {
                        filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
                    } else {
                        filename = videoTitle + "." + ytfile.getFormat().getExt();
                    }
                    filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                    Log.d("urldownloadsong", ytfile.getUrl());
                    mydownloadFromUrl(ytfile.getUrl(), videoTitle, filename);
                }
            });
            mainLayout.addView(btn);
        }
    }

    private void mydownloadFromUrl(String url, String videoTitle, String filename) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString("urlmy", url);
        bundle.putString("title",videoTitle);
        Player myObj = new Player();
        myObj.setArguments(bundle);
        fragmentManager.beginTransaction()
                .add(R.id.container, myObj, "player").addToBackStack(null).commit();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_left_header:
                back_btn_pressed(view);
                break;
            case R.id.imageplay:
                playVideo(view);
                break;
            case R.id.plauy:
                PlayAudio1(view);
                break;
            case R.id.downloade:
                downloadvideo(view);
                break;
        }
    }

    public static class DownloadStatus {
        public String otherFilePath;
        public boolean readyToMerge = false;
        public boolean hasVideo;
    }

    private int getDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(DownlodeID);

        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);

            return status;
        }
        return DownloadManager.ERROR_UNKNOWN;
    }

    public void back_btn_pressed(View view) {

        getFragmentManager().popBackStack();

    }

    public void playVideo(View view) {
        Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
        intent.putExtra("videoid", youtubeDataModel.getVideo_id());
        Log.d("kkk", youtubeDataModel.getVideo_id());
        startActivity(intent);
    }


    public void downloadvideo(View view) {
        final String videoID = youtubeDataModel.getTitle();
        String link = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());
        mainProgressBar.setVisibility(View.VISIBLE);

        if (view == null && Intent.ACTION_SEND.equals(getActivity().getIntent().getAction())
                && getActivity().getIntent().getType() != null && "text/plain".equals(getActivity().getIntent().getType())) {

            String ytLink = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);

            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
                youtubeLink = ytLink;
                getYoutubeDownloadUrl(youtubeLink);
            } else {
                Toast.makeText(getContext(), "Erorrrrr", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        } else if (view != null && youtubeLink != null) {
            getYoutubeDownloadUrl(youtubeLink);
        } else {
            getActivity().finish();
        }
    }

    private void addFormatToList(YtFile ytFile, SparseArray<YtFile> ytFiles) {
        int height = ytFile.getFormat().getHeight();
        if (height != -1) {
            for (YtFragmentedVideo frVideo : formatsToShowList) {
                if (frVideo.height == height && (frVideo.videoFile == null ||
                        frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {
                    return;
                }
            }
        }
        int i = ytFiles.size();
        Log.d("meou", String.valueOf(i));
        YtFragmentedVideo frVideo = new YtFragmentedVideo();
        frVideo.height = height;
        if (ytFile.getFormat().isDashContainer()) {
            if (height > 0) {
                frVideo.videoFile = ytFile;
                frVideo.audioFile = ytFiles.get(ITAG_FOR_AUDIO);
            } else {
                frVideo.audioFile = ytFile;
            }
        } else {
            frVideo.videoFile = ytFile;
        }
        formatsToShowList.add(frVideo);
    }


    private void addButtonToMainLayout(final String videoTitle, final YtFragmentedVideo ytFrVideo) {
        String btnText;
        if (ytFrVideo.height == -1) {
            btnText = "Audio " + ytFrVideo.audioFile.getFormat().getAudioBitrate() + " kbit/s";

        } else
            btnText = (ytFrVideo.videoFile.getFormat().getFps() == 60) ? ytFrVideo.height + "p60" :
                    ytFrVideo.height + "p" + "                " + ytFrVideo.videoFile.getFormat().getExt();
        Button btn = new Button(getContext());
        btn.setText(btnText);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String filename;
                if (videoTitle.length() > 55) {
                    filename = videoTitle.substring(0, 55);
                } else {
                    filename = videoTitle;
                }
                filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                filename += (ytFrVideo.height == -1) ? "" : "-" + ytFrVideo.height + "p";
                String downloadIds = "";
                boolean hideAudioDownloadNotification = false;
                if (ytFrVideo.videoFile != null) {


                    downloadIds += new RequestDownloadVideoStream().execute(ytFrVideo.videoFile.getUrl(), filename + "." + ytFrVideo.videoFile.getFormat().getExt());
                    hideAudioDownloadNotification = true;
                }
                if (ytFrVideo.audioFile != null) {

                    downloadIds += new RequestDownloadVideoStream().execute(ytFrVideo.audioFile.getUrl(), filename + "." + ytFrVideo.audioFile.getFormat().getExt());

                }
                if (ytFrVideo.audioFile != null)
                    cacheDownloadIds(downloadIds);
            }
        });
        mainLayout.addView(btn);
    }


    private void cacheDownloadIds(String downloadIds) {
        File dlCacheFile = new File(getActivity().getCacheDir().getAbsolutePath() + "/" + downloadIds);
        try {
            dlCacheFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class YtFragmentedVideo {
        int height;
        YtFile audioFile;
        YtFile videoFile;
    }


    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(getActivity()) {

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mainProgressBar.setVisibility(View.GONE);

                if (ytFiles == null) {
                    Toast.makeText(getContext(), "Not Found For This Video", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    YtFile ytFile = ytFiles.get(itag);

                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addButtonToMainLayout(vMeta.getTitle(), ytFile);
                    }
                }
            }
        }.extract(youtubeLink, true, false);
    }

    private void addButtonToMainLayout(final String videoTitle, final YtFile ytfile) {
        String btnText = (ytfile.getFormat().getHeight() == -1) ? "Audio " +
                ytfile.getFormat().getAudioBitrate() + " kbit/s" :
                ytfile.getFormat().getHeight() + "p";
        btnText += (ytfile.getFormat().isDashContainer()) ? " dash" : "";
        Button btn = new Button(getContext());
        btn.setText(btnText);
        Log.d("btnn", btnText);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String filename;
                if (videoTitle.length() > 55) {
                    filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
                } else {
                    filename = videoTitle + "." + ytfile.getFormat().getExt();
                }
                filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                downloadFromUrl(ytfile.getUrl(), videoTitle, filename);
                Toast.makeText(getContext(), "Download Start See in Notification Bar ", Toast.LENGTH_SHORT).show();
            }
        });
        mainLayout.addView(btn);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);


        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    //-------------------------------------------------------------------------
    private ProgressDialog pDialog;

    private class RequestDownloadVideoStream extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            URL u = null;
            int len1 = 0;
            int temp_progress = 0;
            int progress = 0;

            try {
                u = new URL(params[0]);
                is = u.openStream();
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                int size = huc.getContentLength();
                double fileSizeInBytes = huc.getContentLength();
                double fileSizeInKB = fileSizeInBytes / 1024;
                double fileSizeInMB = fileSizeInKB / 1024;
                Log.d("size", fileSizeInMB + "MB");
                filr = new DecimalFormat("#.##").format(fileSizeInMB);
                if (huc != null) {
                    String file_name = params[1] + ".mp4";
                    String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/YoutubeVideos";

                    File f = new File(storagePath);
                    if (!f.exists()) {
                        f.mkdir();
                    }

                    FileOutputStream fos = new FileOutputStream(f + "/" + file_name);
                    byte[] buffer = new byte[1024];
                    int total = 0;
                    if (is != null) {
                        while ((len1 = is.read(buffer)) != -1) {
                            total += len1;
                            progress = (total * 100) / size;
                            if (progress >= 0) {
                                temp_progress = progress;
                                publishProgress("" + progress);
                            } else
                                publishProgress("" + temp_progress + 1);
                            fos.write(buffer, 0, len1);
                        }
                    }
                    if (fos != null) {
                        publishProgress("" + 100);
                        fos.close();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            DialogBox();
        }

        //-----------------------------------------------------------------------
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
                Toast.makeText(getActivity(), "Download Successfully -" + filr + "MB", Toast.LENGTH_SHORT).show();
            }
        }


    }


    //---------------------------------------------------------------------------------------------
    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    public void DialogBox() {
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Downloading file. Please wait...\n" + filr);
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();

    }


}
