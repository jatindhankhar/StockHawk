package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;

/**
 * Created by jatin on 22/6/16.
 */

// http://stackoverflow.com/a/36129472
public class SnackBarBehaviour extends CoordinatorLayout.Behavior<FloatingActionButton> {
    public SnackBarBehaviour() {
    }

    public SnackBarBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}

