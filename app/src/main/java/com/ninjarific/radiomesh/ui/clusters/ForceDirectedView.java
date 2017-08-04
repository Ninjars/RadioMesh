package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import timber.log.Timber;


public class ForceDirectedView extends SurfaceView implements Runnable {
    private static final int MAX_FPS = 40; //desired fps
    private static final int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period
    private static final float CIRCLE_RADIUS = 10f;

    private static final double SPRING_LENGTH = 0.1;
    private static final double SPRING_FACTOR = 1;
    private static final double SPRING_DIVISOR = 0.1;
    private static final double REPEL_FACTOR = 0.5;
    private static final double FORCE_FACTOR = 0.1;

    private boolean isRunning = false;
    private Thread updateThread;

    private Paint pointPaint;
    private Paint linePaint;
    private List<ForceConnectedNode> dataset;
    private SurfaceHolder holder;
    private int width;
    private int height;
    private double edgeLength;
    private float yScale;

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

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {}

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                ForceDirectedView.this.width = width;
                ForceDirectedView.this.height = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}
        });
    }

    public void setData(List<ForceConnectedNode> nodes) {
        Timber.d("data: " + nodes);
        this.dataset = nodes;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        yScale = w/(float)h;
        edgeLength = SPRING_LENGTH * width;
    }

    private void performStateUpdate() {
        for (ForceConnectedNode node : dataset) {
            List<Integer> neighbours = node.getNeighbours();
            for (int neighbourIndex : neighbours) {
                ForceConnectedNode neighbour = dataset.get(neighbourIndex);
                // TODO: optimise to avoid calculating force twice for every pair
                double dx = neighbour.getX() - node.getX();
                double dy = neighbour.getY() - node.getY();
                double distance = Math.sqrt(dx*dx + dy*dy);
                if (distance == 0) {
                    continue;
                }

                double force = SPRING_FACTOR * Math.log(distance / SPRING_DIVISOR);
                double scaleFactor = Math.min(5, force / distance);
                double fx = scaleFactor * dx;
                double fy = scaleFactor * dy;
                node.addForce(fx, fy);
            }
            for (int i = 0; i < dataset.size(); i++) {
                if (neighbours.contains(i)) {
                    continue;
                }
                ForceConnectedNode otherNode = dataset.get(i);
                if (otherNode == node) {
                    continue;
                }
                double dx = otherNode.getX() - node.getX();
                double dy = otherNode.getY() - node.getY();
                double distanceSquared = dx*dx + dy*dy;
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
        for (ForceConnectedNode node : dataset) {
            node.updatePosition(FORCE_FACTOR / width);
            node.clearForce();
        }
    }

    private static void drawVisualisation(List<ForceConnectedNode> dataset,
                                          Paint radioPaint, Paint linePaint, int viewWidth, int viewHeight,
                                          float nodeRadius, float yScale, Canvas canvas) {
        canvas.drawColor(Color.argb(255, 31, 31, 31));
        for (ForceConnectedNode node : dataset) {
            float x = node.getX() * viewWidth;
            float y = node.getY() * viewHeight;
            canvas.drawCircle(x, y, nodeRadius, radioPaint);
            for (int neighbourIndex : node.getNeighbours()) {
                ForceConnectedNode b = dataset.get(neighbourIndex);
                canvas.drawLine(x, y, b.getX() * viewWidth, b.getY() * viewHeight, linePaint);
            }
        }
    }

    @Override
    public void run() {
        Timber.d("run()");
        while (isRunning) {
            if (!holder.getSurface().isValid() || dataset.isEmpty() || width == 0) {
                // don't drawn if it's not ready
                continue;
            }

            long started = System.currentTimeMillis();

            // update state
            performStateUpdate();

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                // draw
                drawVisualisation(dataset, pointPaint, linePaint, width, height, CIRCLE_RADIUS, yScale, canvas);
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
