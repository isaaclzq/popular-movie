package com.example.android.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Isaac on 3/3/17.
 */

public class Movie implements Parcelable{


    final static private String BASE_URL = "http://image.tmdb.org/t/p/";
    final static private String SIZE = "w185/";

    private String original_title;
    private String thumnail;
    private String overview;
    private String vote_average;
    private String release_date;
    private String id;
    private int saved;

    public Movie(String original_title, String thumnail, String overview, String vote_average, String release_date, String id) {
        this.original_title = original_title;
        this.thumnail = thumnail;
        this.overview = overview;
        this.vote_average = vote_average;
        this.release_date = release_date;
        this.id = id;
        this.saved = 0;
    }

    public String getId () {
        return id;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getThumnail() {
        return thumnail;
    }

    public String getOverview() {
        return overview;
    }

    public String getVote_average() {
        return vote_average;
    }

    public String getRelease_date() {
        return release_date;
    }

    public boolean isSaved() {
        return saved == 1;
    }

    public Movie() {

    }

    public String getPosterUrl() {
        Uri.Builder uri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendEncodedPath(SIZE)
                .appendEncodedPath(thumnail);
        return uri.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(original_title);
        dest.writeString(getPosterUrl());
        dest.writeString(overview);
        dest.writeString(vote_average);
        dest.writeString(release_date);
        dest.writeString(id);
        dest.writeInt(saved);
    }

    private Movie(Parcel in) {
        original_title = in.readString();
        thumnail = in.readString();
        overview = in.readString();
        vote_average = in.readString();
        release_date = in.readString();
        id = in.readString();
        saved = in.readInt();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
