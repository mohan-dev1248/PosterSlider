package com.asura.library.posters;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class BitmapImage extends ImagePoster implements Parcelable {
    private Bitmap bitmap;
    private int videoResourcePath;

    public BitmapImage(@NonNull Bitmap bitmap, @NonNull int videoResourcePath){
        this.bitmap = bitmap;
        this.videoResourcePath = videoResourcePath;
    }

    public BitmapImage(Parcel in){
        super(in);
        this.bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        this.videoResourcePath = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmap,flags);
        dest.writeInt(videoResourcePath);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BitmapImage> CREATOR = new Parcelable.Creator<BitmapImage>() {
        @Override
        public BitmapImage createFromParcel(Parcel in) {
            return new BitmapImage(in);
        }

        @Override
        public BitmapImage[] newArray(int size) {
            return new BitmapImage[size];
        }
    };

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getVideoResourcePath() {
        return videoResourcePath;
    }
}
