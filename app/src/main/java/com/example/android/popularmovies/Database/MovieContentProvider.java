package com.example.android.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.HashSet;

/**
 * Created by Isaac on 3/19/17.
 */

public class MovieContentProvider extends ContentProvider {

    private MovieDbHelper mMovieDbHelper;
    private Context mContext;
    public static HashSet<String> mIdCache = new HashSet<>();

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();



    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieDbContract.AUTHORITY, MovieDbContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieDbContract.AUTHORITY, MovieDbContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mMovieDbHelper = new MovieDbHelper(mContext);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case MOVIES:
                retCursor = db.query(MovieDbContract.MovieEntry.TABLE_NAME,
                                        projection,
                                        selection,
                                        selectionArgs,
                                        null,
                                        null,
                                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (retCursor != null) {
            while (retCursor.moveToNext()) {
                mIdCache.add(retCursor.getString(retCursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID)));
            }
        }

        retCursor.moveToFirst();
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return "vnd.android.cursor.dir" + "/" + MovieDbContract.AUTHORITY + "/" + MovieDbContract.PATH_MOVIES;
            case MOVIE_WITH_ID:
                return "vnd.android.cursor.item" + "/" + MovieDbContract.AUTHORITY + "/" + MovieDbContract.PATH_MOVIES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES:

                if (mIdCache.contains(values.getAsString(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID))) {
                    return null;
                }

                long id = db.insert(MovieDbContract.MovieEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieDbContract.MovieEntry.CONTENT_URI, id);
                    mIdCache.add(values.getAsString(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert movie info");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int movieDeleted;

        switch (match) {
            case MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                movieDeleted = db.delete(MovieDbContract.MovieEntry.TABLE_NAME,
                                        MovieDbContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                                        new String[]{id});
                mIdCache.remove(id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (movieDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return movieDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static boolean isMovieSaved(String movieId) {
        return mIdCache.contains(movieId);
    }
}
