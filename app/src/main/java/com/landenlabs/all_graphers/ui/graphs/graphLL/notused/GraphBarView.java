/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL.notused;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphBaseView;

import java.util.ArrayList;


/**
 * Draw simple bar graph.
 * Supports multiple series
 */
@SuppressWarnings({"SameParameterValue", "FieldCanBeLocal", "unused"})
public class GraphBarView extends GraphBaseView {

    private static final String TAG = GraphBarView.class.getSimpleName();
    private static final boolean DRAW_LINES = false;    // for experiments

    private final ArrayList<Path> debugPaths = new ArrayList<>();
    private final ArrayList<Path> linePaths = new ArrayList<>();
    private final ArrayList<Path> bgPaths = new ArrayList<>();
    private final ArrayList<SeriesCfg> seriesCfgs = new ArrayList<>();

    private final Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Matrix lineMatrix = new Matrix();
    public float barWidth = 24f;        // R.dimen.bar_graph_data_width
    public float barGap = 9f;           // graph_bar_gap
    public float barRadius = barWidth / 2;
    private boolean firstDraw = false;
    private float scaleX;
    private float scaleY;
    private float mCurX = 0f;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;
    private float userMinY = Float.POSITIVE_INFINITY;
    private float userMaxY = Float.NEGATIVE_INFINITY;
    private float yScale = 100f;
    private float padHeight = 0f;
    private @ColorInt
    int lineColor = Color.WHITE;

    public GraphBarView(Context context) {
        super(context);
        init(context, null, 0);
    }

    // ---------------------------------------------------------------------------------------------

    public GraphBarView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GraphBarView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private  void roundedBotRect(Path path, float left, float top, float right, float bottom, float rad) {
        float width = right - left;
        float widthHalf = width/2;
        if (rad > widthHalf) rad = widthHalf;
        float arcHeight = rad;  // = rad - (float)Math.sqrt( rad*rad - widthHalf*widthHalf );
        arcHeight /= scaleY;    // adjust height to deal with aspect ratio of rendered graph.

        // Shrink height for curved top & bottom.
        top += arcHeight;
        bottom -= arcHeight;
        float height = bottom - top;
        if (height <= 0)
            return;

        float heightMinusCorners = Math.max(0, height - 2*arcHeight);

        path.moveTo(left, top + arcHeight);
        path.arcTo(left, top - arcHeight, right, top + arcHeight, 180, 180, false);
        path.rLineTo(0, heightMinusCorners);
        path.arcTo(left, bottom - arcHeight, right, bottom + arcHeight, 0, 180, false);
        path.rLineTo(0, -heightMinusCorners);
    }

    @SuppressWarnings("unused")
    private void init(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        barWidth = getResources().getDimensionPixelSize(R.dimen.graph_bar_width);
        barGap = getResources().getDimensionPixelSize(R.dimen.graph_bar_gap);
        barRadius = barWidth / 2;

        int fillMaxColor = Color.WHITE;
        int fillMinColor = Color.TRANSPARENT;

        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.GraphBarView, defStyleAttr,
                android.R.style.Widget_SeekBar);
        yScale = a.getFloat(R.styleable.GraphBarView_yScale, yScale);
        barWidth = a.getDimension(R.styleable.GraphBarView_barWidth, barWidth);
        barGap = a.getDimension(R.styleable.GraphBarView_barGap, barGap);
        barRadius = a.getDimension(R.styleable.GraphBarView_barRadius, barWidth / 2);
        fillMinColor = a.getColor(R.styleable.GraphBarView_fillMinColor, fillMinColor);
        fillMaxColor = a.getColor(R.styleable.GraphBarView_fillMaxColor, fillMaxColor);
        seriesCfgs.add(new SeriesCfg(fillMinColor, fillMaxColor, "Rain"));
        a.recycle();

        lineColor = 0xffff5555; // context.getColor(R.color.graphBarColor);

        if (DRAW_LINES) {
            linePaint.setColor(lineColor);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeCap(Paint.Cap.ROUND);
            linePaint.setStrokeWidth(barWidth);
        } else {
            linePaint.setColor(fillMaxColor);
            linePaint.setStyle(Paint.Style.FILL);
        }

        debugPaint.setColor(Color.RED);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(3);

        linePaint.setAntiAlias(true);
        firstDraw = true;
        if (isInEditMode()) {
            addDebugSeries(3*7);
            addDebugPaths(yScale);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        debugPaths.clear();
        linePaths.clear();
        bgPaths.clear();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (firstDraw) {
            minY = 0;   // Force base lines as minimum
            float deltaY = Math.max(yScale, maxY - minY);
            // scaleX = getWidth() / Math.max(1, mCurX );
            scaleX = 1.0f;
            float percentHeight = 1.00f;  // use only part of the view height, leave some padding.
            padHeight = getHeight() * (1f - percentHeight) / 2f;
            scaleY = percentHeight * getHeight() / deltaY;
            // scaleX = scaleY = Math.min(scaleX, scaleY);
            lineMatrix.reset();
            lineMatrix.setScale(scaleX, scaleY);
            int startX = getPaddingStart();
            lineMatrix.postTranslate(startX, -minY * scaleY + padHeight);

            Shader shaderBg = new LinearGradient(0, 0, 0, getHeight(),
                    0xff303030, 0xff303030, Shader.TileMode.CLAMP);
            bgPaint.setShader(shaderBg);
        }

        for (int idx = 0; idx < linePaths.size() && idx < seriesCfgs.size(); idx++) {
            Path path = bgPaths.get(idx);
            if (firstDraw)
                path.transform(lineMatrix);
            canvas.drawPath(path, bgPaint);

            path = linePaths.get(idx);
            if (firstDraw)
                path.transform(lineMatrix);
            linePaint.setShader(seriesCfgs.get(idx).getShader(getHeight()));
            canvas.drawPath(path, linePaint);
        }

        for (int idx = 0; idx < debugPaths.size(); idx++) {
            Path path = debugPaths.get(idx);
            if (firstDraw)
                path.transform(lineMatrix);
            canvas.drawPath(path, debugPaint);
        }

        firstDraw = false;
    }

