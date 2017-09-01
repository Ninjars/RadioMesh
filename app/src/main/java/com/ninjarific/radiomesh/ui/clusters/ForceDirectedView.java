package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;


public class ForceDirectedView extends SurfaceView implements Runnable {
    private static final int MAX_FPS = 40; //desired fps
    private static final int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
    private static final float CIRCLE_RADIUS = 8f;
    private static final double SPRING_FACTOR = 1;
    private static final double SPRING_DIVISOR = 0.1;
    private static final double REPEL_FACTOR = 0.5;
    private static final double FORCE_FACTOR = 0.1;

    private static RectF nodeBounds = new RectF();
    private static Matrix matrix = new Matrix();
    private boolean isRunning = false;

    private Thread updateThread;
    private Paint pointPaint;
    private Paint linePaint;
    private List<ForceConnectedNode> datasetNodes = Collections.emptyList();
    private List<ForceConnection> uniqueConnections = Collections.emptyList();
    private SurfaceHolder holder;

    private int viewWidth;
    private int viewHeight;

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
        pointPaint.setARGB(200, 225, 225, 225);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setARGB(150, 175, 175, 175);
        linePaint.setAntiAlias(true);

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                ForceDirectedView.this.viewWidth = width;
                ForceDirectedView.this.viewHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }

    public void setData(List<ForceConnectedNode> nodes) {
        Timber.d("data: " + nodes);
        this.datasetNodes = nodes;
        uniqueConnections = ListUtils.mapReduce(nodes, new ArrayList<>(),
                (node) -> ListUtils.map(node.getNeighbours(),
                        neighbourIndex -> new ForceConnection(node.getIndex(), neighbourIndex)),
                (currentConnections, newConnections) -> {
                    currentConnections.addAll(ListUtils.filter(newConnections, connection -> !currentConnections.contains(connection)));
                    return currentConnections;
                });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    private void performStateUpdate() {
        for (ForceConnection connection : uniqueConnections) {
            ForceConnectedNode nodeA = datasetNodes.get(connection.from);
            ForceConnectedNode nodeB = datasetNodes.get(connection.to);

            double dx = nodeB.getX() - nodeA.getX();
            double dy = nodeB.getY() - nodeA.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                continue;
            }

            double force = SPRING_FACTOR * Math.log(distance / SPRING_DIVISOR);
            double scaleFactor = Math.min(5, force / distance);
            double fx = scaleFactor * dx;
            double fy = scaleFactor * dy;
            nodeA.addForce(fx, fy);
        }
        for (int i = 0; i < datasetNodes.size(); i++) {
            ForceConnectedNode node = datasetNodes.get(i);

            for (int j = 0; j < datasetNodes.size(); j++) {
                if (i == j || node.getNeighbours().contains(j)) continue;
                ForceConnectedNode otherNode = datasetNodes.get(j);
                double dx = otherNode.getX() - node.getX();
                double dy = otherNode.getY() - node.getY();
                double distanceSquared = dx * dx + dy * dy;
                if (distanceSquared == 0) {
                    continue;
                }
                double force = REPEL_FACTOR / distanceSquared;
                double scaleFactor = Math.min(5, force / Math.sqrt(distanceSquared));
                double fx = -scaleFactor * dx;
                double fy = -scaleFactor * dy;
                node.addForce(fx, fy);
            }
        }
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (ForceConnectedNode node : datasetNodes) {
            node.updatePosition(FORCE_FACTOR);
            node.clearForce();
            updateNodeBounds(node, nodeBounds);
        }
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

    private static void drawVisualisation(List<ForceConnectedNode> dataset,
                                          Paint radioPaint, Paint linePaint, int viewWidth, int viewHeight,
                                          float nodeRadius, Canvas canvas) {
        canvas.drawColor(Color.argb(255, 31, 31, 31));
        float scaleFactor = Math.max((float) viewWidth / nodeBounds.width(), (float) viewHeight / nodeBounds.height());
        matrix.preTranslate(-nodeBounds.left - nodeBounds.width() / 2f, -nodeBounds.top - nodeBounds.height() / 2f);
        matrix.setScale(scaleFactor/2, scaleFactor/2);
        matrix.postTranslate(viewWidth / 2, viewHeight / 2);
        canvas.setMatrix(matrix);
        linePaint.setStrokeWidth(1f / scaleFactor);
        float drawRadius = nodeRadius / scaleFactor;
        for (ForceConnectedNode node : dataset) {
            float x = node.getX();
            float y = node.getY();
            canvas.drawCircle(x, y, drawRadius, radioPaint);
            for (int neighbourIndex : node.getNeighbours()) {
                ForceConnectedNode b = dataset.get(neighbourIndex);
                canvas.drawLine(x, y, b.getX(), b.getY(), linePaint);
            }
        }
    }

    @Override
    public void run() {
        Timber.d("run()");
        while (isRunning) {
            if (!holder.getSurface().isValid() || datasetNodes.isEmpty() || viewWidth == 0) {
                // don't drawn if it's not ready
                continue;
            }

            long started = System.currentTimeMillis();

            // update state
            performStateUpdate();

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                // draw
                drawVisualisation(datasetNodes, pointPaint, linePaint, viewWidth, viewHeight, CIRCLE_RADIUS, canvas);
                holder.unlockCanvasAndPost(canvas);
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
        }
        Timber.d("run ended");
    }

    public void pause() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                updateThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }

    public void resume() {
        isRunning = true;
        updateThread = new Thread(this);
        updateThread.start();
    }
}
