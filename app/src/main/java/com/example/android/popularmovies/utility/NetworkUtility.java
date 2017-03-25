package com.example.android.popularmovies.utility;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Isaac on 3/3/17.
 */

public class NetworkUtility {

    final static public String BASEURL = "http://api.themoviedb.org/3/movie/";
    final static public String POPULAR = "popular";
    final static public String TOPRATED = "top_rated";
    final static public String KEY = "api_key";
    final static public String APIKEY = "";

    public static URL popularMovie () {
        Uri.Builder uri = Uri.parse(BASEURL)
                             .buildUpon()
                             .appendEncodedPath(POPULAR)
                             .appendQueryParameter(KEY, APIKEY);

        URL url = null;
        try {
             url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL topRatedMovie () {
        Uri.Builder uri = Uri.parse(BASEURL)
                             .buildUpon()
                             .appendPath(TOPRATED)
                             .appendQueryParameter(KEY, APIKEY);
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = con.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            con.disconnect();
        }

    }
}
