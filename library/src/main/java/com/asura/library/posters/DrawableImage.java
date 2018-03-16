package com.asura.library.posters;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

public class DrawableImage extends ImagePoster implements Parcelable {
    private int drawable;

    public DrawableImage(@DrawableRes int drawable) {
        this.drawable = drawable;
    }

    public int getDrawable() {
        return drawable;
    }

    protected DrawableImage(Parcel in) {
        drawable = (int) in.readValue(Drawable.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(drawable);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DrawableImage> CREATOR = new Parcelable.Creator<DrawableImage>() {
        @Override
        public DrawableImage createFromParcel(Parcel in) {
            return new DrawableImage(in);
        }

        @Override
        public DrawableImage[] newArray(int size) {
            return new DrawableImage[size];
        }
    };
}
