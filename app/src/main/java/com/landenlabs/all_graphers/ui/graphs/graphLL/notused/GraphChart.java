package com.landenlabs.all_graphers.ui.graphs.graphLL.notused;

import static android.graphics.PathDashPathEffect.Style.TRANSLATE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.landenlabs.all_graphers.ui.data.UiUtils.layoutFromAnyStyle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphLineView;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphPathView;

import java.util.Locale;

public class GraphChart {

    private GridLayout vgraphYaxisBg;
    private ViewGroup vgraphXaxis;
    private GraphLineView vgraphChart;
    private GraphPathView dbgXcolorbar;

    private static final int NUM_ROWS = 11; // 10000 9000 ... 10(0)
    private static final int NUM_COLS = 11; // 0 10 20 ... 100

    public static class Cfg {
        public @IdRes int resYaxisBg;
        public @IdRes int resChart;
        public @IdRes int resXaxis;
        public int NUM_ROWS = 11; // 10000 9000 ... 10(0)
        public int NUM_COLS = 11; // 0 10 20 ... 100
        public @StyleRes int resYaxisStyle;    // R.style.text14
        public @StyleRes int resXaxisStyle;    // R.style.vgraphXaxis

        public @DrawableRes int resBgRowDrawable;    // R.drawable.vdashline

        public int rowHeightPx;            // = context.getResources().getDimensionPixelSize(R.dimen.rowHeight);
        public int lblPadRightPx;          // = toPx(5);
        public int lblWidthPx;             // = context.getResources().getDimensionPixelSize(R.dimen.yLabelWidth) - lblPadRightPx;
        public int yAxisBgStartMarginPx;   // = toPx(5)
        public int markerRadiusPx;         // 15;
        public int lineWidthPx;            // 10

        public @ColorInt int lineColor;    // Color.WHITE);
    }

    private Cfg cfg;

    public void init(ViewGroup root, Cfg cfg) {
        this.cfg = cfg;
        // ----- Build background y-axis, row lines
        vgraphYaxisBg = root.findViewById(cfg.resYaxisBg);
        populateGridBackground(root.getContext());

        // ----- Build x-axis
        vgraphXaxis = root.findViewById(cfg.resXaxis);
        populateXaxis(root.getContext());
        dbgXcolorbar = root.findViewById(R.id.vgraph_xaxis_colorbar);


        // ----- Setup line graph
        vgraphChart = root.findViewById(cfg.resChart);
        vgraphChart.linePaint.setColor(cfg.lineColor);
        vgraphChart.linePaint.setStrokeWidth(cfg.lineWidthPx);  // 10
        vgraphChart.markerRadius = cfg.markerRadiusPx;    // 15;

        boolean dottedLine = false;
        if (dottedLine) {
            Path pathShape = new Path();
            pathShape.addCircle(0, 0, 10, Path.Direction.CCW);
            vgraphChart.linePaint.setPathEffect( new PathDashPathEffect(pathShape,  50,  20, TRANSLATE));
        }

        // Snap graph to background of graph.

        root.post(() -> {
            if (updateGraphLayout()) {
                /*
                populateGridBackground(vgraphYaxisBg.getContext());  // rebuild to handle stretching of rows.
                vgraphChart.invalidate();   // redo its layout
                 */
                // root.post(this::updateGraphLayout);

            }
            int[] colors = new int[] { Color.RED, Color.GREEN, Color.BLUE };
            dbgXcolorbar.setColors(colors);
            dbgXcolorbar.invalidate();
        });

        // updateGraphData();
    }

    private boolean updateGraphLayout() {

        ViewGroup parent = (ViewGroup)vgraphYaxisBg.getParent();
        // ViewGroup.MarginLayoutParams plp;
        // plp = (ViewGroup.MarginLayoutParams) parent.getLayoutParams();

        int numRows = vgraphYaxisBg.getRowCount();

        ViewGroup.MarginLayoutParams  bglp;
        bglp = (ViewGroup.MarginLayoutParams) vgraphYaxisBg.getLayoutParams();
        int parentInnerHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
        int graphHeight = vgraphYaxisBg.getHeight();
        if (graphHeight < parentInnerHeight) {
            graphHeight = parentInnerHeight;
            bglp.height = graphHeight;
            vgraphYaxisBg.setLayoutParams(bglp);
        }

        ViewGroup.MarginLayoutParams chartlp;
        chartlp = (ViewGroup.MarginLayoutParams) vgraphChart.getLayoutParams();
        int halfRowHeight = graphHeight / numRows / 2;
        int heightDelta = 0; // graphHeight - vgraphYaxisBg.getHeight();
        chartlp.bottomMargin = bglp.bottomMargin + halfRowHeight ;
        chartlp.topMargin = bglp.topMargin + halfRowHeight;
     //   vgraphChart.setLayoutParams(chartlp);
     //    vgraphChart.invalidate();

        updateGraphData();

        ViewGroup.MarginLayoutParams xlp;
        xlp = (ViewGroup.MarginLayoutParams) vgraphXaxis.getLayoutParams();
        int[] chartLocation = new int[2];
        vgraphChart.getLocationOnScreen(chartLocation);
        int[] xAxisLocation = new int[2];
        vgraphXaxis.getLocationOnScreen(xAxisLocation);
        int leftDelta = chartLocation[0] - xAxisLocation[0];
        int widthDelta = vgraphXaxis.getWidth() -  vgraphChart.getWidth();

        int columnWidth = vgraphChart.getWidth() / NUM_COLS;
        // glp.width = vgraphChart.getWidth() + columnWidth;

        xlp.leftMargin = leftDelta - columnWidth/2;
        xlp.rightMargin = widthDelta - columnWidth/2 - leftDelta;
        vgraphXaxis.setLayoutParams(xlp);

        return  false;
    }

