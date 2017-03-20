package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieDbContract;
import com.example.android.popularmovies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Isaac on 3/3/17.
 */

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.movie_title) TextView mTitle;
    @BindView(R.id.movie_image) ImageView mPoster;
    @BindView(R.id.movie_release) TextView mDate;
    @BindView(R.id.movie_rating) TextView mVote;
    @BindView(R.id.movie_overview) TextView mOverView;
    @BindView(R.id.movie_video) ListView mVideo;
    @BindView(R.id.movie_review) ListView mReview;
    @BindView(R.id.movie_favorite) Button mFavorite;

    final static private String KEY = "movie";
    final static private String SCALE = "/10";

    private LinkedList<YouTubeVideo> mVideoList;
    private LinkedList<Review> mReviewList;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private String backgroundColor;
    private String mId;
    private boolean mFavorOn;
    final private int LOADER_ID = 22;
    private SQLiteDatabase mDb;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_portrait);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mVideoList = new LinkedList<>();
        mReviewList = new LinkedList<>();

        MovieDbHelper movieDbHelper = new MovieDbHelper(this);
        mDb = movieDbHelper.getWritableDatabase();
        mResolver = getContentResolver();

        final Movie movie = (Movie) getIntent().getParcelableExtra(KEY);

        mTitle.setText(movie.getOriginal_title());
        Picasso.with(this).load(movie.getPosterUrl())
                          .placeholder(R.drawable.empty)
                          .error(R.drawable.empty)
                          .into(mPoster);
        mDate.setText(movie.getRelease_date());
        mVote.setText(movie.getVote_average() + SCALE);
        mOverView.setText(movie.getOverview());
        mId = movie.getId();
        mFavorOn = movie.isSaved();

        final ContentValues cv = new ContentValues();

        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFavorOn) {
                    mFavorite.setBackgroundResource(android.R.color.holo_green_dark);
                    mFavorite.setText("Mark As Favorite");
                    mFavorOn = false;
                } else {
                    mFavorite.setBackgroundColor(Color.parseColor("#ffff00"));
                    mFavorite.setText("Saved");
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getOriginal_title());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_VOTE, movie.getVote_average());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_RELEASEDATE, movie.getRelease_date());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_THUMNAIL, movie.getThumnail());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                    mResolver.insert(MovieDbContract.MovieEntry.CONTENT_URI, cv);
                    mFavorOn = true;
                }
            }
        });

        Bundle bundle = new Bundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject> loader = loaderManager.getLoader(LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(LOADER_ID, bundle, new DetailActivity.MovieExtraAsyncTaskloader());
        } else {
            loaderManager.restartLoader(LOADER_ID, bundle, new DetailActivity.MovieExtraAsyncTaskloader());
        }

        mTrailerAdapter = new TrailerAdapter(this, mVideoList);
        mVideo.setAdapter(mTrailerAdapter);
        mVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YouTubeVideo movie = (YouTubeVideo) parent.getItemAtPosition(position);
                Intent youTubeIntent = new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("vnd.youtube:" + movie.getAddress()));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                              Uri.parse(movie.getCompleteUrl(movie.getAddress()).toString()));
                try {
                    startActivity(youTubeIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
            }
        });
        mReviewAdapter = new ReviewAdapter(this, mReviewList);
        mReview.setAdapter(mReviewAdapter);
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

    private class MovieExtraAsyncTaskloader implements LoaderManager.LoaderCallbacks<LinkedList<JSONObject>> {

        @Override
        public Loader<LinkedList<JSONObject>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<LinkedList<JSONObject>>(DetailActivity.this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    forceLoad();
                }

                @Override
                public LinkedList<JSONObject> loadInBackground() {
                    if (args == null) {
                        return null;
                    }
                    LinkedList<JSONObject> result = new LinkedList<>();
                    Uri.Builder uriVideo = Uri.parse(NetworkUtility.BASEURL)
                            .buildUpon()
                            .appendEncodedPath(mId)
                            .appendEncodedPath("videos")
                            .appendQueryParameter(NetworkUtility.KEY, NetworkUtility.APIKEY);
                    Uri.Builder uriReview = Uri.parse(NetworkUtility.BASEURL)
                            .buildUpon()
                            .appendEncodedPath(mId)
                            .appendEncodedPath("reviews")
                            .appendQueryParameter(NetworkUtility.KEY, NetworkUtility.APIKEY);
                    try {
                        URL urlVideo = new URL(uriVideo.toString());
                        URL urlReview = new URL(uriReview.toString());
                        String videoResult = NetworkUtility.getResponseFromHttpUrl(urlVideo);
                        String reviewResult = NetworkUtility.getResponseFromHttpUrl(urlReview);
                        JSONObject videoJson = new JSONObject(videoResult);
                        JSONObject reviewJson = new JSONObject(reviewResult);
                        result.add(videoJson);
                        result.add(reviewJson);
                        return result;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<LinkedList<JSONObject>> loader, LinkedList<JSONObject> data) {
            if (data == null) {
                return;
            }
            JSONObject videoJson = data.get(0);
            JSONObject reviewJson = data.get(1);
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                jsonArray = videoJson.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    mVideoList.add(new YouTubeVideo(jsonObject.getString("key")));
                }
                jsonArray = reviewJson.getJSONArray("results");
                for (int j = 0; j < jsonArray.length(); j++) {
                    jsonObject = jsonArray.getJSONObject(j);
                    mReviewList.add((new Review(jsonObject.getString("author"), jsonObject.getString("content"))));
                }
                mTrailerAdapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(mVideo);
                mReviewAdapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(mReview);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoaderReset(Loader<LinkedList<JSONObject>> loader) {

        }
    }

    private long saveMovie(Movie movie) {
        ContentValues cv = new ContentValues();

        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_RELEASEDATE, movie.getRelease_date());
        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_THUMNAIL, movie.getThumnail());
        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getOriginal_title());
        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_VOTE, movie.getVote_average());
        cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());

        return mDb.insert(MovieDbContract.MovieEntry.TABLE_NAME, null, cv);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
