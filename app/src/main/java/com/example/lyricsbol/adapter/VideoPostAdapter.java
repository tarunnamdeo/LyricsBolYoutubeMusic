package com.example.lyricsbol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lyricsbol.R;
import com.example.lyricsbol.interfaces.OnItemClickListener;
import com.example.lyricsbol.interfaces.YoutubeDataModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;


public class VideoPostAdapter extends RecyclerView.Adapter<VideoPostAdapter.YoutubePostHolder>  {
    ArrayList<YoutubeDataModel> dataSet;
    Context mContext = null;
    ArrayList<YoutubeDataModel> dataList;
    OnItemClickListener listener;

    public VideoPostAdapter() {
    }


    public VideoPostAdapter(ArrayList<YoutubeDataModel> dataSet, Context mContext, OnItemClickListener listener) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.listener = listener;
        this.dataList = new ArrayList<YoutubeDataModel>();
        this.dataList.addAll(dataSet);
    }

    @NonNull
    @Override
    public YoutubePostHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.youtube_post_layout, viewGroup, false);
        YoutubePostHolder postHolder = new YoutubePostHolder(view);
        return postHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final YoutubePostHolder holder, int position) {

        //set Views
        TextView textViewTitle = holder.textViewTitle;
        TextView textViewDes = holder.textViewDes;
        TextView textViewDate = holder.textViewDate;
        ImageView ImageThumb = holder.ImageThumb;

        final YoutubeDataModel object = dataSet.get(position);

        textViewTitle.setText(object.getTitle());
        textViewDes.setText(object.getDescription());
        textViewDate.setText(object.getPublishedAt());
        //  Log.d("totl",object.getTitle());
        //Image will be downloaded with URL
        Picasso.get().load(object.getThumbnail()).into(ImageThumb);

        holder.bind(dataSet.get(position), listener);

     /*   holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(mContext, object.getVideo_id(), Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(mContext,Main.class);
                mContext.startActivity(intent);

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

  /*  @Override
    public Filter getFilter() {
        return FilterHelper.newInstance(dataSet, this);

    }
    */

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
//-------------------------------------------------------------------------------


}
