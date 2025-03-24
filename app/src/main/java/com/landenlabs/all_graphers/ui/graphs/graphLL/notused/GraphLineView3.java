/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL.notused;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.ui.graphs.graphLL.CubicSplineF;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphBaseView;

import java.util.ArrayList;


/**
 * Draw simple line graph with optional circular markers.
 */
public class GraphLineView3 extends GraphBaseView {

    public boolean isSmooth = true;
    private final static int DARKEN_ALPHA = 128;

    private final ArrayList<PointF> rawPoints = new ArrayList<>();
    public final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Matrix lineMatrix = new Matrix();

    public final Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path areaPath = new Path();

    public final Paint markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<PointF> markPoints = new ArrayList<>();
    private final PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final PorterDuffXfermode mult = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
    private boolean firstDraw = true;
    private float scaleX;
    private float scaleY;
    public float markRadius = 24;

    private final Paint darkenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF darkenMaskRect;
    private boolean darkenMaskIsPercent;

    private float mCurX = 0f;
    private float minX = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float mCurY = 0f;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;
    private float userMinY = Float.POSITIVE_INFINITY;
    private float userMaxY = Float.NEGATIVE_INFINITY;
    private float yScale = 10f;
    private float padHeight = 0f;

    private int lineColor = Color.WHITE;
    private int fillStartColor = Color.TRANSPARENT;
    private int fillEndColor = Color.WHITE;

    public GraphLineView3(Context context) {
        super(context);
        init(context, null, 0);
    }

    // ---------------------------------------------------------------------------------------------

