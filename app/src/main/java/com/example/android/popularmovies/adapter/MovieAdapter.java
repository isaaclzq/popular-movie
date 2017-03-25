package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Isaac on 3/2/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private ArrayList<Movie> mInUsedData;
    private ArrayList<Movie> mMovieData;
    private ArrayList<Movie> mFavoriteData;
    private final MovieAdapterOnClickHandler mClickHandler;
    private Context mContext;


    public interface MovieAdapterOnClickHandler {
        void onClick (Movie movie);
    }

    public MovieAdapter(MovieAdapterOnClickHandler handler) {
        this.mContext = (Context) handler;
        this.mMovieData = new ArrayList<>();
        this.mFavoriteData = new ArrayList<>();
        this.mInUsedData = this.mMovieData;
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
        Movie movie = mInUsedData.get(position);
        String url = movie.getPosterUrl();
        Context context = holder.mImageView.getContext();
        Picasso.with(context).load(url)
                             .placeholder(R.drawable.empty)
                             .error(R.drawable.empty)
                             .into(holder.mImageView);
    }


    @Override
    public int getItemCount() {
        return mInUsedData.size();
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
            Movie movie = mInUsedData.get(position);
            mClickHandler.onClick(movie);
        }
    }

    public void setEmptyData () {
        mInUsedData.clear();
    }

    public void setmMovieData (JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                mMovieData.clear();
                JSONArray jsonArray = jsonObject.getJSONArray(mContext.getString(R.string.mAdapter_results));
                JSONObject object;
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);
                    mMovieData.add(new Movie(object.getString(mContext.getString(R.string.mAdapter_title)),
                                             object.getString(mContext.getString(R.string.mAdapter_poster)),
                                             object.getString(mContext.getString(R.string.mAdapter_overview)),
                                             object.getString(mContext.getString(R.string.mAdapter_vote)),
                                             object.getString(mContext.getString(R.string.mAdapter_release)),
                                             object.getString(mContext.getString(R.string.mAdapter_id)), 0));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mInUsedData = mMovieData;
    }


    public void setmMovieData (Cursor cursor, boolean swap) {
        if (cursor != null) {
            mFavoriteData.clear();
            while (cursor.moveToNext()) {
                mFavoriteData.add(new Movie(cursor));
            }
        }

        if (swap) {
            mInUsedData = mFavoriteData;
        }
    }
}
