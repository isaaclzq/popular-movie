package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Isaac on 3/3/17.
 */

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.original_title) TextView mTitle;
    @BindView(R.id.poster_view) ImageView mPoster;
    @BindView(R.id.release_date) TextView mDate;
    @BindView(R.id.vote_average) TextView mVote;
    @BindView(R.id.overview_text) TextView mOverView;

    final static String KEY = "movie";
    final static String SCALE = "/10";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Movie movie = (Movie) getIntent().getParcelableExtra(KEY);

        mTitle.setText(movie.getOriginal_title());
        Picasso.with(this).load(movie.getThumnail()).into(mPoster);
        mDate.setText(movie.getRelease_date());
        mVote.setText(movie.getVote_average() + SCALE);
        mOverView.setText(movie.getOverview());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
