package com.ninjarific.radiomesh.ui.visualisation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.database.RadioPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;


public class CircularVisualisation extends View {

    private static final float CIRCLE_RADIUS = 10f;

    private List<RadioPoint> dataset;
    private Map<RadioPoint, PointF> pendingPositionedData;
    private Paint radioPaint;
    private Paint linePaint;
    private Paint bitmapPaint;
    private Point center = new Point(0,0);
    private float radius;
    private Bitmap bitmap;

    public CircularVisualisation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularVisualisation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        radioPaint = new Paint();
        radioPaint.setARGB(150, 100, 100, 100);
        radioPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setARGB(255, 179, 180, 181);
        linePaint.setAntiAlias(true);

        bitmapPaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        center = new Point(w/2, h/2);
        radius = (Math.min(w, h) - Math.max(getPaddingLeft(), getPaddingRight())) / 2f;
        updateDisplayIfReady();
    }

    public void setDataset(List<RadioPoint> dataset) {
        this.dataset = groupData(dataset);
        updateDisplayIfReady();
    }

    private List<RadioPoint> groupData(List<RadioPoint> dataset) {
        List<RadioPoint> datapoints = new ArrayList<>(dataset.size());

        for (RadioPoint point : dataset) {
            if (datapoints.contains(point)) continue;
            HashSet<RadioPoint> currentSet = new HashSet<>(dataset.size()/4);
            addConnectedNodesRecursive(point, currentSet);
            datapoints.addAll(currentSet);
        }

        return datapoints;
    }

    private void addConnectedNodesRecursive(RadioPoint point, HashSet<RadioPoint> currentSet) {
        currentSet.add(point);
        for (RadioPoint connected : point.getConnectedPoints()) {
            if (!currentSet.contains(connected)) {
                addConnectedNodesRecursive(connected, currentSet);
            }
        }
    }

    private void updateDisplayIfReady() {
        if (dataset != null && radius > 0) {
            pendingPositionedData = calculatePositions(dataset, center, radius);
            invalidate();
        }
    }

    private static Map<RadioPoint, PointF> calculatePositions(List<RadioPoint> dataset, Point center, float radius) {
        Map<RadioPoint, PointF> positionedData = new HashMap<>(dataset.size());
        for (int i = 0; i < dataset.size(); i++) {
            RadioPoint dataPoint = dataset.get(i);
            PointF point = calculatePoint(i, dataset.size(), center, radius);
            positionedData.put(dataPoint, point);
        }
        return positionedData;
    }

    private static PointF calculatePoint(int position, int totalPositions, Point center, float radius) {
        float fraction = (float) position / (float) (totalPositions);
        double angle = 2 * Math.PI * fraction;
        float x = (float) (Math.cos(angle) * radius) + center.x;
        float y = (float) (Math.sin(angle) * radius) + center.y;
        return new PointF(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkForPendingUpdate();
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        }
    }

    private void checkForPendingUpdate() {
        if (pendingPositionedData != null) {
            Map<RadioPoint, PointF> positionedData = pendingPositionedData;
            pendingPositionedData = null;

            int targetWidth = center.x * 2;
            int targetHeight = center.y * 2;
            if (bitmap != null && bitmap.getWidth() == targetWidth && bitmap.getHeight() == targetHeight) {
                Timber.i("reusing bitmap");
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight);
            } else {
                Timber.i("creating new bitmap");
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            }
            drawVisualisation(positionedData, dataset, radioPaint, linePaint, CIRCLE_RADIUS, new Canvas(bitmap));
        }
    }

    private static void drawVisualisation(Map<RadioPoint, PointF> positionedData,
                                    List<RadioPoint> dataset,
                                    Paint radioPaint, Paint linePaint, float radius, Canvas canvas) {
        canvas.drawColor(Color.argb(255, 71, 71, 71));
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RadioPoint> radioPoints = realm.where(RadioPoint.class).findAll();
        linePaint.setAlpha((int)(10 + 245/Math.sqrt(dataset.size()/2)));
        Timber.i("setting alpha to " + (int)(10 + 245/Math.sqrt(dataset.size()/2)));
        for (RadioPoint radio : dataset) {
            RadioPoint backgroundThreadRadio
                    = radioPoints.where().equalTo(RadioPoint.KEY_BSSID, radio.getBssid()).findFirst();
            PointF point = positionedData.get(backgroundThreadRadio);
            canvas.drawCircle(point.x, point.y, radius, radioPaint);
            for (RadioPoint connectedPoint : backgroundThreadRadio.getConnectedPoints()) {
                PointF targetPoint = positionedData.get(connectedPoint);
                if (targetPoint == null) continue;
                canvas.drawLine(point.x, point.y, targetPoint.x, targetPoint.y, linePaint);
            }
        }
    }
}
