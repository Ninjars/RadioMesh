package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

public class ForceDirectedView extends View {
    private static final int MAX_FPS = 40; //desired fps
    private static final int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
    private static final float CIRCLE_RADIUS = 8f;
    private static final double FORCE_FACTOR = 0.1;
    private static final float SCREEN_PADDING_PX = 16;

    private RectF viewBounds = new RectF();
    private Matrix matrix = new Matrix();
    private RectF nodeBounds = new RectF();
    private RectF squareBounds = new RectF();
    private QuadTree<ForceConnectedNode> quadTree;

    private Paint pointPaint;
    private Paint linePaint;
    private Paint repulsionLinePaint;
    private Paint boundsPaint;
    private List<ForceConnectedNode> datasetNodes = Collections.emptyList();
    private List<ForceConnection> uniqueConnections = Collections.emptyList();
    private List<ForceConnection> uniqueRepulsions = Collections.emptyList();
    private boolean debugDraw = true;

    private int viewWidth;
    private int viewHeight;

    public ForceDirectedView(Context context) {
        super(context);
        init();
    }

    public ForceDirectedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceDirectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static void updateNodeBounds(ForceConnectedNode node, RectF nodeBounds) {
        if (node.getX() < nodeBounds.left) {
            nodeBounds.left = node.getX();
        }
        if (node.getY() < nodeBounds.top) {
            nodeBounds.top = node.getY();
        }
        if (node.getX() > nodeBounds.right) {
            nodeBounds.right = node.getX();
        }
        if (node.getY() > nodeBounds.bottom) {
            nodeBounds.bottom = node.getY();
        }
    }

    private static void drawBounds(QuadTree<ForceConnectedNode> quadTree, Canvas canvas, Paint paint) {
        if (quadTree.isLeaf()) {
            //draw
            canvas.drawRect(quadTree.getBounds(), paint);
        } else {
            for (QuadTree<ForceConnectedNode> node : quadTree.getSubTrees()) {
                drawBounds(node, canvas, paint);
            }
        }
    }

    private void init() {
        pointPaint = new Paint();
        pointPaint.setARGB(200, 225, 225, 225);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setARGB(100, 170, 170, 170);
        linePaint.setAntiAlias(true);

        repulsionLinePaint = new Paint();
        repulsionLinePaint.setStyle(Paint.Style.STROKE);
        repulsionLinePaint.setStrokeWidth(0.5f);
        repulsionLinePaint.setARGB(50, 230, 150, 150);
        repulsionLinePaint.setAntiAlias(true);

        boundsPaint = new Paint();
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(3f);
        boundsPaint.setARGB(100, 30, 250, 30);
        repulsionLinePaint.setAntiAlias(true);
    }

