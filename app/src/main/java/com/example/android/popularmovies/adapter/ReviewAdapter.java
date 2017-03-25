package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Review;

import java.util.List;

/**
 * Created by Isaac on 3/18/17.
 */

public class ReviewAdapter extends ArrayAdapter<Review> {

    public ReviewAdapter(Context context, List resource) {
        super(context, 0, resource);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Review review = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }

        TextView reviewView = (TextView) convertView.findViewById(R.id.single_movie_review);
        reviewView.setText(review.getContent());

        TextView authorView = (TextView) convertView.findViewById(R.id.review_author);
        authorView.setText(review.getAuthor());

        return convertView;
    }


}
