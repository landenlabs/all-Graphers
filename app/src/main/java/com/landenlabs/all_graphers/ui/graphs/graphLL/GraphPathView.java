/*
 *
 * Copyright © 2024 The Weather Company. All rights reserved.
 *
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class GraphPathView extends View {

    public Path path;
    public Paint paint;

    public GraphPathView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphPathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public GraphPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setColors(int[] colors) {
        if (colors != null && colors.length > 1) {
            Shader lineShader = new LinearGradient(
                    0, 0,           // intensity increases along x-axis
                    getWidth(), 0,
                    colors,
                    null,
                    Shader.TileMode.CLAMP);
            paint.setShader(lineShader);
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int y = getHeight() / 2;
        path.rewind();
        path.moveTo(0, y);
        path.lineTo(getWidth(), y);
        // path.close();
        paint.setStrokeWidth(getHeight());
        canvas.drawPath(path, paint);
    }
}