package com.ninjarific.radiomesh.ui.clusters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ninjarific.radiomesh.database.RadioPoint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import timber.log.Timber;


public class ForceDirectedView extends SurfaceView implements Runnable {
    private static final int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period

    private boolean isRunning = false;
    private Thread updateThread;

    private Paint pointPaint;
    private Paint linePaint;
    private Point center;
    private float radius;
    private List<RadioPoint> dataset;
    private SurfaceHolder holder;

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

    @Override
    public void run() {
        while (isRunning) {
            if (!holder.getSurface().isValid()) {
                // don't drawn if it's not ready
                continue;
            }

            long started = System.currentTimeMillis();

            // update state

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                // draw
                holder.unlockCanvasAndPost(canvas);
            }

            long lastCheck = System.currentTimeMillis();
            long deltaTime = lastCheck - started;
            long sleepTime = FRAME_PERIOD - deltaTime;
            long currentSleep = 0;
            if (sleepTime > 0) {
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
            }
        }
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
