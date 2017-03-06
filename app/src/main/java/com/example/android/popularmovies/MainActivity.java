package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler{

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private int numColumn = 2;
    private String intentDataKey = "movie";
    private String APIKEY = "";
    private int WIDTHDIVIDER = 400;

    public interface AsyncTaskCompleteListener<T> {
        public void onTaskComplete(T result);
    }

    public class MovieQueryCompleteListener implements AsyncTaskCompleteListener<JSONObject> {

        @Override
        public void onTaskComplete(JSONObject result) {
            if (result != null) {
                mMovieAdapter.setmMovieData(result);
                mMovieAdapter.notifyDataSetChanged();
            } else {
                mMovieAdapter.setmMovieData(null);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns(WIDTHDIVIDER));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData(R.id.popular);
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
        switch (itemSelected) {
            case R.id.popular:
                new MovieQueryTask(this, new MovieQueryCompleteListener(), mProgressBar)
                                .execute(NetworkUtility.popularMovie(APIKEY));
                break;
            case R.id.rate:
                new MovieQueryTask(this, new MovieQueryCompleteListener(), mProgressBar)
                                .execute(NetworkUtility.topRatedMovie(APIKEY));
                break;
            default:
                return false;
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
}
