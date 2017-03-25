package com.example.android.popularmovies;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
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

import com.example.android.popularmovies.adapter.MovieAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.database.MovieDbContract;
import com.example.android.popularmovies.utility.NetworkUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    // widgets
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private TextView mNetworkConnection;

    private ContentResolver mContentResolver;

    // variables
    private int WIDTHDIVIDER = 400;
    private final int LOADER_ID = 22;
    private final int CACHE_LOAD_ID = 100;
    private int sortCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupCache();

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mNetworkConnection = (TextView) findViewById(R.id.no_internet);
        mContentResolver = getContentResolver();

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(WIDTHDIVIDER));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);



        if (savedInstanceState != null) {
            loadMovieData(savedInstanceState.getInt(getString(R.string.main_sort_criteria)));
        } else {
            loadMovieData(R.id.popular);
        }
    }

    private void setupCache() {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.main_url_key), MovieDbContract.MovieEntry.CONTENT_URI.toString());
        bundle.putBoolean(getString(R.string.main_swap), false);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject> loader = loaderManager.getLoader(LOADER_ID);

        if (loader == null) {
            loaderManager.initLoader(CACHE_LOAD_ID, bundle, new MovieDbAsyncTaskloader());
        } else {
            loaderManager.restartLoader(CACHE_LOAD_ID, bundle, new MovieDbAsyncTaskloader());
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
        boolean isCursor = false;

        switch (itemSelected) {
            case R.id.popular:
                sortCriteria = R.id.popular;
                bundle.putString(getString(R.string.main_url_key), NetworkUtility.popularMovie().toString());
                break;
            case R.id.rate:
                sortCriteria = R.id.rate;
                bundle.putString(getString(R.string.main_url_key), NetworkUtility.topRatedMovie().toString());
                break;
            case R.id.favorite:
                sortCriteria = R.id.favorite;
                bundle.putString(getString(R.string.main_url_key), MovieDbContract.MovieEntry.CONTENT_URI.toString());
                bundle.putBoolean(getString(R.string.main_swap), true);
                isCursor = true;
                break;
            default:
                return false;
        }

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject> loader = loaderManager.getLoader(LOADER_ID);
        if (isCursor) {
            if (loader == null) {
                loaderManager.initLoader(LOADER_ID, bundle, new MovieDbAsyncTaskloader());
            } else {
                loaderManager.restartLoader(LOADER_ID, bundle, new MovieDbAsyncTaskloader());
            }
        } else {
            if (loader == null) {
                loaderManager.initLoader(LOADER_ID, bundle, new MovieInfoAsyncTaskloader());
            } else {
                loaderManager.restartLoader(LOADER_ID, bundle, new MovieInfoAsyncTaskloader());
            }
        }
        return true;
    }

    @Override
    public void onClick(Movie movie) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(getString(R.string.main_intent_key), movie);
        startActivity(i);
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
        outState.putInt(getString(R.string.main_sort_criteria), sortCriteria);
    }



    private class MovieDbAsyncTaskloader implements LoaderManager.LoaderCallbacks<Cursor> {

        boolean swap = false;

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(MainActivity.this) {
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
                public Cursor loadInBackground() {
                    Uri uri = Uri.parse(args.getString(getString(R.string.main_url_key)));
                    swap = args.getBoolean(getString(R.string.main_swap));
                    return mContentResolver.query(uri, null,null,null,null);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                mMovieAdapter.setmMovieData(data, swap);
                mNetworkConnection.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                data.close();
            } else {
                mMovieAdapter.setEmptyData();
                mNetworkConnection.setText(R.string.main_empty_list);
                mNetworkConnection.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                data.close();
            }
            mMovieAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
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
                        url = new URL(args.getString(getString(R.string.main_url_key)));
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
                mMovieAdapter.setEmptyData();
                mNetworkConnection.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {

        }
    }


}
