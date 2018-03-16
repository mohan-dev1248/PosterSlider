package com.asura.library.posters;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

public class ImagePoster extends Poster implements Parcelable {

    private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;

    public ImagePoster(){

    }

    public ImagePoster(Parcel in){
        scaleType = (ImageView.ScaleType) in.readValue(ImageView.ScaleType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(scaleType);
    }

    public static final Parcelable.Creator<ImagePoster> CREATOR = new Parcelable.Creator<ImagePoster>() {
        @Override
        public ImagePoster createFromParcel(Parcel in) {
            return new ImagePoster(in);
        }

        @Override
        public ImagePoster[] newArray(int size) {
            return new ImagePoster[size];
        }
    };

    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }
}
