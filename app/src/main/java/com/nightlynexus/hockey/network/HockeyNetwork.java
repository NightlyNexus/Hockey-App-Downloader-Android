package com.nightlynexus.hockey.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.model.App;
import com.nightlynexus.hockey.model.AppVersion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;

public final class HockeyNetwork {

    public static final String URL_AUTH_TOKENS = "https://rink.hockeyapp.net/manage/auth_tokens";

    private static final String ENDPOINT = "https://rink.hockeyapp.net/api/2";
    private static final String HEADER_APP_TOKEN = "X-HockeyAppToken";

    private static interface HockeyService {

        @GET("/apps")
        void getAppList(@Header(HEADER_APP_TOKEN) String token, Callback<List<App>> cb);

        @GET("/apps/{app_id}/app_versions")
        List<AppVersion> getAppVersionList(@Header(HEADER_APP_TOKEN) String token,
                                @Path("app_id") String appId);
    }

    private static final HockeyService restService = new RestAdapter.Builder()
            .setEndpoint(ENDPOINT)
            .setConverter(new GsonConverter(new GsonBuilder()
                    .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
                    .create()))
            .build()
            .create(HockeyService.class);

    private static class ItemTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {

            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    JsonElement jsonElement = elementAdapter.read(in);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.entrySet().size() == 2) {
                            jsonObject.remove("status");
                            jsonElement = jsonObject.entrySet().iterator().next().getValue();
                        }
                    }
                    return delegate.fromJsonTree(jsonElement);
                }
            }.nullSafe();
        }
    }

    public static void getAppList(String token, Callback<List<App>> cb) {
        restService.getAppList(token, cb);
    }

    public static List<AppVersion> getVersionList(String token, String appId) {
        return restService.getAppVersionList(token, appId);
    }

    public static String getIconUrl(String publicIdentifier) {
        return ENDPOINT + "/apps/" + publicIdentifier + ".png";
    }

    /**
     * @return an ID for the download, unique across the system.  This ID is used to make future
     *         calls related to this download.
     */
    public static long enqueueDownload(Context context, String token, String publicIdentifier,
                                       String appName, String appVersion, String downloadUrl,
                                       String extension) {
        downloadUrl = getDirectDownloadUrl(publicIdentifier, downloadUrl, extension);
        final String fileName = getFilename(appName, appVersion, extension);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(
                Context.DOWNLOAD_SERVICE);
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(
                downloadUrl));
        request.setDestinationUri(Uri.fromFile(new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + "/" + fileName)));
        request.addRequestHeader(HEADER_APP_TOKEN, token);
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(fileName);
        request.setDescription(context.getText(R.string.download_notification_description));
        return downloadManager.enqueue(request);
    }

    private static String getFilename(String appName, String appVersion, String extension) {
        return appName + "-" + appVersion + "." + extension;
    }

    /**
     * Use this to get the direct download url.
     * The download url from AppVersion is not very useful.
     *
     * @param publicIdentifier the app's public identifier
     * @param downloadUrl the download URL from the AppVersion
     * @param extension the file type extension of the app (example: apk)
     * @return the direct download url
     */
    private static String getDirectDownloadUrl(String publicIdentifier, String downloadUrl,
                                               String extension) {
        final String appVersionHockey = getHockeyAppVersion(downloadUrl);
        return ENDPOINT + "/apps/" + publicIdentifier + "/app_versions/" + appVersionHockey
                + "?format=" + extension;
    }

    private static String getHockeyAppVersion(String downloadUrl) {
        final int i = downloadUrl.lastIndexOf("/");
        return downloadUrl.substring(i + 1);
    }

    private HockeyNetwork() {
    }
}
