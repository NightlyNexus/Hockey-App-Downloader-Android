package com.nightlynexus.hockey.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.network.HockeyNetwork;

public class App implements Parcelable, Comparable<App> {

    public static final int PLATFORM_UNKNOWN = 0;
    public static final int PLATFORM_ANDROID = 1;
    public static final int PLATFORM_IOS = 2;

    // magic numbers from Hockey
    private static final int RELEASE_TYPE_ALPHA = 2;
    private static final int RELEASE_TYPE_BETA = 0;
    private static final int RELEASE_TYPE_STORE = 1;
    private static final int RELEASE_TYPE_ENTERPRISE = 3;

    @SerializedName("title")
    public final String title;

    @SerializedName("bundle_identifier")
    public final String bundleIdentifier;

    @SerializedName("public_identifier")
    public final String publicIdentifier;

    @SerializedName("device_family")
    public final String deviceFamily;

    @SerializedName("minimum_os_version")
    public final String minimumOsVersion;

    @SerializedName("release_type")
    public final int releaseType;

    @SerializedName("status")
    public final int status;

    @SerializedName("platform")
    public final String platform;

    public final String getIconUrl() {
        return HockeyNetwork.getIconUrl(publicIdentifier);
    }

    public int getReleaseTypeResId() {
        final int resId;
        switch (releaseType) {
            case RELEASE_TYPE_ALPHA:
                resId = R.string.release_type_alpha;
                break;
            case RELEASE_TYPE_BETA:
                resId = R.string.release_type_beta;
                break;
            case RELEASE_TYPE_STORE:
                resId = R.string.release_type_store;
                break;
            case RELEASE_TYPE_ENTERPRISE:
                resId = R.string.release_type_enterprise;
                break;
            default:
                throw new RuntimeException("Unsupported release type: " + releaseType);
        }
        return resId;
    }

    public final int getPlatform() {
        final String platform = this.platform.toLowerCase();
        switch (platform) {
            case "android":
                return PLATFORM_ANDROID;
            case "ios":
                return PLATFORM_IOS;
            default:
                return PLATFORM_UNKNOWN;
        }
    }

    public final String getExtension() {
        final String platform = this.platform.toLowerCase();
        switch (platform) {
            case "android":
                return "apk";
            case "ios":
                return "ipa";
            default:
                return null;
        }
    }

    @Override
    public int compareTo(@NonNull App another) {
        final int diff = another.releaseType - this.releaseType;
        if (diff != 0) {
            return diff;
        }
        return this.title.compareTo(another.title);
    }

    public App(String title, String bundleIdentifier, String publicIdentifier, String deviceFamily,
               String minimumOsVersion, int releaseType, int status, String platform) {
        this.title = title;
        this.bundleIdentifier = bundleIdentifier;
        this.publicIdentifier = publicIdentifier;
        this.deviceFamily = deviceFamily;
        this.minimumOsVersion = minimumOsVersion;
        this.releaseType = releaseType;
        this.status = status;
        this.platform = platform;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(bundleIdentifier);
        out.writeString(publicIdentifier);
        out.writeString(deviceFamily);
        out.writeString(minimumOsVersion);
        out.writeInt(releaseType);
        out.writeInt(status);
        out.writeString(platform);
    }

    public static final Parcelable.Creator<App> CREATOR
            = new Parcelable.Creator<App>() {
        public App createFromParcel(Parcel in) {
            final String title = in.readString();
            final String bundleIdentifier = in.readString();
            final String publicIdentifier = in.readString();
            final String deviceFamily = in.readString();
            final String minimumOsVersion = in.readString();
            final int releaseType = in.readInt();
            final int status = in.readInt();
            final String platform = in.readString();
            return new App(title, bundleIdentifier, publicIdentifier, deviceFamily,
                    minimumOsVersion, releaseType, status, platform);
        }

        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
