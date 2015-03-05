package com.nightlynexus.hockey.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            throw new IllegalStateException(DownloadReceiver.class.getSimpleName()
                    + " is only to be used with " + DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        }
        final DownloadManager.Query query = new DownloadManager.Query();
        // query.setFilterById(enqueue);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(
                Context.DOWNLOAD_SERVICE);
        final Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                final String uriString = c.getString(c.getColumnIndex(
                        DownloadManager.COLUMN_LOCAL_URI));
                if (!uriString.endsWith(".apk")) {
                    return;
                }
                final Uri uri = Uri.parse(uriString);
                final Intent intentInstall = new Intent(Intent.ACTION_VIEW);
                intentInstall.setDataAndType(uri, "application/vnd.android.package-archive");
                intentInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentInstall);
            }
        }
    }
}
