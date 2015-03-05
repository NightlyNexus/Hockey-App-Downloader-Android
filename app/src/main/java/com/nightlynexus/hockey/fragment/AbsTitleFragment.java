package com.nightlynexus.hockey.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nightlynexus.hockey.activity.LoginActivity;

public abstract class AbsTitleFragment extends Fragment {

    public static final String ARG_TITLE = "ARG_TITLE";

    private CharSequence mTitleAlt = null;

    public final CharSequence getTitle() {
        return mTitleAlt != null ? mTitleAlt : getArguments().getCharSequence(ARG_TITLE);
    }

    /**
     * Sets a custom title that will be returned in getTitle(),
     * rather than the default title from ARG_TITLE.
     * Pass in {@code null} to reset to the default title.
     * Note that setTitle(title) will not be called here.
     *
     * @param title the alternate title
     * @return the result of getTitle(),
     *         for convenience in case of desired one-liners: activity.setTitle(setTitleAlt(title))
     */
    protected final CharSequence setTitleAlt(CharSequence title) {
        mTitleAlt = title;
        return getTitle();
    }

    public final LoginActivity getLoginActivity() {
        return (LoginActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getTitle() == null) {
            throw new IllegalStateException(
                    "Title cannot be null.  Make sure ARG_TITLE has been passed in.");
        }
        super.onCreate(savedInstanceState);
    }
}
