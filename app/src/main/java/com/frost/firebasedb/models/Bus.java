package com.frost.firebasedb.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Gokul Kalagara (Mr. Pyscho) on 07-03-2020.
 * <p>
 * FROST
 */
public class Bus implements Parcelable {
    private String busId;

    private String name;

    private String registrationNumber;

    private boolean status;

    private Location current;

    public Bus() {

    }

    public Bus(String name, String registrationNumber, boolean status) {
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.status = status;
    }

    protected Bus(Parcel in) {
        busId = in.readString();
        name = in.readString();
        registrationNumber = in.readString();
        status = in.readByte() != 0;
        current = in.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busId);
        dest.writeString(name);
        dest.writeString(registrationNumber);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeParcelable(current, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Bus> CREATOR = new Creator<Bus>() {
        @Override
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }

        @Override
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Location getCurrent() {
        return current;
    }

    public void setCurrent(Location current) {
        this.current = current;
    }

}
