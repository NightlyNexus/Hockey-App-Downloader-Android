package com.nightlynexus.hockey.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class AppVersion implements Parcelable, Comparable<AppVersion> {

    @SerializedName("version")
    public final String version;

    @SerializedName("mandatory")
    public final boolean mandatory;

    @SerializedName("config_url")
    public final String configUrl;

    @SerializedName("download_url")
    public final String downloadUrl; // this is not a direct download url

    @SerializedName("timestamp")
    public final long timestamp;

    @SerializedName("appsize")
    public final long appSize;

    @SerializedName("device_family")
    public final String deviceFamily;

    @SerializedName("notes")
    public final String notes;

    @SerializedName("status")
    public final int status;

    @SerializedName("shortversion")
    public final String shortVersion;

    @SerializedName("minimum_os_version")
    public final String minimumOsVersion;

    @SerializedName("title")
    public final String title;

    public AppVersion(String version, boolean mandatory, String configUrl, String downloadUrl,
                      long timestamp, long appSize, String deviceFamily, String notes, int status,
                      String shortVersion, String minimumOsVersion, String title) {
        this.version = version;
        this.mandatory = mandatory;
        this.configUrl = configUrl;
        this.downloadUrl = downloadUrl;
        this.timestamp = timestamp;
        this.appSize = appSize;
        this.deviceFamily = deviceFamily;
        this.notes = notes;
        this.status = status;
        this.shortVersion = shortVersion;
        this.minimumOsVersion = minimumOsVersion;
        this.title = title;
    }

    @Override
    public int compareTo(@NonNull AppVersion another) {
        if (another.timestamp == this.timestamp) {
            return 0;
        } else if (another.timestamp > this.timestamp) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(version);
        out.writeByte((byte) (mandatory ? 1 : 0));
        out.writeString(configUrl);
        out.writeString(downloadUrl);
        out.writeLong(timestamp);
        out.writeLong(appSize);
        out.writeString(deviceFamily);
        out.writeString(notes);
        out.writeInt(status);
        out.writeString(shortVersion);
        out.writeString(minimumOsVersion);
        out.writeString(title);
    }

    public static final Parcelable.Creator<AppVersion> CREATOR
            = new Parcelable.Creator<AppVersion>() {
        public AppVersion createFromParcel(Parcel in) {
            final String version = in.readString();
            final boolean mandatory = in.readByte() != 0;
            final String configUrl = in.readString();
            final String downloadUrl = in.readString();
            final long timestamp = in.readLong();
            final long appSize = in.readLong();
            final String deviceFamily = in.readString();
            final String notes = in.readString();
            final int status = in.readInt();
            final String shortVersion = in.readString();
            final String minimumOsVersion = in.readString();
            final String title = in.readString();
            return new AppVersion(version, mandatory, configUrl, downloadUrl, timestamp, appSize,
                    deviceFamily, notes, status, shortVersion, minimumOsVersion, title);
        }

        public AppVersion[] newArray(int size) {
            return new AppVersion[size];
        }
    };
}
