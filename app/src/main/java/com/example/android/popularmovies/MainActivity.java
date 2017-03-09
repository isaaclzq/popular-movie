package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject>,
        MovieAdapter.MovieAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private int numColumn = 2;
    private String intentDataKey = "movie";
    private String APIKEY = "";
    private int WIDTHDIVIDER = 400;
    private TextView mNetworkConnection;
    private int sortCriteria;
    private String SORT_CRITERIA = "sortCriteria";

    final private int LOADER_ID = 22;
    final private String URL_KEY = "key";

    @Override
    public Loader<JSONObject> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<JSONObject>(this) {
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
                    Log.v("load", "here");
                    url = new URL(args.getString(URL_KEY));
                    Log.v("loadInBackground", args.getString(URL_KEY));
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
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject result) {
        if (result != null) {
            mMovieAdapter.setmMovieData(result);
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


    private boolean loadMovieData (int itemSelected) {
        Bundle bundle = new Bundle();

        switch (itemSelected) {
            case R.id.popular:
                sortCriteria = R.id.popular;
                bundle.putString(URL_KEY, NetworkUtility.popularMovie(APIKEY).toString());
                break;
            case R.id.rate:
                sortCriteria = R.id.rate;
                bundle.putString(URL_KEY, NetworkUtility.topRatedMovie(APIKEY).toString());
                break;
            default:
                return false;
        }

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject> loader = loaderManager.getLoader(LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(LOADER_ID, bundle, this);
        } else {
            loaderManager.restartLoader(LOADER_ID, bundle, this);
        }
        return true;
    }

    @Override
    public void onClick(Movie movie) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(intentDataKey, movie);
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
        outState.putInt(SORT_CRITERIA, sortCriteria);
    }
}
