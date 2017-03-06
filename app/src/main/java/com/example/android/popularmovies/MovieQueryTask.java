package com.example.android.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Isaac on 3/5/17.
 */

public class MovieQueryTask extends AsyncTask<URL, Void, JSONObject> {

    private Context mContext;
    private MainActivity.MovieQueryCompleteListener mListener;
    private View mProgressBar;

    public MovieQueryTask(Context context,
                          MainActivity.MovieQueryCompleteListener listener,
                          View view) {
        mContext = context;
        mListener = listener;
        mProgressBar = (ProgressBar) view;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected JSONObject doInBackground(URL... params) {
        URL url = params[0];
        try {
            String result = NetworkUtility.getResponseFromHttpUrl(url);
            JSONObject jsonObject = new JSONObject(result);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        mListener.onTaskComplete(jsonObject);
    }
}
