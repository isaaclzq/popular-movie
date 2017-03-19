package com.example.android.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Isaac on 3/11/17.
 */

public class YouTubeVideo implements Parcelable {

    private static final String YOUTUBE = "https://www.youtube.com/";
    private static final String QUERY = "v";
    private String address;

    public YouTubeVideo(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    private YouTubeVideo(Parcel in) {
        address = in.readString();
    }

    public URL getCompleteUrl (String address) {
        Uri.Builder uri = Uri.parse(YOUTUBE).buildUpon().appendQueryParameter(QUERY, address);
        try {
            URL url = new URL(uri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address.toString());
    }

    public static final Parcelable.Creator<YouTubeVideo> CREATOR = new Parcelable.Creator<YouTubeVideo>() {

        @Override
        public YouTubeVideo createFromParcel(Parcel source) {
            return new YouTubeVideo(source);
        }

        @Override
        public YouTubeVideo[] newArray(int size) {
            return new YouTubeVideo[size];
        }
    };
}