    private void populateXaxis(@NonNull Context context) {
        /*
        ViewGroup.LayoutParams glp1;
        if (vgraphXaxis instanceof GridLayout)
            glp1 = new GridLayout.LayoutParams();
        else
            glp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutFromAnyStyle(context, R.style.vgraphXaxis, glp1);
        */

        for (int xValue = 0; xValue <=10; xValue++) {
            TextView tv = new TextView(new ContextThemeWrapper(context, cfg.resXaxisStyle));
            tv.setGravity(Gravity.CENTER_VERTICAL + Gravity.END);
            tv.setText(String.format(Locale.US, "%d", xValue*10));
            tv.setGravity(Gravity.CENTER);

            if (((xValue) & 1) == 0)
                tv.setBackgroundColor(0x80800000);

            tv.setIncludeFontPadding(false);
            tv.setFirstBaselineToTopHeight(0);
            tv.setLineSpacing(0, 0);


            // GridLayout.LayoutParams glp = new GridLayout.LayoutParams(glp1);
            LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            layoutFromAnyStyle(context, R.style.vgraphXaxis, glp);
            vgraphXaxis.addView(tv, glp);
        }
    }

    private void populateGridBackground(@NonNull Context context) {
        // if (true) return;

        boolean isStretchVertical = true; // vgraphYaxisBg.getLayoutParams().height != WRAP_CONTENT;

        vgraphYaxisBg.removeAllViews();
    //    vgraphYaxisBg.setUseDefaultMargins(false);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        dpToPx = metrics.density;

        int[] yLabels = new int[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };
        for (int idx = 0; idx < yLabels.length; idx++) {
            TextView tv = new TextView(new ContextThemeWrapper(context, cfg.resYaxisStyle));
            tv.setGravity(Gravity.CENTER_VERTICAL + Gravity.END);
            tv.setTextColor(0xffc0c0c0);
            tv.setTypeface(Typeface.create(Typeface.SANS_SERIF,500,false)); // TODO use Robo font
            tv.setText(String.format(Locale.US, "%d,000'", yLabels[idx]));
            tv.setPadding(0, 0, cfg.lblPadRightPx, 0);
            tv.setIncludeFontPadding(false);
            tv.setFirstBaselineToTopHeight(0);
            tv.setLineSpacing(0, 0);
            tv.setMinHeight(cfg.rowHeightPx);
            tv.setBackgroundColor((idx & 1) == 0 ? 0xff80010 : 0xff406040);

            // MUST create new GridLayout.LayoutParams for each item because its column and row get set
            // and thus cannot share across multiple cells.
            // Must construct GridLayout.LayoutParams with View layout to set internal flags.
            GridLayout.LayoutParams txLp;
            if (isStretchVertical) {
                txLp = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(cfg.lblWidthPx, cfg.rowHeightPx));
                txLp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);       // x1
                // txLp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, GridLayout.FILL, 1f);       // x1
            } else {
                txLp = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(cfg.lblWidthPx, cfg.rowHeightPx));
            }
            vgraphYaxisBg.addView(tv, txLp);

            ImageView iv = new ImageView(context);
            iv.setImageResource(R.drawable.vdashline);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setColorFilter(Color.argb(255, 128, 128, 128));
            iv.setBackgroundColor((idx & 1) == 0 ? 0xff404060 : 0xff406040);

            // MUST create new GridLayout.LayoutParams for each item because its column and row get set
            // and thus cannot share across multiple cells.
            // Must construct GridLayout.LayoutParams with View layout to set internal flags.
            GridLayout.LayoutParams ivlp;
            ivlp = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(0, 0));
            if (isStretchVertical) {
                ivlp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            } else {
                ivlp.height = cfg.rowHeightPx;
            }

            // ivlp.height = cfg.rowHeightPx;
            ivlp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            ivlp.setMarginStart(cfg.yAxisBgStartMarginPx);
            vgraphYaxisBg.addView(iv, ivlp);
        }
    }

    private void updateGraphData() {
        int Y_MAX = 10; // 10,000 feet
        int Y_MIN = 0;  // 0 feet (y axis shows 10 feet)
        int X_MIN = 0;
        int X_MAX = 100;
        int numSamples = 11;
        // y-axis elev feet 1000's     10   9,  8,  7,  6,  5,  4,  3,  2,  1, 0
        int[] testValues = new int[] { 50, 40, 30, 20, 0, 20, 30, 45, 49, 52, 100 };

        if (!vgraphChart.hasPoints()) {
            vgraphChart.clear();
            for (int idx = 0; idx < numSamples; idx++) {
                int x = testValues[idx];
                int y = Y_MAX - idx;
                if (idx == 0)
                    vgraphChart.setStart(x, y);
                else
                    vgraphChart.addPoint(x, y);
                vgraphChart.addMarker(x, y);
            }
        }
        vgraphChart.setEnd();
    }

    float dpToPx;
    private int toPx(int dp) {
        return Math.round(dp * dpToPx);
    }

}
