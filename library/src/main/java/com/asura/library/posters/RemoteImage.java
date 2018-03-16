package com.asura.library.posters;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteImage extends ImagePoster implements Parcelable {
    private String url;
    private Drawable placeHolder;
    private Drawable errorDrawable;

    public RemoteImage(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Drawable getPlaceHolder() {
        return placeHolder;
    }

    public RemoteImage setPlaceHolder(Drawable placeHolder) {
        this.placeHolder = placeHolder;
        return this;
    }

    public Drawable getErrorDrawable() {
        return errorDrawable;
    }

    public RemoteImage setErrorDrawable(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
        return this;
    }

    protected RemoteImage(Parcel in) {
        url = in.readString();
        placeHolder = (Drawable) in.readValue(Drawable.class.getClassLoader());
        errorDrawable = (Drawable) in.readValue(Drawable.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        if (placeHolder!=null) {
            dest.writeParcelable(((BitmapDrawable) placeHolder).getBitmap(), flags);
        }
        if (errorDrawable!=null) {
            dest.writeParcelable(((BitmapDrawable) errorDrawable).getBitmap(), flags);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RemoteImage> CREATOR = new Parcelable.Creator<RemoteImage>() {
        @Override
        public RemoteImage createFromParcel(Parcel in) {
            return new RemoteImage(in);
        }

        @Override
        public RemoteImage[] newArray(int size) {
            return new RemoteImage[size];
        }
    };
}
