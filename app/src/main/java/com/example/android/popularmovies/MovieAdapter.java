package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Isaac on 3/2/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final String RESULTS = "results";
    private final String TITLE = "original_title";
    private final String POSTER = "poster_path";
    private final String OVERVIEW = "overview";
    private final String VOTE = "vote_average";
    private final String RELEASE = "release_date";

    private ArrayList<Movie> mMovieData;
    private final MovieAdapterOnClickHandler mClickHandler;


    public interface MovieAdapterOnClickHandler {
        void onClick (Movie movie);
    }

    public MovieAdapter(MovieAdapterOnClickHandler handler) {
        this.mMovieData = new ArrayList<>();
        mClickHandler = handler;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovieData.get(position);
        String url = movie.getPosterUrl();
        Context context = holder.mImageView.getContext();
        Picasso.with(context).load(url)
                             .placeholder(R.drawable.empty)
                             .error(R.drawable.empty)
                             .into(holder.mImageView);
    }


    @Override
    public int getItemCount() {
        return mMovieData.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView mImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.movie);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Movie movie = mMovieData.get(position);
            mClickHandler.onClick(movie);
        }
    }

    public void setEmptyData () {
        mMovieData.clear();
    }

    public void setmMovieData (JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                mMovieData.clear();
                JSONArray jsonArray = jsonObject.getJSONArray(RESULTS);
                JSONObject object;
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);
                    mMovieData.add(new Movie(object.getString(TITLE),
                                             object.getString(POSTER),
                                             object.getString(OVERVIEW),
                                             object.getString(VOTE),
                                             object.getString(RELEASE),
                                             object.getString("id")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setmMovieData (Cursor cursor) {
        if (cursor != null) {
            mMovieData.clear();
//            mMovieData.add(new Movie(cursor));
            while (cursor.moveToNext()) {
                mMovieData.add(new Movie(cursor));
            }
        }
    }
}
