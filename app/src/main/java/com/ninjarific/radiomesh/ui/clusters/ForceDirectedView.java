package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.utils.Bounds;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import timber.log.Timber;

public class ForceDirectedView extends View {
    private static final int MAX_FPS = 10; //desired fps
    private static final int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
    private static final float CIRCLE_RADIUS = 8f;
    private static final double FORCE_FACTOR = 1000;
    private static final float SCREEN_PADDING_PX = 16;

    private static final double NODE_WORLD_SIZE = 10000;
    private static final double NODE_FORCE_FACTOR = 0.2;
    private static final double NODE_OPTIMAL_DISTANCE = 1;

    private RectF viewBounds = new RectF();
    private Matrix matrix = new Matrix();
    private RectF nodeBounds = new RectF();
    private QuadTree<ForceConnectedNode> quadTree;

    private Paint pointPaint;
    private Paint linePaint;
    private Paint boundsPaint;
    private Paint debugTextPaint;
    private List<ForceConnectedNode> datasetNodes = Collections.emptyList();
    private List<ForceConnection> uniqueConnections = Collections.emptyList();
    private boolean debugDraw = false;

    private int viewWidth;
    private int viewHeight;
    private NodeForceCalculator forceCalculator;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private boolean isUpdating;

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
            canvas.drawRect(quadTree.getBounds().asRectF(), paint);
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

        boundsPaint = new Paint();
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(3f);
        boundsPaint.setARGB(100, 30, 250, 30);
        boundsPaint.setAntiAlias(true);

        debugTextPaint = new Paint();
        debugTextPaint.setTextSize(30f);
        debugTextPaint.setARGB(100, 30, 250, 30);
        debugTextPaint.setAntiAlias(true);

        forceCalculator = new NodeForceCalculator(NODE_FORCE_FACTOR, NODE_OPTIMAL_DISTANCE);

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
        stopUpdateLoop();
        this.datasetNodes = nodes;
        final HashSet<ForceConnection> connections = new HashSet<>(nodes.size());
        for (int i = 0; i < datasetNodes.size(); i++) {
            ForceConnectedNode node = datasetNodes.get(i);
            for (int j = 0; j < datasetNodes.size(); j++) {
                if (i != j && node.getNeighbours().contains(j)) {
                    connections.add(new ForceConnection(i, j));
                }
            }
        }
        uniqueConnections = new ArrayList<>(connections);
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        ListUtils.foreach(datasetNodes, node -> updateNodeBounds(node, nodeBounds));
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        startUpdateLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopUpdateLoop();
        handlerThread.quitSafely();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        viewBounds = new RectF(SCREEN_PADDING_PX, SCREEN_PADDING_PX, w - 2 * SCREEN_PADDING_PX, h - 2 * SCREEN_PADDING_PX);
    }

    private void performStateUpdate(double timeDelta) {
        float maxDim = Math.max(nodeBounds.width(), nodeBounds.height());
        Bounds squareBounds = new Bounds(nodeBounds.left, nodeBounds.top, nodeBounds.left + maxDim, nodeBounds.top + maxDim);
        quadTree = new QuadTree<>(0, squareBounds);
        quadTree.insertAll(datasetNodes);

        for (ForceConnectedNode node : datasetNodes) {
            forceCalculator.repelNode(node, quadTree);
//            ForceHelper.applyForceForNode(node, quadTree);
        }

        for (ForceConnection connection : uniqueConnections) {
            ForceConnectedNode nodeA = datasetNodes.get(connection.from);
            ForceConnectedNode nodeB = datasetNodes.get(connection.to);
            forceCalculator.attractNodes(nodeA, nodeB);
//            ForceHelper.applyAttractionBetweenNodes(nodeA, nodeB);
        }
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (ForceConnectedNode node : datasetNodes) {
            node.updatePosition(FORCE_FACTOR / timeDelta);
            node.clearForce();
            updateNodeBounds(node, nodeBounds);
        }
    }

    private void stopUpdateLoop() {
        if (backgroundHandler != null) {
            backgroundHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startUpdateLoop() {
        stopUpdateLoop();
        isUpdating = true;
        backgroundHandler.post(() -> {
            while (isUpdating) {
                long updateTime = SystemClock.uptimeMillis();

                long lastCheck = SystemClock.uptimeMillis();
                long deltaTime = Math.min(lastCheck - updateTime, 1000);
                long sleepTime = FRAME_PERIOD - deltaTime;
                if (sleepTime > 0) {
                    long currentSleep = 0;
                    while (currentSleep < sleepTime) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        long current = SystemClock.uptimeMillis();
                        currentSleep += current - lastCheck;
                        lastCheck = current;
                    }
                }

                performStateUpdate(Math.max(SystemClock.uptimeMillis() - updateTime, 500));
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datasetNodes.isEmpty() || viewWidth == 0) {
            // don't drawn if it's not ready
            return;
        }

//        long started = System.currentTimeMillis();

        if (canvas != null) {
            // draw
            drawVisualisation(viewWidth, viewHeight, CIRCLE_RADIUS, canvas);
            invalidate();
        }

//        long lastCheck = SystemClock.uptimeMillis();
//        long deltaTime = Math.min(lastCheck - started, 1000);
//        long sleepTime = FRAME_PERIOD - deltaTime;
//        if (sleepTime > 0) {
//            long currentSleep = 0;
//            while (currentSleep < sleepTime) {
//                long current = SystemClock.uptimeMillis();
//                currentSleep += current - lastCheck;
//                lastCheck = current;
//            }
//        } else if (sleepTime < 0) {
//            while (sleepTime < 0) {
//                performStateUpdate();
//                sleepTime += FRAME_PERIOD;
//            }
//        }
    }

    private void drawVisualisation(int viewWidth, int viewHeight,
                                   float nodeRadius, Canvas canvas) {
        canvas.drawColor(Color.argb(255, 31, 31, 31));
        float scaleFactor = Math.min((float) viewWidth / nodeBounds.width(), (float) viewHeight / nodeBounds.height());
        if (debugDraw) {
            canvas.drawText(String.valueOf(scaleFactor) + " : " +
                    nodeBounds.toShortString(), 0, viewHeight, debugTextPaint);
        }
        matrix.reset();
        matrix.setRectToRect(nodeBounds, viewBounds, Matrix.ScaleToFit.CENTER);
        canvas.setMatrix(matrix);
        linePaint.setStrokeWidth(1f / scaleFactor);
        boundsPaint.setStrokeWidth(1f / scaleFactor);
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
