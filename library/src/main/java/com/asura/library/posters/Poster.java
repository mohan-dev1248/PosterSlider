package com.asura.library.posters;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.asura.library.events.OnPosterClickListener;

public class Poster implements Parcelable {

    private int id;
    private int position;

    private OnPosterClickListener onPosterClickListener;
    private View.OnTouchListener onTouchListener;

    public Poster(){

    }

    public Poster(Parcel in){
        id = in.readInt();
        position = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(position);
    }

    public static final Parcelable.Creator<Poster> CREATOR = new Parcelable.Creator<Poster>() {
        @Override
        public Poster createFromParcel(Parcel in) {
            return new Poster(in);
        }

        @Override
        public Poster[] newArray(int size) {
            return new Poster[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OnPosterClickListener getOnPosterClickListener() {
        return onPosterClickListener;
    }

    public void setOnPosterClickListener(OnPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
    }

    public View.OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }
}
