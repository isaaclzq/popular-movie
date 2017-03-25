package com.example.android.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Isaac on 3/11/17.
 */

public class Review implements Parcelable{

    private String author;
    private String content;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    private Review(Parcel in) {
        author = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
    }


    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {

        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
