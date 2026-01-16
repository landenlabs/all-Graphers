/*
 *
 * Copyright © 2024 The Weather Company. All rights reserved.
 *
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL;


import static com.landenlabs.all_graphers.ui.data.Ui.findParent;
import static com.landenlabs.all_graphers.ui.data.Ui.layoutFromStyle;
import static com.landenlabs.all_graphers.ui.graphs.GraphData.COLOR_X_AXIS;
import static com.landenlabs.all_graphers.ui.graphs.GraphData.NORMALIZE_X_AXIS;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.ui.data.Ui;
import com.landenlabs.all_graphers.ui.graphs.GraphData;
import com.landenlabs.all_graphers.ui.graphs.GraphType;
import com.landenlabs.all_graphers.ui.logger.ALog;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manage creation of a line graph -
 *    + Overlay three views (adjust margins)
 *          y-axis with background
 *          x-axis
 *          graph
 *    + Build the y-axis labels and background line
 *    + Build the x-axis labels
 *    + Collect and normalize data (adjust x-axis range)
 */
public class GraphLineMgr {

    // Cfg defines how graph is presented.
    public static class Cfg {
        // Main layout components
        public @IdRes int resYaxisBg;
        public @IdRes int resChart;
        public @IdRes int resXaxis;
        public @IdRes int resXtitle;

        // Styles and drawables for axis and background
        public @StyleRes int resYaxisStyle;         // R.style.text14
        public @StyleRes int resXaxisStyle;         // R.style.vgraphXaxis
        public @DrawableRes int resBgRowDrawable;   // R.drawable.vdashline

        // Colors
        public @ColorInt int lineColor;    // Color.WHITE);
        public @ColorInt int markerColor;

        public int numRows = 11;            // ex: 10,000 9,000 ... 1,000
        public int numCols = 11;            // ex: 0 10 20 ... 100

        public int rowHeightPx;             // ex: context.getResources().getDimensionPixelSize(R.dimen.rowHeight);
        public int lblPadRightPx;           // ex: toPx(5);
        public int lblWidthPx;              // ex: context.getResources().getDimensionPixelSize(R.dimen.yLabelWidth) - lblPadRightPx;
        public int yAxisBgStartMarginPx;    // ex: toPx(5)

        public int markerRadiusPx;          // ex: 15;
        public int lineWidthPx;             // ex: 10
    }

    private GridLayout vgraphYaxisBg;
    private GraphLineView vgraphChart;
    private GraphPathView dbgXaxisColorbar;
    private ViewGroup vgraphXaxis;
    private TextView vgraphXtitle;

    private Cfg cfg;
    private GraphData graphData;
    private int[] lineColors;

    // --- Values computed when normalizing x-axis
    private float xScale = 1f;
 //   private float xOffset = 0f;
    private float minX = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private int step = 10;
    private int numXlabels = 0;

    // ---------------------------------------------------------------------------------------------

