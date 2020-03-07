package com.frost.firebasedb.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gokul Kalagara (Mr. Pyscho) on 07-03-2020.
 * <p>
 * FROST
 */
public class Location implements Parcelable {

    public Location() {

    }

    public Location(double latitude, double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    private double latitude;

    private double longitude;

    private String time;

    protected Location(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        time = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
