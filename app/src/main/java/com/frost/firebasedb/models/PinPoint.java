package com.frost.firebasedb.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gokul Kalagara (Mr. Psycho) on 16-05-2020.
 * <p>
 * Frost
 */
public class PinPoint implements Parcelable {
    private double latitude;
    private double longitude;
    private String locationName;

    public PinPoint() {

    }

    protected PinPoint(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        locationName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(locationName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PinPoint> CREATOR = new Creator<PinPoint>() {
        @Override
        public PinPoint createFromParcel(Parcel in) {
            return new PinPoint(in);
        }

        @Override
        public PinPoint[] newArray(int size) {
            return new PinPoint[size];
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