    public void init(@NonNull ViewGroup root, @NonNull Cfg cfg, @NonNull GraphData graphData) {
        this.cfg = cfg;
        this.graphData = graphData;
        this.lineColors = graphData.colors;
        this.numXlabels = graphData.xLabels.length;
        assert (graphData.graphType != GraphType.None);

        // DEBUG
        this.dbgXaxisColorbar = root.findViewById(R.id.vgraph_xaxis_colorbar);
        Ui.setVisibleIf(COLOR_X_AXIS, dbgXaxisColorbar);

        /*
        xScale = 1f;
        xOffset = 0f;
        minX = graphData.xRange.minValue;
        maxX = graphData.xRange.maxValue;
        step = getStep(graphData.xRange.distance(), graphData.xLabels.length);
        */

        // --- Build background y-axis, row lines
        vgraphYaxisBg = root.findViewById(cfg.resYaxisBg);
        populateGridBackground();

        // --- Build x-axis
        vgraphXaxis = root.findViewById(cfg.resXaxis);
        // populateXaxis();

        // --- Setup line graph
        vgraphChart = root.findViewById(cfg.resChart);
        vgraphChart.linePaint.setColor(cfg.lineColor);
        vgraphChart.linePaint.setStrokeWidth(cfg.lineWidthPx);
        vgraphChart.markerRadius = cfg.markerRadiusPx;
        vgraphChart.markerPaint.setColor(cfg.markerColor);
        setLineColors(lineColors);

        vgraphXtitle = root.findViewById(cfg.resXtitle);
        refresh(graphData); // process data, build x-axis

        // --- Snap graph to background of graph.
        root.post(() -> {
            if (vgraphChart.getWidth() == 0) {
                ALog.e.tagMsg(this, "GraphChart is not visible, width is zero");
                return;
            }

            ViewGroup.MarginLayoutParams glp;

            View parent = findParent(vgraphYaxisBg, ScrollView.class.getName());
            int graphBgHeight = vgraphYaxisBg.getHeight();
            if (parent != null && graphBgHeight < parent.getHeight() - 20) {
                glp = (ViewGroup.MarginLayoutParams) vgraphYaxisBg.getLayoutParams();
                graphBgHeight = glp.height = parent.getHeight();
                vgraphYaxisBg.setLayoutParams(glp);
                setLineColors(lineColors);
            }

            // --- Snap graph to inside of background (avoid row labels)
            glp = (ViewGroup.MarginLayoutParams) vgraphChart.getLayoutParams();
            int halfRowHeight = graphBgHeight / cfg.numRows / 2;
            glp.bottomMargin = halfRowHeight;
            glp.topMargin = halfRowHeight;
            vgraphChart.setLayoutParams(glp);

            updateGraphData();

            // --- Snap xAxis to width of graph
            glp = (ViewGroup.MarginLayoutParams) vgraphXaxis.getLayoutParams();
            int[] chartLocation = new int[2];
            vgraphChart.getLocationOnScreen(chartLocation);
            int[] xAxisLocation = new int[2];
            vgraphXaxis.getLocationOnScreen(xAxisLocation);

            int leftDelta = chartLocation[0] - xAxisLocation[0];
            int widthDelta = vgraphXaxis.getWidth() -  vgraphChart.getWidth();
            int columnWidth = vgraphChart.getWidth() / cfg.numCols;

            glp.leftMargin += leftDelta - columnWidth/2;
            glp.rightMargin += widthDelta - columnWidth/2 - leftDelta ;
            vgraphXaxis.setLayoutParams(glp);

            int graphLeftMargin = glp.leftMargin;
            int graphRightMargin = glp.rightMargin;
            glp = (ViewGroup.MarginLayoutParams) dbgXaxisColorbar.getLayoutParams();
            glp.leftMargin = graphLeftMargin + columnWidth/2;
            glp.rightMargin = graphRightMargin + columnWidth/2;
            dbgXaxisColorbar.setLayoutParams(glp);
            setLineColors(lineColors);
            dbgXaxisColorbar.invalidate();
        });
    }

    public void setLineColors(int[] colors) {
        this.lineColors = colors;
        vgraphChart.setLineColors(lineColors);
        dbgXaxisColorbar.setColors(lineColors);
    }

    public void refresh(@NonNull GraphData graphData) {
        this.graphData = graphData;
        ALog.d.tagMsg(this, "graphType=", graphData.graphType.name());

        xScale = 1f;
    //    xOffset = 0f;
        minX = graphData.xRange.minValue;
        maxX = graphData.xRange.maxValue;
        getStep(graphData.xRange.distance());

        setLineColors(graphData.colors);
        populateXaxis();
        updateGraphData();
    }

    public void disposeImpl() {
         vgraphYaxisBg = null;
         vgraphXaxis = null;
         vgraphChart = null;
    }

    private void populateXaxis() {
        Context context = vgraphXaxis.getContext();
        vgraphXaxis.removeAllViews();

    ALog.d.tagFmt(this, "xLabel minX=%.2f, step=%d", minX, step);
        for (int xIdx = 0; xIdx < numXlabels; xIdx++) {
            TextView tv = new TextView(new ContextThemeWrapper(context, cfg.resXaxisStyle));
            tv.setGravity(Gravity.CENTER);
            float value = minX + xIdx * step;
            String xLbl = graphData.getXLabels(xIdx, value, step); // getXLabels(xIdx);
            tv.setText(xLbl);
    ALog.d.tagFmt(this, "xLabel[%d] minX=%.2f, value=%.2f lbl=%s", xIdx, minX, value, xLbl);
            // if (((xValue) & 1) == 0)  tv.setBackgroundColor(0x80800000);

            tv.setIncludeFontPadding(false);
            tv.setFirstBaselineToTopHeight(0);
            tv.setLineSpacing(0, 0);

            LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutFromStyle(context, cfg.resXaxisStyle, glp);
            vgraphXaxis.addView(tv, glp);
        }
    }

