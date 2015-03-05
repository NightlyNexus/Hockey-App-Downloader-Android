package com.nightlynexus.hockey.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.model.App;
import com.nightlynexus.hockey.model.AppVersion;
import com.nightlynexus.hockey.network.HockeyNetwork;
import com.nightlynexus.hockey.network.ViewTarget;
import com.nightlynexus.hockey.util.SharedPreferencesUtils;
import com.nightlynexus.hockey.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import retrofit.RetrofitError;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolderBase> {

    private static final int VIEW_TYPE_DEFAULT = R.layout.row_apps_list;
    private static final int VIEW_TYPE_SECTION_TITLE = R.layout.row_apps_list_section_title;
    private static final long DURATION_FADE_IN_APP_ICON = 320l;
    private static final int NUM_COLORS_PALETTE = 24;

    public static class ViewHolderBase extends RecyclerView.ViewHolder {

        public final View rootView;
        public CharSequence sectionTitle;

        public ViewHolderBase(View v) {
            super(v);
            rootView = v;
            sectionTitle = null;
        }
    }

    public static class ViewHolderSectionTitle extends ViewHolderBase {

        public final TextView sectionTitleView;

        public ViewHolderSectionTitle(View v) {
            super(v);
            final StaggeredGridLayoutManager.LayoutParams lp
                    = (StaggeredGridLayoutManager.LayoutParams) rootView.getLayoutParams();
            lp.setFullSpan(true);
            rootView.setLayoutParams(lp);
            sectionTitleView = (TextView) v.findViewById(R.id.section_title);
        }
    }

    public static class ViewHolderDefault extends ViewHolderBase {

        public final ImageView iconView;
        public final TextView nameView;
        public final TextView versionFormattedView;
        public final TextView versionNumberView;
        public final TextView timestampView;
        public AsyncTask<Bitmap, Void, Palette> paletteTask;
        public AsyncTask<Void, Void, AppVersion> setDownloadUrlTask;

        public ViewHolderDefault(View v) {
            super(v);
            iconView = (ImageView) v.findViewById(R.id.icon);
            nameView = (TextView) v.findViewById(R.id.name);
            versionFormattedView = (TextView) v.findViewById(R.id.version_formatted);
            versionNumberView = (TextView) v.findViewById(R.id.version_number);
            timestampView = (TextView) v.findViewById(R.id.timestamp);
            paletteTask = null;
            setDownloadUrlTask = null;
        }
    }

    private final Context mContext;
    private final List<App> mAppList;
    private final String mApiKey;

    /**
     * Note that AppsAdapter uses a copy of the appList parameter,
     * so modifications to this List<App> will not affect the AppsAdapter
     */
    public AppsAdapter(Context context, List<App> appList) {
        mContext = context;
        mAppList = new ArrayList<App>(appList);
        insertSectionTitles();
        mApiKey = SharedPreferencesUtils.getApiKey(mContext);
    }

    private void insertSectionTitles() {
        if (mAppList.size() > 0) {
            final ListIterator<App> iter = mAppList.listIterator();
            // not used, due to our cool header implementation:
            // iter.add(null); // first section title
            int releaseTypeId = iter.next().releaseType;
            while (iter.hasNext()) {
                final App app = iter.next();
                if (app.releaseType != releaseTypeId) {
                    iter.previous();
                    iter.add(null); // insert section title
                    iter.next();
                }
                releaseTypeId = app.releaseType;
            }
        }
    }

    public CharSequence getSectionTitleFromAdapterPosition(int position) {
        final App app = mAppList.get(position);
        if (app == null) {
            throw new IllegalArgumentException(position + " is a section header");
        }
        return mContext.getText(app.getReleaseTypeResId());
    }

    @Override
    public int getItemViewType(int position) {
        if (mAppList.get(position) == null) {
            return VIEW_TYPE_SECTION_TITLE;
        }
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        if (viewType == VIEW_TYPE_SECTION_TITLE) {
            return new ViewHolderSectionTitle(v);
        }
        return new ViewHolderDefault(v);
    }

    @Override
    public void onViewRecycled(ViewHolderBase holder) {
        cancelTasks(holder);
    }

    public static void cancelTasks(RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof ViewHolderDefault)) {
            return;
        }
        final ViewHolderDefault holder = (ViewHolderDefault) viewHolder;
        holder.iconView.clearAnimation();
        if (holder.paletteTask != null) {
            holder.paletteTask.cancel(true);
            holder.paletteTask = null;
        }
        if (holder.setDownloadUrlTask != null) {
            holder.setDownloadUrlTask.cancel(true);
            holder.setDownloadUrlTask = null;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolderBase viewHolder, int position) {
        if (viewHolder instanceof ViewHolderSectionTitle) {
            final ViewHolderSectionTitle holder = (ViewHolderSectionTitle) viewHolder;
            final App app = mAppList.get(position + 1); // safe, but confusing
            final CharSequence releaseType = mContext.getText(app.getReleaseTypeResId());
            holder.sectionTitleView.setText(releaseType);
            holder.sectionTitle = releaseType;
            return;
        }
        final ViewHolderDefault holder = (ViewHolderDefault) viewHolder;
        final App app = mAppList.get(position);
        holder.nameView.setText(app.title);
        holder.sectionTitle = mContext.getText(app.getReleaseTypeResId());
        ViewUtils.setImageAlpha(holder.iconView, 0);
        final ViewTarget targetIcon = new ViewTarget(holder.iconView) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                holder.iconView.setImageBitmap(bitmap);
                if (from == Picasso.LoadedFrom.NETWORK) {
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime,
                                                           Transformation t) {
                            ViewUtils.setImageAlpha(holder.iconView,
                                    (int) (interpolatedTime * 255));
                        }
                    };
                    animation.setDuration(DURATION_FADE_IN_APP_ICON);
                    holder.iconView.startAnimation(animation);
                } else {
                    ViewUtils.setImageAlpha(holder.iconView, 255);
                }
                holder.paletteTask = Palette.generateAsync(bitmap, NUM_COLORS_PALETTE,
                        new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                ViewUtils.setViewBackground(holder.rootView, ViewUtils
                                                .getPressedColorSelector(palette.getVibrantColor(
                                                        mContext.getResources().getColor(
                                                                R.color.background_row_apps_list_default)
                                                ))
                                );
                            }
                        }
                );
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        holder.iconView.setTag(targetIcon);

        Picasso.with(mContext)
                .load(app.getIconUrl())
                .into(targetIcon);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, mContext.getString(R.string.click_still_fetching_url,
                        app.title), Toast.LENGTH_SHORT).show();
            }
        });

        holder.versionFormattedView.setText(null);
        holder.versionNumberView.setText(null);
        holder.timestampView.setText(null);

        holder.setDownloadUrlTask = new SetDownloadUrlTask(holder.rootView,
                holder.versionFormattedView, holder.versionNumberView, holder.timestampView,
                mApiKey, app.publicIdentifier, app.getExtension(), app.title);
        holder.setDownloadUrlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }

    private static class SetDownloadUrlTask extends AsyncTask<Void, Void, AppVersion> {

        private final WeakReference<View> mRootViewWeakReference;
        private final WeakReference<TextView> mVersionFormattedViewWekReference;
        private final WeakReference<TextView> mVersionNumberViewWekReference;
        public final WeakReference<TextView> mTimestampViewWeakReference;
        private final String mApiKey;
        private final String mAppId;
        private final String mAppExtension;
        private final String mAppName;
        private boolean mError;

        private SetDownloadUrlTask(View rootView, TextView versionFormattedView,
                                   TextView versionNumberView, TextView timestampView,
                                   String apiKey, String appId, String appExtension,
                                   String appName) {
            mRootViewWeakReference = new WeakReference<View>(rootView);
            mVersionFormattedViewWekReference = new WeakReference<TextView>(versionFormattedView);
            mVersionNumberViewWekReference = new WeakReference<TextView>(versionNumberView);
            mTimestampViewWeakReference = new WeakReference<TextView>(timestampView);
            mApiKey = apiKey;
            mAppId = appId;
            mAppExtension = appExtension;
            mAppName = appName;
            mError = false;
        }

        @Override
        protected AppVersion doInBackground(Void... params) {
            try {
                final List<AppVersion> appVersionList = HockeyNetwork.getVersionList(mApiKey,
                        mAppId);
                if (isCancelled()) {
                    return null;
                }
                if (appVersionList == null || appVersionList.size() == 0) {
                    return null;
                }
                Collections.sort(appVersionList);
                return appVersionList.get(0);
            } catch (RetrofitError error) {
                final RetrofitError.Kind kind = error.getKind();
                if (kind == RetrofitError.Kind.NETWORK
                        || kind == RetrofitError.Kind.HTTP) {
                    error.printStackTrace();
                    mError = true;
                    return null;
                } else {
                    throw error;
                }
            }
        }

        @Override
        protected void onPostExecute(final AppVersion appVersion) {
            final View rootView = mRootViewWeakReference.get();
            if (rootView != null) {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Context context = rootView.getContext();
                        if (mError) {
                            Toast.makeText(context, R.string.click_network_error,
                                    Toast.LENGTH_SHORT).show();
                        } else if (appVersion != null && !TextUtils.isEmpty(appVersion.version)
                                && !TextUtils.isEmpty(appVersion.downloadUrl)) {
                            HockeyNetwork.enqueueDownload(context, mApiKey, mAppId, mAppName,
                                    appVersion.version, appVersion.downloadUrl, mAppExtension);
                        } else {
                            Toast.makeText(context, context.getString(
                                            R.string.click_empty_version_list, mAppName),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            final TextView versionFormattedView = mVersionFormattedViewWekReference.get();
            if (versionFormattedView != null) {
                if (appVersion != null && !TextUtils.isEmpty(appVersion.shortVersion)) {
                    versionFormattedView.setText(appVersion.shortVersion);
                } else {
                    versionFormattedView.setText(R.string.row_app_list_error);
                }
            }
            final TextView versionNumberView = mVersionNumberViewWekReference.get();
            if (versionNumberView != null) {
                if (appVersion != null && !TextUtils.isEmpty(appVersion.version)) {
                    versionNumberView.setText(appVersion.version);
                } else {
                    versionNumberView.setText(R.string.row_app_list_error);
                }
            }
            final TextView timestampView = mTimestampViewWeakReference.get();
            if (timestampView != null) {
                if (appVersion != null) {
                    timestampView.setText(getDateFromTimestamp(timestampView.getContext(),
                            appVersion.timestamp));
                } else {
                    timestampView.setText(R.string.row_app_list_error);
                }
            }
        }

        private static String getDateFromTimestamp(Context context, long timestamp) {
            timestamp *= 1000;
            final Locale locale = context.getResources().getConfiguration().locale;
            final Calendar cal = Calendar.getInstance(locale);
            cal.setTimeInMillis(timestamp);
            return DateFormat.format("M-d-yyyy", cal).toString();
        }
    }
}
