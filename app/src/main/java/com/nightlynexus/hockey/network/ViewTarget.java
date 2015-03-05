package com.nightlynexus.hockey.network;

import android.view.View;

import com.squareup.picasso.Target;

/**
 * Implements equals() and hashCode() to allow automagical recycling in an adapter
 */
public abstract class ViewTarget implements Target {

    private final View mView;

    public ViewTarget(View view) {
        mView = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewTarget)) {
            return false;
        }
        ViewTarget other = (ViewTarget) o;
        return this.mView.equals(other.mView);
    }

    @Override
    public int hashCode() {
        return mView.hashCode();
    }
}
