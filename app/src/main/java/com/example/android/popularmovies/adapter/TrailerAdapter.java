package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.YouTubeVideo;

import java.util.List;

/**
 * Created by Isaac on 3/18/17.
 */

public class TrailerAdapter extends ArrayAdapter<YouTubeVideo> {

    private Context mContext;

    public TrailerAdapter(Context context, List resource) {
        super(context, 0, resource);
        mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        YouTubeVideo video = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
        }

        TextView trailerNum = (TextView) convertView.findViewById(R.id.movie_trailer_num);
        trailerNum.setText(mContext.getString(R.string.tAdapter_trailer) + (position+1));

        return convertView;
    }


}
