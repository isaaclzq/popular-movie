package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Isaac on 3/19/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieDbContract.MovieEntry.TABLE_NAME + " (" +
                        MovieDbContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_THUMNAIL + " TEXT NOT NULL," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_VOTE + " TEXT NOT NULL," +
                        MovieDbContract.MovieEntry.COLUMN_MOVIE_RELEASEDATE + " TEXT NOT NULL" +
                        "); ";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        Log.v("MovieHelper", "created a table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieDbContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
