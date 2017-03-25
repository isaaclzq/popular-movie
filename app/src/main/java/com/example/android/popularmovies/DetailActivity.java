package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.android.popularmovies.adapter.ReviewAdapter;
import com.example.android.popularmovies.adapter.TrailerAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.data.Review;
import com.example.android.popularmovies.data.YouTubeVideo;
import com.example.android.popularmovies.database.MovieContentProvider;
import com.example.android.popularmovies.database.MovieDbContract;
import com.example.android.popularmovies.utility.NetworkUtility;
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

    private LinkedList<YouTubeVideo> mVideoList;
    private LinkedList<Review> mReviewList;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private String mId;
    private boolean mFavorOn;
    final private int LOADER_ID = 22;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Movie movie = getIntent().getParcelableExtra(getString(R.string.detail_key));
        final ContentValues cv = new ContentValues();


        mVideoList = new LinkedList<>();
        mReviewList = new LinkedList<>();
        mResolver = getContentResolver();

        mTitle.setText(movie.getOriginal_title());
        Picasso.with(this).load(movie.getPosterUrl())
                          .placeholder(R.drawable.empty)
                          .error(R.drawable.empty)
                          .into(mPoster);
        mDate.setText(movie.getRelease_date());
        mVote.setText(movie.getVote_average() + getString(R.string.detail_scale));
        mOverView.setText(movie.getOverview());
        mId = movie.getId();
        if (MovieContentProvider.isMovieSaved(mId)) {
            mFavorOn = true;
            buttonUiSaved();
        } else {
            mFavorOn = false;
            buttonUiUnsaved();
        }


        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFavorOn) {
                    buttonUiSaved();
                    Uri uri = MovieDbContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movie.getId()).build();
                    mResolver.delete(uri, null, null);
                    mFavorOn = false;
                    Toast.makeText(DetailActivity.this, R.string.detail_removed, Toast.LENGTH_SHORT).show();
                } else {
                    buttonUiUnsaved();
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getOriginal_title());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_VOTE, movie.getVote_average());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_RELEASEDATE, movie.getRelease_date());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_THUMNAIL, movie.getThumnail());
                    cv.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                    mResolver.insert(MovieDbContract.MovieEntry.CONTENT_URI, cv);
                    mFavorOn = true;
                    Toast.makeText(DetailActivity.this, getString(R.string.detail_saved), Toast.LENGTH_SHORT).show();
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


    // UI text and background color for saved movies
    private void buttonUiSaved () {
        mFavorite.setBackgroundColor(Color.parseColor(getString(R.string.detail_saved_color)));
        mFavorite.setText(R.string.detail_saved);
    }


    // UI text and background color for non-saved movies
    private void buttonUiUnsaved () {
        mFavorite.setBackgroundResource(android.R.color.holo_green_dark);
        mFavorite.setText(R.string.detail_marked);
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
                            .appendEncodedPath(getString(R.string.detail_videos))
                            .appendQueryParameter(NetworkUtility.KEY, NetworkUtility.APIKEY);
                    Uri.Builder uriReview = Uri.parse(NetworkUtility.BASEURL)
                            .buildUpon()
                            .appendEncodedPath(mId)
                            .appendEncodedPath(getString(R.string.detail_reviews))
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
                jsonArray = videoJson.getJSONArray(getString(R.string.detail_json_results));
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    mVideoList.add(new YouTubeVideo(jsonObject.getString(getString(R.string.detail_json_key))));
                }
                jsonArray = reviewJson.getJSONArray(getString(R.string.detail_json_results));
                for (int j = 0; j < jsonArray.length(); j++) {
                    jsonObject = jsonArray.getJSONObject(j);
                    mReviewList.add((new Review(jsonObject.getString(getString(R.string.detail_json_author)),
                                                jsonObject.getString(getString(R.string.detail_json_content)))));
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
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