    private void populateGridBackground() {
        Context context = vgraphYaxisBg.getContext();
        vgraphYaxisBg.setUseDefaultMargins(false);

        vgraphYaxisBg.removeAllViews();

        int numLbls = graphData.yLabels.length;
        for (int yIdx = 0; yIdx < numLbls; yIdx++) {
            TextView tv = new TextView(new ContextThemeWrapper(context, cfg.resYaxisStyle));
            tv.setGravity(Gravity.CENTER_VERTICAL + Gravity.END);
            tv.setTextColor(0xffc0c0c0);
            tv.setTypeface(Typeface.create(Typeface.SANS_SERIF,500,false)); // TODO use Robo font
            tv.setText(graphData.yLabels[yIdx]);
            tv.setPadding(0, 0, cfg.lblPadRightPx, 0);
            tv.setIncludeFontPadding(false);
            tv.setFirstBaselineToTopHeight(0);
            tv.setLineSpacing(0, 0);
            tv.setMinHeight(cfg.rowHeightPx);

            // MUST create new GridLayout.LayoutParams for each item because its column and row get set
            // and thus cannot share across multiple cells.
            GridLayout.LayoutParams txLp = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(cfg.lblWidthPx, cfg.rowHeightPx));
            txLp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);       // x1
            // txLp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, GridLayout.FILL, 1f);       // x1
            vgraphYaxisBg.addView(tv, txLp);

            ImageView iv = new ImageView(context);
            iv.setImageResource(cfg.resBgRowDrawable);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setColorFilter(Color.argb(255, 128, 128, 128));
       //     iv.setBackgroundColor((level & 1) == 0 ? 0xff404060 : 0xff406040);

