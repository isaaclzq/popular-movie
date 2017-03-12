package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private int numColumn = 2;
    private String intentDataKey = "movie";
    private int WIDTHDIVIDER = 400;
    private TextView mNetworkConnection;
    private int sortCriteria;
    private String SORT_CRITERIA = "sortCriteria";
    final private int LOADER_ID = 22;
    final private int MOVIE_EXTRA = 23;
    final private String URL_KEY = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mNetworkConnection = (TextView) findViewById(R.id.no_internet);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(WIDTHDIVIDER));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        if (savedInstanceState != null) {
            loadMovieData(savedInstanceState.getInt(SORT_CRITERIA));
        } else {
            loadMovieData(R.id.popular);
        }
    }

    private int numberOfColumns(int widthDivider) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }


    private boolean loadMovieData(int itemSelected) {
        Bundle bundle = new Bundle();

        switch (itemSelected) {
            case R.id.popular:
                sortCriteria = R.id.popular;
                bundle.putString(URL_KEY, NetworkUtility.popularMovie().toString());
                break;
            case R.id.rate:
                sortCriteria = R.id.rate;
                bundle.putString(URL_KEY, NetworkUtility.topRatedMovie().toString());
                break;
            default:
                return false;
        }

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject> loader = loaderManager.getLoader(LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(LOADER_ID, bundle, new MovieInfoAsyncTaskloader());
        } else {
            loaderManager.restartLoader(LOADER_ID, bundle, new MovieInfoAsyncTaskloader());
        }
        return true;
    }

    @Override
    public void onClick(Movie movie) {
        Intent i = new Intent(this, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", movie.getId());
        if (!movie.hasTrailersAndReviews()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<JSONObject> loader = loaderManager.getLoader(MOVIE_EXTRA);
            if (loader == null) {
                loaderManager.initLoader(MOVIE_EXTRA, bundle, new MovieExtraAsyncTaskloader(movie, i));
            } else {
                loaderManager.restartLoader(MOVIE_EXTRA, bundle, new MovieExtraAsyncTaskloader(movie, i));
            }
        } else {
            i.putExtra(intentDataKey, movie);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemSelected = item.getItemId();
        return loadMovieData(itemSelected);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SORT_CRITERIA, sortCriteria);
    }

    private class MovieInfoAsyncTaskloader implements LoaderManager.LoaderCallbacks<JSONObject> {

        @Override
        public Loader<JSONObject> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<JSONObject>(MainActivity.this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }

                @Override
                public JSONObject loadInBackground() {
                    URL url = null;
                    String result = null;
                    JSONObject resultJson = null;
                    try {
                        url = new URL(args.getString(URL_KEY));
                        result = NetworkUtility.getResponseFromHttpUrl(url);
                        resultJson = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return resultJson;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
            if (data != null) {
                mMovieAdapter.setmMovieData(data);
                mMovieAdapter.notifyDataSetChanged();
            } else {
                mMovieAdapter.setmMovieData(null);
                mNetworkConnection.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {

        }
    }

    private class MovieExtraAsyncTaskloader implements LoaderManager.LoaderCallbacks<LinkedList<JSONObject>> {

        Movie curMovie;
        Intent mIntent;

        public MovieExtraAsyncTaskloader(Movie movie, Intent intent) {
            this.curMovie = movie;
            this.mIntent = intent;
        }

        @Override
        public Loader<LinkedList<JSONObject>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<LinkedList<JSONObject>>(MainActivity.this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }

                @Override
                public LinkedList<JSONObject> loadInBackground() {
                    if (args == null) {
                        return null;
                    }
                    String id = args.getString("id");
                    LinkedList<JSONObject> result = new LinkedList<>();
                    Uri.Builder uriVideo = Uri.parse(NetworkUtility.BASEURL)
                            .buildUpon()
                            .appendEncodedPath(id)
                            .appendEncodedPath("videos")
                            .appendQueryParameter(NetworkUtility.KEY, NetworkUtility.APIKEY);
                    Uri.Builder uriReview = Uri.parse(NetworkUtility.BASEURL)
                            .buildUpon()
                            .appendEncodedPath(id)
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
            YouTubeVideo youTubeVideo;
            Review review;
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                jsonArray = videoJson.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    curMovie.addTrailer(new YouTubeVideo(jsonObject.getString("key")));
                }
                jsonArray = reviewJson.getJSONArray("results");
                for (int j = 0; j < jsonArray.length(); j++) {
                    jsonObject = jsonArray.getJSONObject(j);
                    curMovie.addReview((new Review(jsonObject.getString("author"), jsonObject.getString("content"))));
                }
                mIntent.putExtra(intentDataKey, curMovie);
                startActivity(mIntent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onLoaderReset(Loader<LinkedList<JSONObject>> loader) {

        }
    }
}
