package com.nightlynexus.hockey.util;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public final class ViewUtils {

    public static void doOnGlobalLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    public static void setViewBackground(final View view, final Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void setImageAlpha(ImageView imageView, int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setImageAlpha(alpha);
        } else {
            imageView.setAlpha(alpha);
        }
    }

    // ripple for >=L, else default to StateListDrawable
    public static Drawable getPressedColorSelector(final int colorValue) {
        return getPressedColorSelector(colorValue, new ColorDrawable(colorValue));
    }

    // Drawable is mask for >=L, else is the pressed Drawable
    public static Drawable getPressedColorSelector(int colorValue, Drawable mask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(new ColorStateList(new int[][] { {} },
                    new int[] {colorValue}), null, mask);
        } else {
            final StateListDrawable sld = new StateListDrawable();
            sld.addState(new int[] { android.R.attr.state_pressed }, mask);
            sld.setColorFilter(colorValue, PorterDuff.Mode.SRC_IN);
            return sld;
        }
    }
}
