/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL.notused;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.ui.data.UiUtils;

/**
 * Custom View draws an animated arc with text in the middle.
 */
public class ArcView extends View {
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintArcFg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintArcBg = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float radius = -1; // Utils.dpToPixel(80);
    private float radiusPercent = 0.90f;
    private int txtSize = (int) UiUtils.dpToPixel(40);
    private int arcWidth = (int) UiUtils.dpToPixel(15);

    @ColorInt private int arcBgColor = 0x3dffffff; // R.color.white5
    @ColorInt private int arcFgColor1 = 0x80800000;
    @ColorInt private int arcFgColor2 = 0xffff3030;

    private String fmtProgress = "%.0f%%";
    private float progress = 50;
    private float progressSecond = 100;
    
    private float arcStartDegrees = 135;
    private float arcSweepDegrees = 270;
    private RectF arcRectF = new RectF();
    private LinearGradient shader;

    // ---------------------------------------------------------------------------------------------

    public ArcView(Context context) {
        super(context);
        init(null, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        shader = null;
        paintArcFg.setShader(null);
        super.onDetachedFromWindow();
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ArcView, defStyleAttr,
                android.R.style.Widget_SeekBar);

        progress = a.getFloat(R.styleable.ArcView_progress, progress);
        radiusPercent = a.getFloat(R.styleable.ArcView_radiusPercent, 80) / 100f;
        radius = a.getDimensionPixelSize(R.styleable.ArcView_radius, -1);
        txtSize = a.getDimensionPixelSize(R.styleable.ArcView_textSize, txtSize);
        if (a.hasValue(R.styleable.ArcView_progressFormat)) {
            fmtProgress = a.getString(R.styleable.ArcView_progressFormat);
        }

        arcStartDegrees = a.getFloat(R.styleable.ArcView_arcStartDegrees, arcStartDegrees);
        arcSweepDegrees = a.getFloat(R.styleable.ArcView_arcSweepDegrees, arcSweepDegrees);
        arcWidth = a.getDimensionPixelSize(R.styleable.ArcView_arcWidth, arcWidth);
        arcBgColor = a.getColor(R.styleable.ArcView_arcBgColor, arcBgColor);
        arcFgColor1 = a.getColor(R.styleable.ArcView_arcFgColor1, arcFgColor1); // 0x80800000, 0xffff3030
        arcFgColor2 = a.getColor(R.styleable.ArcView_arcFgColor2, arcFgColor2);
        a.recycle();

        paintText.setTextSize(txtSize);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(Color.WHITE);
        paintText.setStyle(Paint.Style.FILL);

        // paint.setColor(Color.parseColor("#E91E63"));
        paintArcFg.setStyle(Paint.Style.STROKE);
        paintArcFg.setStrokeCap(Paint.Cap.ROUND);
        paintArcFg.setStrokeWidth(arcWidth);

        // paintArcFg.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        paintArcBg.setColor(arcBgColor);
        paintArcBg.setStyle(Paint.Style.STROKE);
        //    paintArcBg.setStrokeCap(Paint.Cap.ROUND);
        paintArcBg.setStrokeWidth(arcWidth /* + UtilsUi.dpToPixel(2) */);

        // Could also draw inverse dash on top of arc.
    }

    private void buildArc() {
        // Add gaps in arc at 25%
        float arcLen = (float) (2 * Math.PI * radius * arcSweepDegrees / 360f);
        float gapP = 0.01f;
        float drawP = 0.25f;

        float[] dashParts = new float[6];
        dashParts[0] = (drawP - gapP / 2) * arcLen;
        dashParts[1] = gapP * arcLen;
        dashParts[2] = (drawP - gapP) * arcLen;
        dashParts[3] = gapP * arcLen;
        dashParts[4] = (drawP - gapP) * arcLen;
        dashParts[5] = gapP * arcLen;
        DashPathEffect dashEffect = new DashPathEffect(dashParts, 0);
        paintArcFg.setPathEffect(dashEffect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float newRadius = Math.min(widthSize, heightSize) / 2 * radiusPercent - arcWidth;
        if (radius < 0 && newRadius > 0) {
            radius = newRadius;
        }
        // buildArc();  // Not used in this app
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setProgress(float progress, @NonNull String fmtProgress) {
        this.progress = progress;
        this.fmtProgress = fmtProgress;
        invalidate();
    }

    public void setFmtProgress(@NonNull String fmtProgress) {
        this.fmtProgress = fmtProgress;
        invalidate();
    }

    public float getSecondProgress() {
        return progressSecond;
    }

    public void setSecondProgress(float progressSecond) {
        this.progressSecond = progressSecond;
        invalidate();
    }

    public void setArcColor1(@ColorInt int color) {
        arcFgColor1 = color;
        shader = null;
    }

    public void setArcColor2(@ColorInt int color) {
        arcFgColor2 = color;
        shader = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        if (shader == null) {
            shader = new LinearGradient(0, 0, getWidth(), 0,
                    arcFgColor1, arcFgColor2, Shader.TileMode.CLAMP);
        }

        paintArcFg.setShader(shader);
        arcRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // paint.setColorFilter()
        canvas.drawArc(arcRectF, arcStartDegrees, progressSecond * arcSweepDegrees / 100, false, paintArcBg);
        canvas.drawArc(arcRectF, arcStartDegrees, progress * arcSweepDegrees / 100, false, paintArcFg);
        String msg = String.format(fmtProgress, progress);
        if (msg.length() > 0) {
            canvas.drawText(msg, centerX, centerY - (paintText.ascent() + paintText.descent()) / 2, paintText);
        }
    }
}