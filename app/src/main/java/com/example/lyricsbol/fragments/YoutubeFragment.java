package com.example.lyricsbol.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lyricsbol.Mykey;
import com.example.lyricsbol.R;
import com.example.lyricsbol.adapter.VideoPostAdapter;
import com.example.lyricsbol.interfaces.OnItemClickListener;
import com.example.lyricsbol.interfaces.YoutubeDataModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class YoutubeFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {


    private static String CHANNEL_ID = "UC6RqnWOAMap7WJcbUqBpYqQ";
    private static String CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&order=relevance&q=%257C&regionCode=IN&type=video&key=" + Mykey.Google_Youtube_API_KEY;
    SearchView searchView;
    EditText editText;
    Button button;
    ProgressBar progressBar, progressBar11;
    TextView textper;
    private Handler handler = new Handler();
    int progressbarstatus = 1;
    String search;
    //  private static String CHANNEL_GET_URL="https://developers.google.com/apis-explorer/#p/youtube/v3/youtube.channelSections.list?" +
    //     "part=snippet,contentDetails" +
    String CHANNEL_GET = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&order=relevance&q=%257C&regionCode=IN&type=video&key=" + Mykey.Google_Youtube_API_KEY;
    //   "&channelId=UCK8sQmJBp8GCxrOtXWBpyEA";
    private RecyclerView mList_videos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();
    Context mContext;

    public static int ITEM_PER_ADD=8;
    private static final String BANNER_ADD_ID="ca-app-pub-3940256099942544/6300978111";

//    private static final String BANNER_ADD_ID="ca-app-pub-8622490339945284/4865292917";

    private List<Object> recyclerItem=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);

        MobileAds.initialize(getContext() ,BANNER_ADD_ID);

    }

    public YoutubeFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.youtube_fragment, container, false);
        mList_videos = view.findViewById(R.id.mList_videos);
        editText = view.findViewById(R.id.edittext1);
        editText.setOnEditorActionListener(editorActionListener);
        progressBar = view.findViewById(R.id.progressBar1);
        progressBar11 = view.findViewById(R.id.progressBar11);
        textper = view.findViewById(R.id.textper);
        button = view.findViewById(R.id.button1);
        try {
            //   String nae = this.getArguments().getBundle("Key").toString();
            //  Log.d("na2", nae);
        } catch (Exception e) {
            e.printStackTrace();
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonclickgo();
                //banner
//                getBannerAds();
//                loadBannerAds();


            }
        });




        //  new RequestYoutubeAPI().execute();
        getBannerAds();
        return view;

    }

    private void buttonclickgo() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
        String te = editText.getText().toString().trim().replace(" ", "+");
        search = te;
        if (search.equals("")) {
            search = "string";
            Log.d("dog1", search);
        } else {
            search = te;
        }
        Log.d("dog0", search);
        CHANNEL_GET = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=20&order=relevance&q=" + search + "&regionCode=IN&type=video&key=" + Mykey.Google_Youtube_API_KEY;
        Log.d("uuuu", CHANNEL_GET);
        new RequestYoutubeAPI().execute();
        initList(mListData);
    }

    private TextView.OnEditorActionListener editorActionListener=new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                  switch (i){
                      case EditorInfo.IME_ACTION_SEARCH:
                          Toast.makeText(mContext, "Done", Toast.LENGTH_SHORT).show();
                          buttonclickgo();
                          break;
                  }
            return false;
        }
    };


    private void initList(ArrayList<YoutubeDataModel> mListData) {
        mList_videos.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new VideoPostAdapter(mListData, getActivity(), new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;

                Bundle bundle = new Bundle();
                Fragment fragment = new DetailsFrag();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                bundle.putParcelable(YoutubeDataModel.class.toString(), youtubeDataModel);
//                startActivity(intent);
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.frameyoutube, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        },recyclerItem);
        mList_videos.setAdapter(adapter);
    }

  /*  @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");

        super.onCreateOptionsMenu(menu, inflater);
    }*/

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.d("dogj", s);
        adapter.updateList(s);
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }


    //Create and Asynctask to get all the data
    public class RequestYoutubeAPI extends AsyncTask<String, Integer, String> {
        int progress_status;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar11.setProgress(0);
            progressBar11.setVisibility(View.VISIBLE);
            textper.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... tasks) {
            while (progress_status < 100) {
                progress_status += 2;
                publishProgress(progress_status);
                SystemClock.sleep(10);
            }
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //progressBar.incrementProgressBy(10);
                publishProgress(i);
                //  progressBar11.setProgress(i);
            }
          /*  new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressbarstatus < 100) {
                        progressbarstatus += 1;
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable() {
                            public void run() {
                               // progressBar11.setProgress(progressbarstatus);
                                //publishProgress(progressbarstatus);
                                //textper.setText(progressbarstatus + "/" + progressBar11.getMax());
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();   */

            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                progressBar.incrementProgressBy(10);
                //  progressBar11.setProgress(i);
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(CHANNEL_GET);
            Log.e("URL", CHANNEL_GET);
            try {

                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Task Completed";
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar11.setVisibility(View.GONE);
            textper.setVisibility(View.GONE);
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    mListData = parseVideoListFromResponse(jsonObject);
//                    recyclerItem= Collections.singletonList(parseVideoListFromResponse(jsonObject));

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d("valuee", String.valueOf(values[0]));
            progressBar11.setProgress(values[0]);
            // textper.setText(progressbarstatus + "/" + progressBar11.getMax());
            textper.setText(progress_status + "/" + progressBar11.getMax());
            super.onProgressUpdate(values);
        }
    }


    private ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();
        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("id")) {
                        JSONObject jsonID = json.getJSONObject("id");
                        String video_id = "";
                        if (jsonID.has("videoId")) {
                            video_id = jsonID.getString("videoId");
                        }
                        if (jsonID.has("kind")) {
                            if (jsonID.getString("kind").equals("youtube#video")) {
                                //get snippet
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                String description = jsonSnippet.getString("description");
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                                YoutubeDataModel youtube = new YoutubeDataModel();
                                youtube.setTitle(title);
                                youtube.setDescription(description);
                                youtube.setPublishedAt(publishedAt);
                                youtube.setThumbnail(thumbnail);
                                youtube.setVideo_id(video_id);
                                mListData.add(youtube);
                                recyclerItem.add(youtube);


                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mList;
    }



    private void getBannerAds(){
//         for (int i=0;i<20;i+=ITEM_PER_ADD){
        try {

            if (recyclerItem != null) {
                for (int i = 0; i <= recyclerItem.size(); i += ITEM_PER_ADD) {
//                ITEM_PER_ADD+=1;
//                Log.d("sukusuku",String.valueOf(ITEM_PER_ADD+" ss "+recyclerItem.size()+" sds "+mListData.size()));
                    AdView adView = new AdView(mContext);
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(BANNER_ADD_ID);
                    recyclerItem.add(adView);
                }
                loadBannerAds();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void loadBannerAds(){
        for (int i=0;i<recyclerItem.size();i++) {
//            Log.d("sixww", String.valueOf(recyclerItem.size()));

            Object item = recyclerItem.get(i);

            if (item instanceof AdView){
                final AdView adView=(AdView)item;
                adView.loadAd(new AdRequest.Builder().build());
            }
        }
    }


    private void getvideospost(){
        int count=0;

    }

}
