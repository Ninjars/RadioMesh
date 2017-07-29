package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.database.RadioPoint;

import java.util.List;

import timber.log.Timber;


public class ForceDirectedView extends View {

    private Paint pointPaint;
    private Paint linePaint;
    private Point center;
    private float radius;
    private List<RadioPoint> dataset;

    public ForceDirectedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceDirectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pointPaint = new Paint();
        pointPaint.setARGB(150, 100, 100, 100);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setARGB(255, 179, 180, 181);
        linePaint.setAntiAlias(true);
    }

    public void setData(List<RadioPoint> radioPoints) {
        Timber.d("data: " + radioPoints);
        this.dataset = radioPoints;
        updateDisplayIfReady();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        center = new Point(w/2, h/2);
        radius = (Math.min(w, h) - Math.max(getPaddingLeft(), getPaddingRight())) / 2f;
        updateDisplayIfReady();
    }

    private void updateDisplayIfReady() {

    }
}
