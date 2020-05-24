package com.example.lyricsbol.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lyricsbol.R;
import com.example.lyricsbol.fragments.YoutubeFragment;
import com.example.lyricsbol.interfaces.OnItemClickListener;
import com.example.lyricsbol.interfaces.YoutubeDataModel;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class VideoPostAdapter extends RecyclerView.Adapter  {
    ArrayList<YoutubeDataModel> dataSet;
    Context mContext = null;
    ArrayList<YoutubeDataModel> dataList;
    OnItemClickListener listener;

    private List<Object> recyclerViewItems;

    private static final int ITEMS_VIDEO = 0;
    private static final int ITEM_BANNER_AD = 1;


    public VideoPostAdapter() {
    }


    public VideoPostAdapter(ArrayList<YoutubeDataModel> dataSet, Context mContext, OnItemClickListener listener,List<Object> recyclerViewItems) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.listener = listener;
        this.dataList = new ArrayList<YoutubeDataModel>();
        this.dataList.addAll(dataSet);
        this.recyclerViewItems = recyclerViewItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       switch (i){
           case ITEMS_VIDEO:
               View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.youtube_post_layout, viewGroup, false);
               YoutubePostHolder postHolder = new YoutubePostHolder(view);
               return postHolder;
           case ITEM_BANNER_AD:

           default:
               View bannerAdView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_ad_container,
                       viewGroup, false);
               return new BannerAdViewHolder(bannerAdView);



       }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int viewType=getItemViewType(position);

        switch (viewType) {
            case ITEMS_VIDEO:
//                Toast.makeText(mContext, "Video", Toast.LENGTH_SHORT).show();
//                Log.d("loggg", String.valueOf(viewType));

                YoutubePostHolder holder1 = (YoutubePostHolder) holder;

                //set Views
                TextView textViewTitle = holder1.textViewTitle;
                TextView textViewDes = holder1.textViewDes;
                TextView textViewDate = holder1.textViewDate;
                ImageView ImageThumb = holder1.ImageThumb;

                final YoutubeDataModel object = dataSet.get(position);

                textViewTitle.setText(object.getTitle());
                textViewDes.setText(object.getDescription());
                textViewDate.setText(object.getPublishedAt());
                //  Log.d("totl",object.getTitle());
                //Image will be downloaded with URL
                Picasso.get().load(object.getThumbnail()).into(ImageThumb);

                holder1.bind(dataSet.get(position), listener);


            case ITEM_BANNER_AD:
            default:
                if (recyclerViewItems.get(position) instanceof AdView) {
                    BannerAdViewHolder bannerHolder = (BannerAdViewHolder) holder;
                    AdView adView = (AdView) recyclerViewItems.get(position);
                    ViewGroup adCardView = (ViewGroup) bannerHolder.itemView;
                    // The AdViewHolder recycled by the RecyclerView may be a different
                    // instance than the one used previously for this position. Clear the
                    // AdViewHolder of any subviews in case it has a different
                    // AdView associated with it, and make sure the AdView for this position doesn't
                    if (adCardView.getChildCount() > 0) {
                        adCardView.removeAllViews();
                    }
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }

                    // Add the banner ad to the ad view.
                    adCardView.addView(adView);

                }
        }

    }



    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }



    public void setSpaceCraft(ArrayList<YoutubeDataModel> filteredSpace) {
        this.dataSet = filteredSpace;
    }

    public static class YoutubePostHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDes;
        TextView textViewDate;
        ImageView ImageThumb;

        public YoutubePostHolder(@NonNull View itemView) {
            super(itemView);
            this.textViewTitle = itemView.findViewById(R.id.textViewTitle);
            this.textViewDes = itemView.findViewById(R.id.textViewDes);
            this.textViewDate = itemView.findViewById(R.id.textViewDate);
            this.ImageThumb = itemView.findViewById(R.id.ImageThumb);

        }

        public void bind(final YoutubeDataModel item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }

    //--------------------------------------------------------------------------------
    public void updateList(String newList) {
        newList = newList.toLowerCase(Locale.getDefault());
        //   dataSet.clear();
        if (newList.length() == 0) {
            dataSet.addAll(dataList);
        } else {
            for (YoutubeDataModel youtubeDataModel : dataList) {
                if (youtubeDataModel.getTitle().toLowerCase(Locale.getDefault()).contains(newList)) {
                    dataSet.add(youtubeDataModel);
                }
            }
        }
        notifyDataSetChanged();


    }


    private static class BannerAdViewHolder extends RecyclerView.ViewHolder {
        public BannerAdViewHolder(View bannerAdView) {
            super(bannerAdView);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position % YoutubeFragment.ITEM_PER_ADD == 0)
            return ITEM_BANNER_AD;
        else
            return ITEMS_VIDEO;
//        return super.getItemViewType(position);
    }
}