    /**
     * Call this to turn on "helpful" extra lines :D
     */
    public void enableDebugDraw() {
        debugDraw = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ForceDirectedView.this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        ForceDirectedView.this.viewHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    public void setData(List<ForceConnectedNode> nodes) {
        Timber.i("data: " + nodes);
        this.datasetNodes = nodes;
        final HashSet<ForceConnection> connections = new HashSet<>(nodes.size());
        final HashSet<ForceConnection> repulsions = new HashSet<>(nodes.size() * nodes.size());
        for (int i = 0; i < datasetNodes.size(); i++) {
            ForceConnectedNode node = datasetNodes.get(i);
            for (int j = 0; j < datasetNodes.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (node.getNeighbours().contains(j)) {
                    connections.add(new ForceConnection(i, j));
                } else {
                    repulsions.add(new ForceConnection(i, j));
                }
            }
        }
        uniqueConnections = new ArrayList<>(connections);
        uniqueRepulsions = new ArrayList<>(repulsions);
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        ListUtils.foreach(datasetNodes, node -> updateNodeBounds(node, nodeBounds));
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        viewBounds = new RectF(SCREEN_PADDING_PX, SCREEN_PADDING_PX, w - 2 * SCREEN_PADDING_PX, h - 2 * SCREEN_PADDING_PX);
    }

    private void performStateUpdate() {
        float maxDim = Math.max(nodeBounds.width(), nodeBounds.height());
        squareBounds.set(nodeBounds.left, nodeBounds.top, nodeBounds.left + maxDim, nodeBounds.top + maxDim);
        quadTree = new QuadTree<>(0, squareBounds);
        quadTree.insertAll(datasetNodes);

        for (ForceConnectedNode node : datasetNodes) {
            ForceHelper.applyForceForNode(node, quadTree);
        }

        for (ForceConnection connection : uniqueConnections) {
            ForceConnectedNode nodeA = datasetNodes.get(connection.from);
            ForceConnectedNode nodeB = datasetNodes.get(connection.to);
            ForceHelper.applyAttractionBetweenNodes(nodeA, nodeB);
        }
        for (ForceConnection connection : uniqueRepulsions) {
            ForceConnectedNode nodeA = datasetNodes.get(connection.from);
            ForceConnectedNode nodeB = datasetNodes.get(connection.to);
            ForceHelper.applyRepulsionBetweenNodes(nodeA, nodeB);
        }
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (ForceConnectedNode node : datasetNodes) {
            node.updatePosition(FORCE_FACTOR);
            node.clearForce();
            updateNodeBounds(node, nodeBounds);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datasetNodes.isEmpty() || viewWidth == 0) {
            // don't drawn if it's not ready
            return;
        }

        long started = System.currentTimeMillis();

        // update state
        performStateUpdate();

        if (canvas != null) {
            // draw
            drawVisualisation(viewWidth, viewHeight, CIRCLE_RADIUS, canvas);
        }

        long lastCheck = System.currentTimeMillis();
        long deltaTime = Math.min(lastCheck - started, 1000);
        long sleepTime = FRAME_PERIOD - deltaTime;
        if (sleepTime > 0) {
            long currentSleep = 0;
            while (currentSleep < sleepTime) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long current = System.currentTimeMillis();
                currentSleep += current - lastCheck;
                lastCheck = current;
            }
        } else if (sleepTime < 0) {
            while (sleepTime < 0) {
                performStateUpdate();
                sleepTime += FRAME_PERIOD;
            }
        }
        invalidate();
    }

    private void drawVisualisation(int viewWidth, int viewHeight,
                                   float nodeRadius, Canvas canvas) {
        canvas.drawColor(Color.argb(255, 31, 31, 31));
        matrix.reset();
        float scaleFactor = Math.min((float) viewWidth / nodeBounds.width(), (float) viewHeight / nodeBounds.height());
        matrix.setRectToRect(nodeBounds, viewBounds, Matrix.ScaleToFit.CENTER);
        canvas.setMatrix(matrix);
        linePaint.setStrokeWidth(1f / scaleFactor);
        if (debugDraw) {
            //            for (ForceConnection node : uniqueRepulsions) {
            //                ForceConnectedNode nodeA = datasetNodes.get(node.from);
            //                ForceConnectedNode nodeB = datasetNodes.get(node.to);
            //                float ax = nodeA.getX();
            //                float ay = nodeA.getY();
            //                float bx = nodeB.getX();
            //                float by = nodeB.getY();
            //                canvas.drawLine(ax, ay, bx, by, repulsionLinePaint);
            //            }
            drawBounds(quadTree, canvas, boundsPaint);
        }
        for (ForceConnection node : uniqueConnections) {
            ForceConnectedNode nodeA = datasetNodes.get(node.from);
            ForceConnectedNode nodeB = datasetNodes.get(node.to);
            float ax = nodeA.getX();
            float ay = nodeA.getY();
            float bx = nodeB.getX();
            float by = nodeB.getY();
            canvas.drawLine(ax, ay, bx, by, linePaint);
        }
        float drawRadius = nodeRadius / scaleFactor;
        for (ForceConnectedNode node : datasetNodes) {
            float x = node.getX();
            float y = node.getY();
            canvas.drawCircle(x, y, drawRadius, pointPaint);
        }
    }
}