            // MUST create new GridLayout.LayoutParams for each item because its column and row get set
            // and thus cannot share across multiple cells.
            GridLayout.LayoutParams ivlp = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, cfg.rowHeightPx));
            ivlp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            ivlp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            ivlp.setMarginStart(cfg.yAxisBgStartMarginPx);
            vgraphYaxisBg.addView(iv, ivlp);
        }
    }

    public ArrayList<PointF> testData;
    private void updateGraphData() {
        ArrayList<PointF> points = testData;
   //     xOffset = 0f;
        xScale = 1f;
        getStep(graphData.xRange.distance());
        if (NORMALIZE_X_AXIS) {
            normalizeX(points);
            vgraphChart.setLineColors(lineColors);
            dbgXaxisColorbar.setColors(lineColors);
            populateXaxis();
        }
        Ui.setVisibleIf(COLOR_X_AXIS, dbgXaxisColorbar);
        updateGraphData(points);
    }

    /** xx
    private void updateGraphData() {
        vgraphChart.clear();
        AlphaAnimation anim = new AlphaAnimation(0.0f, 0.5f);
        anim.setDuration(1000);
        anim.setRepeatCount(0);
        vgraphChart.startAnimation(anim);

        LiveQueue<ElevData> queue = graphData.getDataAsync(graphData.dataMilli).queue();
        queue.observeForever( (elevData) -> {
            ALog.d.tagMsg(this, "got elev data numValues=", elevData.numValues(), " queueSize=", queue.size());
            if (queue.size() == 0) {
                if (elevData.numValues() > vgraphChart.getLinePointCnt()) {
                    ArrayList<PointF> points = elevData.getPoints(graphData.dataUnit.outMetric);
                    xOffset = 0f;
                    xScale = 1f;
                    step = getStep(graphData.xRange.distance(), graphData.xLabels.length);
                    if (NORMALIZE_X_AXIS) {
                        normalizeX(points);
                        vgraphChart.setLineColors(lineColors);
                        dbgXaxisColorbar.setColors(lineColors);
                        populateXaxis();
                    }
                    Ui.setVisibleIf(COLOR_X_AXIS, dbgXaxisColorbar);
                    updateGraphData(points);
                }
            }
            queue.next();
        });
    }
     */

    private void getStep(float distance) {
        numXlabels = graphData.xLabels.length;
        step = (int)(distance / (numXlabels - 1));
        if (step > 20)
            step = 20;
        else if (step >= 10)
            step = 10;
        else if (step >= 5)
            step = 5;
        else if (step >= 2)
            step = 2;
        else
            step = 1;
        numXlabels = (int)Math.ceil(distance / step + 1);
    }

    private void normalizeX(ArrayList<PointF> points) {
        int numSamples = points.size();
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        for (int idx = 0; idx < numSamples; idx++) {
            PointF pt = points.get(idx);
            minX = Math.min(minX, pt.x);
            maxX = Math.max(maxX, pt.x);
        }

        float dataDistance = maxX - minX;
        minX = (float)Math.floor(minX / step) * step;
        maxX = (float)Math.ceil(maxX / step) * step;
        dataDistance = maxX - minX;
        getStep(dataDistance);

        boolean isInside = minX >= 0 && maxX <= 100;
        final float MIN_X_PERCENT = 75;
        final float MAX_X_SCALE = 5;
        final float X_START = 0f;
        final float X_END = 100f;
        final float X_DISTANCE = X_END - X_START;

        xScale = 1;
        if (dataDistance < MIN_X_PERCENT || !isInside) {
            if (dataDistance < MIN_X_PERCENT || dataDistance > X_DISTANCE) {
                xScale = Math.min(MAX_X_SCALE, X_DISTANCE / dataDistance);
            }

            ALog.d.tagMsg(this, graphData.graphType.name()
                    , " xPercent=", dataDistance, " isInside=", isInside
                    , " minX=", minX, " maxX=", maxX
                    , ", xScale=", xScale);

            // Rebuild color array
            //   colors[xRange.min] ... colors[xRange.max]
            lineColors = new int[numXlabels];
            int defStep = (int)(graphData.xRange.distance() / (graphData.colors.length-1));
            for (int outIdx = 0; outIdx < numXlabels; outIdx++) {
                float outValue = minX + outIdx * step;
                int inIdx = Math.max(0,
                        Math.min(graphData.colors.length-1,
                                Math.round((outValue - graphData.xRange.minValue) / defStep)));
                lineColors[outIdx] = graphData.colors[inIdx];
            }
        } else {
            lineColors = graphData.colors;
            minX = graphData.xRange.minValue;
            maxX = graphData.xRange.maxValue;
            getStep(graphData.xRange.distance());
        }
    }

    // Incoming point data is scaled based on its default range to
    // fit in x domain  0..100
    // Ex:
    //      Ice probability domain is 0..100
    //      Rel humidity    domain is 0..100
    //      Temperature F   domain is -30..70
    //      Wind speed mph  domain is 0..70
    private void updateGraphData(final ArrayList<PointF> points) {
        vgraphChart.clear();

        int numSamples = points.size();
        boolean first = true;

        for (int idx = 0; idx < numSamples; idx++) {
            PointF pt = points.get(idx);
            float x = (pt.x - minX) * xScale;
            ALog.i.tagFmt(this, "%5.0f #%-2d x=%6.2f adj=%6.2f", pt.y, idx, pt.x, x);
            if (first)
                vgraphChart.setStart(x, pt.y);
            else
                vgraphChart.addPoint(x, pt.y);
            vgraphChart.addMarker(x, pt.y);
            first = false;
        }
        vgraphChart.setEnd();

        vgraphChart.setAlpha(0f);
        vgraphChart.removeCallbacks(this::fadeIn);
        vgraphChart.postDelayed(this::fadeIn, 1000);

        vgraphXtitle.setText(String.format(Locale.US, "min=%.0f  max=%.0f xScale=%.2f", minX, maxX, xScale));
        // ALog.i.tagMsg(this, "draw elev size", numSamples);
    }

    private void fadeIn() {
        vgraphChart.setAlpha(1f);
    }
}