    public GraphLineView3(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GraphLineView3(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    void init(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {

        int fillMaxColor = Color.WHITE;
        int fillMinColor = Color.TRANSPARENT;

        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.GraphLineView, defStyleAttr,
                android.R.style.Widget_SeekBar);
        fillMinColor = a.getColor(R.styleable.GraphLineView_fillMinColor, fillMinColor);
        fillMaxColor = a.getColor(R.styleable.GraphLineView_fillMaxColor, fillMaxColor);
        a.recycle();

        // lineColor = 0xff3D60A4;         //  context.getColor(R.color.graphLineColor);
        // fillStartColor = 0xff99bbff;    //  context.getColor(R.color.graphFillColor);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(context.getResources().getDimension(R.dimen.graph_line_width));     // 6.0fShould use Dimension res to scale by density.

        areaPaint.setStyle(Paint.Style.FILL);
        areaPaint.setAntiAlias(true);

        markPaint.setColor(Color.WHITE);
        // markPaint.setAntiAlias(true);
        markRadius = context.getResources().getDimension(R.dimen.graph_line_radius);
        firstDraw = true;

        if (isInEditMode()) {
            int cnt = 10;
            // setSeriesCfg(1, 0xffffc0c0, 0xff802020, "Mixed");
            for (int idx = 0; idx < cnt; idx++) {
                int series = (idx < 5 || idx > 8) ? 0 : 1;
                addPoint(idx, yScale * (cnt - idx) / cnt);
            }
        }
    }

    public void setDarkenMask(RectF darkenMaskRect, boolean isPercent) {
        this.darkenMaskIsPercent = isPercent;
        this.darkenMaskRect = darkenMaskRect;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (firstDraw && getHeight() > 0) {
            firstDraw = false;

            float deltaY = Math.max(yScale, maxY - minY);
            scaleX = getWidth() / Math.max(1, mCurX);
            float percentHeight = 1.00f;  // use only part of the view height, leave some padding.
            padHeight = getHeight() * (1f - percentHeight) / 2f;
            scaleY = percentHeight * getHeight() / deltaY;

            lineMatrix.reset();
            lineMatrix.setScale(scaleX, scaleY);
            lineMatrix.postTranslate(0f, -minY * scaleY + padHeight);
            lineMatrix.postScale(1, -1, getWidth() / 2f, getHeight() / 2f);
            linePath.transform(lineMatrix);

            areaPath.transform(lineMatrix);
            // height only available after layout completes.
            Shader shadow = new LinearGradient(
                    /* shade increases with height
                    0, 0,
                    0, getHeight(),     // intensity increases along y-axis
                     */

                    0, 0,           // intensity increases along x-axis
                    getWidth(), 0,

                    fillStartColor, fillEndColor,
                    Shader.TileMode.CLAMP);
            areaPaint.setShader(shadow);

            if (darkenMaskIsPercent && darkenMaskRect != null) {
                darkenMaskIsPercent = false;
                darkenMaskRect = new RectF(
                        darkenMaskRect.left * getWidth(), darkenMaskRect.top * getHeight(),
                        darkenMaskRect.right * getWidth(), darkenMaskRect.bottom * getHeight());
            }

            if (darkenMaskRect != null) {
                // Currently PorterDuff xfer only works if hardware acceleration is disabled !
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                darkenPaint.setColor(lineColor);
                darkenPaint.setAlpha(DARKEN_ALPHA);
            }
        }
    //zz    canvas.drawPath(areaPath, areaPaint);

        if (darkenMaskRect != null) {
            darkenPaint.setXfermode(clear);
            canvas.drawRect(darkenMaskRect, darkenPaint);
        }

        canvas.drawPath(linePath, linePaint);

        float[] pts = new float[2];
        for (PointF point : markPoints) {
            pts[0] = point.x;
            pts[1] = point.y;
            lineMatrix.mapPoints(pts);
            canvas.drawCircle(pts[0],  pts[1], markRadius, markPaint);
            // canvas.drawCircle(point.x * scaleX,
            //        getHeight() - (point.y - minY ) * scaleY - padHeight, markRadius, markPaint);
        }

        if (darkenMaskRect != null) {
            darkenPaint.setXfermode(mult);
            canvas.drawRect(darkenMaskRect, darkenPaint);
        }
    }

    @Override
    public void setSeriesCfg(int series, int minColor, int maxColor, @NonNull String name) {
        // TODO - implement
    }

    @Override
    public void addSeriesPoint(int series, float x, float y) {
        // TODO - implement
    }

    public void setStart(float x, float y) {
        if (isSmooth) {
            rawPoints.clear();
            rawPoints.add(new PointF(x, y));
        } else {
            linePath.moveTo(x, y);
        }
        mCurX = x;
        mCurY = y;
        minY = maxY = y;
        minX = maxX = x;
    }

    public void addPoint(float x, float y) {
        if (isSmooth) {
            rawPoints.add(new PointF(x, y));
            // linePath.quadTo(mCurX, mCurY, x, y);
            // linePath.quadTo(mCurX, mCurY, (x*3 + mCurX) / 4, (y*3 + mCurY) / 4);
            // linePath.quadTo(mCurX, mCurY, (x*3 + mCurX) / 4, (y*3 + mCurY) / 4);
            // linePath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2);
            // linePath.quadTo(x,y, x, y);
            // linePath.quadTo((x + mCurX) / 2, (y + mCurY) / 2, x, y);
        } else
            linePath.lineTo(x, y);

        mCurX = x;
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        mCurY = y;
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
    }

    public void setEnd() {
        if (isSmooth) {
            linePath.reset();
            ArrayList<PointF> splinedPoints = new ArrayList<>();
            CubicSplineF.splinePointFs(rawPoints, splinedPoints);
            PointF pt1 = splinedPoints.get(0);
            linePath.moveTo(pt1.x, pt1.y);
            for (int idx = 1; idx < splinedPoints.size(); idx++) {
                pt1 = splinedPoints.get(idx);
                    // Clamp spline line to original data range.
                if (pt1.x >= minX && pt1.x <= maxX) {
                    linePath.lineTo(pt1.x, pt1.y);
                }
            }
        } else {
            linePath.lineTo(mCurX, mCurY);
        }

        if (!Float.isInfinite(userMinY))
            minY = userMinY;
        if (!Float.isInfinite(userMaxY))
            maxY = userMaxY;

        // Close line path to form area path, used for shading.

        areaPath.rewind();
        areaPath.addPath(linePath);
        /* area snap to x-axis
        areaPath.lineTo(mCurX, minY - 1);
        areaPath.lineTo(0, minY - 1);
         */
        // Area snap to y-axis
        areaPath.lineTo(0, mCurY);
        areaPath.lineTo(0, maxY);
        areaPath.close();

        firstDraw = true;
        invalidate();
    }

    public void addMarker(float x, float y) {
        markPoints.add(new PointF(x, y));
    }

    public boolean hasPoints() {
        return isSmooth && !rawPoints.isEmpty();
    }
    public void clear() {
        firstDraw = true;
        linePath.rewind();
        areaPath.rewind();
    }

    @Override
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
        if (color != this.fillStartColor) {
            this.fillStartColor = color;
            firstDraw = true;
        }
    }

    @Override
    public void setFillEndColor(@ColorInt int color) {
        if (color != this.fillEndColor) {
            this.fillEndColor = color;
            firstDraw = true;
        }
    }

    private static class SeriesCfg {
        @ColorInt
        int minColor;
        @ColorInt
        int maxColor;
        String name;

        SeriesCfg(@ColorInt int minColor, @ColorInt int maxColor, String name) {
            this.minColor = minColor;
            this.maxColor = maxColor;
            this.name = name;
        }
    }
}
