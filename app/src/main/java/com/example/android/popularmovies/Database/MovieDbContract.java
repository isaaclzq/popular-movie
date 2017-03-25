package com.example.android.popularmovies.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Isaac on 3/19/17.
 */

public class MovieDbContract {

    public static final String AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";


    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                                .appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_TITLE = "movieTitle";
        public static final String COLUMN_MOVIE_THUMNAIL = "movieThumnail";
        public static final String COLUMN_MOVIE_OVERVIEW = "movieOverview";
        public static final String COLUMN_MOVIE_VOTE = "movieVote";
        public static final String COLUMN_MOVIE_RELEASEDATE = "movieReleaseDate";
        public static final String COLUMN_MOVIE_ID = "movieId";
    }
}
