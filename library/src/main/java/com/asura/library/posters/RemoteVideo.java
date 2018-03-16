package com.asura.library.posters;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteVideo extends VideoPoster implements Parcelable {
    private Uri uri;

    public RemoteVideo(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public RemoteVideo(Parcel in){
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri,flags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RemoteVideo> CREATOR = new Creator<RemoteVideo>() {
        @Override
        public RemoteVideo createFromParcel(Parcel in) {
            return new RemoteVideo(in);
        }

        @Override
        public RemoteVideo[] newArray(int size) {
            return new RemoteVideo[size];
        }
    };
}