    public void setSeriesCfg(int series, @ColorInt int minColor, @ColorInt int maxColor, @NonNull String name) {
        while (series >= seriesCfgs.size()) {
            seriesCfgs.add(null);
        }
        seriesCfgs.set(series, new SeriesCfg(minColor, maxColor, name));
    }

    /**
     * Add data value on a series.
     *
     * @param x      0...n
     * @param y      magnitude of value
     * @param series Each series has its own color.
     */
    public void addSeriesPoint(int series, float x, float y) {
        x = x * (barWidth + barGap);

        while (series >= linePaths.size()) {
            linePaths.add(new Path());
            bgPaths.add(new Path());
        }
        Path linePath = linePaths.get(series);

        if (DRAW_LINES) {
            x += barWidth / 2;
            linePath.moveTo(x, 0);
            linePath.lineTo(x, y);
        } else {
            float barDiameter = 2 * barRadius;  // add space for rounded top and bottom.
            float maxY = 99 - barDiameter;
            float barSize = (y / 100f * maxY) + barDiameter;
            if (y != 0)
                roundedBotRect(linePath, x, 99 - barSize, x + barWidth, 99, barRadius);
            roundedBotRect(bgPaths.get(series), x, 0, x + barWidth , 99, barRadius);
        }

        mCurX = x;
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
    }

    public void setEnd() {
        firstDraw = true;
    }

    private void addDebugPaths(float maxValue) {
        debugPaths.clear();
        debugPaths.add(new Path());
        Path linePath = debugPaths.get(0);
        linePath.moveTo(0,0);
        linePath.lineTo(mCurX, maxValue);
        linePath.moveTo(0, maxValue);
        linePath.lineTo(mCurX, 0);

        float x=mCurX/2;
        float r=maxValue/2;
        float y=maxValue-r;
        linePath.moveTo(x, y);
        linePath.arcTo(x-r, y-r, x+r, y+r, -90, 270, false);
    }

    public void addDebugSeries(int numPnts) {
        // graph_bar_rain graph_bar_mixed graph_bar_snow
        setSeriesCfg(0, 0xff47A25D, 0xff47A25D, "Rain");
        setSeriesCfg(1, 0xff3B9CBB, 0xff3B9CBB, "Snow");
        setSeriesCfg(2, 0xffB7678F, 0xffB7678F, "Mixed");
        for (int idx = 0; idx < numPnts; idx++) {
            int series = idx / (numPnts/3);
            addSeriesPoint(series, idx, yScale * (numPnts - idx) / numPnts);
        }
    }

    public void clear() {
        firstDraw = true;
        for (Path linePath : linePaths) {
            linePath.reset();
        }
        for (Path bgPath : bgPaths) {
            bgPath.reset();
        }
        if (scaleY == 0) {
            scaleY = Math.max(1.0f, ourHeight() / 100f);
        }
    }

    public int ourHeight() {
        return (getHeight() > 0) ? getHeight() : Math.max(getLayoutParams().height, 0);
    }

    public float getMinY() {
        return minY;
    }

    @Override
    public void setMinY(float minY) {
        if (minY != this.userMinY) {
            this.minY = this.userMinY = minY;
            firstDraw = true;
        }
    }

    @Override
    public float getMaxY() {
        return maxY;
    }

    @Override
    public void setMaxY(float maxY) {
        if (maxY != this.userMaxY) {
            this.maxY = this.userMaxY = maxY;
            firstDraw = true;
        }
    }

    @Override
    public void setLineColor(@ColorInt int color) {
        if (color != this.lineColor) {
            this.lineColor = color;
            firstDraw = true;
        }
    }

    @Override
    public void setFillStartColor(@ColorInt int color) {
    }

    public void setFillEndColor(@ColorInt int color) {
    }

    // =============================================================================================
    private static class SeriesCfg {
        @ColorInt
        int minColor;
        @ColorInt
        int maxColor;
        String name;

        Shader shader;
        int height;

        SeriesCfg(@ColorInt int minColor, @ColorInt int maxColor, String name) {
            this.minColor = minColor;
            this.maxColor = maxColor;
            this.name = name;
        }

        Shader getShader(int height) {
            if (shader == null && this.height != height) {
                shader = new LinearGradient(0, 0, 0, height,
                        maxColor, minColor, Shader.TileMode.CLAMP);
            }
            return shader;
        }
    }
}
