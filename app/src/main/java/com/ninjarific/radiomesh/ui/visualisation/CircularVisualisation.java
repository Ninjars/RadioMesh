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

import com.ninjarific.radiomesh.database.realm.RadioPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;


public class CircularVisualisation extends View {

    private static final float CIRCLE_RADIUS = 10f;

    private final List<OrderedNode<RadioPoint>> orderedNodes = new ArrayList<>();
    private final List<OrderedNode<RadioPoint>> nodes = new ArrayList<>();

    private boolean pendingUpdate;
    private Paint radioPaint;
    private Paint linePaint;
    private Paint bitmapPaint;
    private Point center = new Point(0,0);
    private float radius;

    private Bitmap bitmap;

    private final Comparator<OrderedNode> comparator = (a, b) -> Float.compare(a.getAverage(), b.getAverage());

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
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setAlpha(200);
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
        nodes.clear();
        orderedNodes.clear();

        for (int i = 0; i < dataset.size(); i++) {
            RadioPoint node = dataset.get(i);
            List<Integer> neighbours = new ArrayList<>();
            for (RadioPoint neighbour : node.getConnectedPoints()) {
                neighbours.add(dataset.indexOf(neighbour));
            }
            nodes.add(new OrderedNode<>(node, i, neighbours));
        }

        orderedNodes.addAll(nodes);

        for (int i = 0; i < nodes.size() * 3; i++) {
            performSortingPass();
        }

        updateDisplayIfReady();
    }

    /**
     * lazily updates positions of nodes and caches results
     * @param i index of node in dataset
     * @return position of queried index in ordered list
     */
    private int getOrderedPositionOfNode(int i) {
        // look to see if cached value is valid
        if (orderedNodes.get(nodes.get(i).getPosition()).getIndex() != i) {
            // cache invalid; update all cached values
            for (int j = 0; j < nodes.size(); j++) {
                nodes.get(orderedNodes.get(j).getIndex()).setPosition(j);
            }
        }
        return nodes.get(i).getPosition();
    }

    private void performSortingPass() {
        for (int index = 0; index < nodes.size(); index++) {
            OrderedNode<RadioPoint> nodeI = nodes.get(index);
            int pos1 = getOrderedPositionOfNode(index);
            int sum = pos1;
            List<Integer> neighbours = nodeI.getNeighbours();
            for (int j = 0; j <neighbours.size(); j++) {
                int neighbourIndex = neighbours.get(j);
                int pos2 = getOrderedPositionOfNode(neighbourIndex);
                sum += pos2;
                orderedNodes.get(pos1).setAverage(sum / (nodeI.getNeighbours().size() + 1f));
            }
        }
        Collections.sort(orderedNodes, comparator);
    }

    private void updateDisplayIfReady() {
        if (orderedNodes != null && radius > 0) {
            pendingUpdate = true;
            invalidate();
        }
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
        if (pendingUpdate) {
            pendingUpdate = false;

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
            drawVisualisation(orderedNodes, nodes, radioPaint, linePaint, center, radius, CIRCLE_RADIUS, new Canvas
                    (bitmap));
        }
    }

    private static void drawVisualisation(List<OrderedNode<RadioPoint>> orderedNodes,
                                    List<OrderedNode<RadioPoint>> dataset,
                                    Paint radioPaint, Paint linePaint, Point center, float viewRadius, float radius,
                                          Canvas canvas) {
        canvas.drawColor(Color.argb(255, 31, 31, 31));
        Timber.i("setting alpha to " + (int)(10 + 245/Math.sqrt(dataset.size()/2)));

        List<PointF> nodePositions = new ArrayList<>();
        for (OrderedNode node : dataset) {
            nodePositions.add(calculatePoint(node.getPosition(), dataset.size(), center, viewRadius));
        }

        for (OrderedNode<RadioPoint> node : orderedNodes) {
            PointF a = nodePositions.get(node.getIndex());
            canvas.drawCircle(a.x, a.y, radius, radioPaint);
            for (int neighbourIndex : node.getNeighbours()) {
                PointF b = nodePositions.get(neighbourIndex);
                canvas.drawLine(a.x, a.y, b.x, b.y, linePaint);
            }
        }
    }
}
