package com.nightlynexus.hockey.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.activity.LoginActivity;
import com.nightlynexus.hockey.adapter.AppsAdapter;
import com.nightlynexus.hockey.model.App;
import com.nightlynexus.hockey.receiver.DownloadReceiver;
import com.nightlynexus.hockey.util.SharedPreferencesUtils;
import com.nightlynexus.hockey.util.ViewUtils;

import java.util.Collections;
import java.util.List;

public class AppsListFragment extends AbsTitleFragment {

    public static final String ARG_APPS = "ARG_APPS";

    private View mRootView;
    private RecyclerView mRecyclerView;
    private ScrollView mSectionScrollView;
    private TextView mSectionTitleTopView;
    private TextView mSectionTitleMiddleView;
    private TextView mSectionTitleBottomView;
    private StaggeredGridLayoutManager mLayoutManager;
    private AppsAdapter mAdapter;
    private List<App> mAppList;
    private BroadcastReceiver mDownloadReceiver;

    private final RecyclerView.OnScrollListener mOnScrollListener
            = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            setSectionTitleViewText(dy);
        }
    };

    private void setSectionTitleViewText(int dy) {
        if (mLayoutManager.getChildCount() == 0) {
            mSectionScrollView.setVisibility(View.GONE);
            return;
        }
        mSectionScrollView.setVisibility(View.VISIBLE);
        final int i = mLayoutManager.findFirstVisibleItemPositions(null)[0];
        final AppsAdapter.ViewHolderBase viewHolder
                = (AppsAdapter.ViewHolderBase) mRecyclerView.findViewHolderForPosition(i);
        if (viewHolder == null) {
            return;
        }
        if (!(viewHolder instanceof AppsAdapter.ViewHolderSectionTitle)) {
            // reset scroll header
            mSectionTitleTopView.setText(mSectionTitleMiddleView.getText());
            mSectionTitleMiddleView.setText(viewHolder.sectionTitle);
            mSectionScrollView.scrollTo(0, mSectionTitleMiddleView.getTop());
            return;
        }
        final AppsAdapter.ViewHolderSectionTitle holder
                = (AppsAdapter.ViewHolderSectionTitle) viewHolder;
        if (i > 0) {
            mSectionTitleTopView.setText(mAdapter.getSectionTitleFromAdapterPosition(i - 1));
        }
        mSectionTitleBottomView.setText(holder.sectionTitle);
        mSectionScrollView.scrollBy(0, dy);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppList = getArguments().getParcelableArrayList(ARG_APPS);
        Collections.sort(mAppList);
        mDownloadReceiver = new DownloadReceiver();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_apps_list, container, false);
        mRootView.setVisibility(View.INVISIBLE);
        mRecyclerView = (RecyclerView) mRootView.findViewById(
                R.id.apps_list_recycler);
        mSectionScrollView = (ScrollView) mRootView.findViewById(
                R.id.section_title_scroller);
        mSectionTitleTopView = (TextView) mSectionScrollView.findViewById(
                R.id.section_title_top);
        mSectionTitleMiddleView = (TextView) mSectionScrollView.findViewById(
                R.id.section_title_middle);
        mSectionTitleBottomView = (TextView) mSectionScrollView.findViewById(
                R.id.section_title_bottom);
        ViewUtils.doOnGlobalLayout(mSectionTitleTopView, new Runnable() {
            @Override
            public void run() {
                final ViewGroup.LayoutParams lp = mSectionScrollView.getLayoutParams();
                lp.height = mSectionTitleTopView.getHeight();
                mSectionScrollView.setLayoutParams(lp);
            }
        });
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.VERTICAL); // 1 is temporary
        mAdapter = new AppsAdapter(getActivity(), mAppList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setSpanCount();
        mRecyclerView.setOnScrollListener(mOnScrollListener);
    }

    private void setSpanCount() {
        final View parent = (View) mRecyclerView.getParent();
        ViewUtils.doOnGlobalLayout(parent, new Runnable() {
            @Override
            public void run() {
                final int widthParent = parent.getWidth();
                final int widthMinRow = getActivity().getResources().getDimensionPixelSize(
                        R.dimen.row_apps_list_min_width);
                final int spanCount = Math.max(widthParent / widthMinRow, 1);
                mLayoutManager.setSpanCount(spanCount);
                mLayoutManager.requestLayout();
                ViewUtils.doOnGlobalLayout(mSectionScrollView, new Runnable() {
                    @Override
                    public void run() {
                        setSectionTitleViewText(0);
                        mRootView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mDownloadReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mDownloadReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apps_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        final LoginActivity loginActivity = getLoginActivity();
        SharedPreferencesUtils.clearPreferencesUser(loginActivity);
        loginActivity.completeLogout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            final View child = mRecyclerView.getChildAt(i);
            final AppsAdapter.ViewHolderBase viewHolder
                    = (AppsAdapter.ViewHolderBase) mRecyclerView.getChildViewHolder(child);
            AppsAdapter.cancelTasks(viewHolder);
        }
    }
}
