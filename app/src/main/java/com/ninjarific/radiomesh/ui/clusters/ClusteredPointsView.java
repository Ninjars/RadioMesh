package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.database.RadioPoint;

import java.util.List;

import timber.log.Timber;


public class ClusteredPointsView extends View {

    public ClusteredPointsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClusteredPointsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(List<RadioPoint> radioPoints) {
        Timber.d("data: " + radioPoints);
    }
}
