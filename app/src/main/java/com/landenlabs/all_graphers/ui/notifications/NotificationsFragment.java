package com.landenlabs.all_graphers.ui.notifications;

import static com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.landenlabs.all_graphers.databinding.FragmentNotificationsBinding;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    // Begin Graph
    private LineChart mpPlot;
    protected DateTime startTime = DateTime.now();
    protected final int lineOnlyWidth = 3;    // TODO use DP
    protected final int lineFillWidth = 3;    // TODO use DP
    protected final Map<Integer, String> legendName = new HashMap<>();
    protected int mult = 1;   // 1= min & max together, 2=separate
    // End Graph

    //region [createView] --------------------------------------------------------------------------
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        mpPlot = binding.mpchart;
        setupChart();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    //endregion

    //region [Graph] -------------------------------------------------------------------------------
    protected final DateTimeFormatter yAxisHourFmt = DateTimeFormat.forPattern("ha");
    protected final DateTimeFormatter yAxisDayFmt1 = DateTimeFormat.forPattern("E d");
    protected final DateTimeFormatter yAxisDayFmt2 = DateTimeFormat.forPattern("MMM d");
    protected DateTimeFormatter yAxisDayFmt = yAxisDayFmt1;
    protected final DateTimeFormatter dayFmt = DateTimeFormat.forPattern("d E");

    protected static final float LABEL_TEXT_SIZE = 14f;
    protected static final float AXIS_TEXT_SIZE = 20f;    // ####
    protected static final float LEGEND_TEXT_SIZE = 15f;
    protected static final float TITLE_TEXT_SIZE = 20f;
    protected static final int LEGEND_TEXT_COLOR = Color.WHITE;

    private void setupChart() {
        mpPlot.setTouchEnabled(true);
        mpPlot.setPinchZoom(false);
        mpPlot.setDragEnabled(true);
        mpPlot.setScaleXEnabled(true);
        // mpPlot.animateY(5000);

        // mpPlot.setTitle("Home Sensor");
        mpPlot.getDescription().setText("");
        mpPlot.getDescription().setTextColor(Color.WHITE);
        mpPlot.getDescription().setTextSize(TITLE_TEXT_SIZE);

        RectF rectF = mpPlot.getContentRect();
        float mpPlotWidth = (rectF != null) ? rectF.right : 250;
        // float mpPlotHeight = (rectF != null) ? rectF.height() : 250;

        // --- Does not work
        Shader colorShader = new LinearGradient(0, 0, mpPlotWidth, 0,
                Color.BLACK, Color.rgb(0, 64, 0), Shader.TileMode.MIRROR);
        // graph.getGridBackgroundPaint().setShader(colorShader);
        mpPlot.getPaint(Chart.PAINT_GRID_BACKGROUND).setShader(colorShader);
        // -----

        // DateTimeZone.forID("America/New_York")
        // DateTimeFormatter yAxisFmtTz = yAxisFmt.withZone(tz);
        mpPlot.setExtraTopOffset(10);   // Top xAxis
        mpPlot.getXAxis().setTextColor(Color.WHITE);
        mpPlot.getXAxis().setTextSize(AXIS_TEXT_SIZE);
        mpPlot.getXAxis().setLabelRotationAngle(90);

        mpPlot.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (sampleBy == Units.SampleBy.Hours) {
                    DateTime dt1 = startTime.plusHours((int) value);
                    return yAxisHourFmt.print(dt1);
                } else {
                    DateTime dt1 = startTime.plusDays((int) value);
                    return yAxisDayFmt.print(dt1);
                }
            }
        });


        CustomMarkerView mv = new CustomMarkerView(
                getContext(), R.layout.chart_live_label,
                mpPlot.getXAxis().getValueFormatter(), mpPlot.getXAxis());
        mpPlot.setMarker(mv);
    }

    protected static Entry DayEntry(
            int dayIdx, DateTime startTime, DeviceGoveeHelper.D2 rec, Function<DeviceGoveeHelper.D2, Float> getVal) {
        DateTime dt = new DateTime(rec.milli);
        int seconds = Seconds.secondsBetween(startTime, dt).getSeconds();
        double days = seconds / (double) TimeUnit.DAYS.toSeconds(1);
        return new Entry((float) days, getVal.apply(rec));
    }

    protected static void normalizeAxis(
            @NonNull AxisBase axis, final int numLabels, final int STEP, @NonNull LineDataSet... sets) {
        float yMax = Float.MIN_VALUE;
        float yMin = Float.MAX_VALUE;
        for (LineDataSet set : sets) {
            set.calcMinMax();
            yMax = Math.max(yMax, (float) Math.ceil(set.getYMax() / STEP) * STEP);
            yMin = Math.min(yMin, (float) Math.floor(set.getYMin() / STEP) * STEP);
        }
        float yRange0 = yMax - yMin;
        int yStep0 = (int) Math.ceil(yRange0 / numLabels);
        yStep0 = (int) Math.ceil(yStep0 / (float) STEP) * STEP;
        float extra = Math.max(STEP, yRange0 - ((numLabels - 1) * yStep0));
        float extraMin = (float) Math.floor(extra / 2 / STEP) * STEP;
        yMin -= extraMin;
        yMax += (extra - extraMin);
        axis.setAxisMinimum(yMin);
        axis.setAxisMaximum(yMax);
        axis.setLabelCount(numLabels, /*force: */true);
    }

    protected void addDayLine(@NonNull XAxis xAxis, int xPos, @NonNull String label, boolean bottom) {
        final int ORANGE = 0xffFFA500;
        @ColorInt int color = bottom ? Color.RED : ORANGE;
        LimitLine limitLine = new LimitLine(xPos, label);
        limitLine.setLineColor(color);
        limitLine.setLineWidth(lineOnlyWidth / 2f);
        //    ll1.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(bottom
                ? LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                : LimitLine.LimitLabelPosition.RIGHT_TOP
        );
        // limitLine.setLabelRotationAngle(-45);
        limitLine.setTextSize(LABEL_TEXT_SIZE);
        limitLine.setTextColor(color);
        xAxis.addLimitLine(limitLine);
    }

    public void enableSeries(@NonNull Units.Sensors sensor, boolean show) {
        if (mpPlot != null && mpPlot.getData() != null) {
            if (mult == 1) {
                if (enableSeries(sensor.ordinal(), show)) {
                    mpPlot.invalidate();
                }
            } else {
                boolean refresh;
                refresh = enableSeries(sensor.ordinal() * 2, show);
                refresh |= enableSeries(sensor.ordinal() * 2 + 1, show);
                if (refresh)
                    mpPlot.invalidate();
            }
        }
    }

    protected boolean enableSeries(int idx, boolean show) {
        ILineDataSet set = mpPlot.getData().getDataSetByIndex(idx);
        if (set != null && set.isVisible() != show) {
            set.setVisible(show);
            if (set.getAxisDependency() == LEFT) {
                mpPlot.getAxisLeft().setEnabled(show);
            } else {
                mpPlot.getAxisRight().setEnabled(show);
            }
            if (legendName.containsKey(idx) && idx < mpPlot.getLegend().getEntries().length) {
                mpPlot.getLegend().getEntries()[idx].label = show ? legendName.get(idx) : "";
            }
            return true;
        }
        return false;
    }

    protected void addSet(
            @NonNull String name,
            @NonNull ArrayList<Entry> series,
            int lineColor,
            Drawable fillDrawable,
            YAxis.AxisDependency yAxis,     // Left or Right
            int idx) {
        LineDataSet set;   // Temperature
        legendName.put(idx, name);
        if (mpPlot.getData() != null && idx < mpPlot.getData().getDataSetCount()) {
            set = (LineDataSet) mpPlot.getData().getDataSetByIndex(idx);
            set.setValues(series);
        } else {
            set = new LineDataSet(series, name);

            // set.enableDashedLine(10f, 0f, 0f);
            // set.enableDashedHighlightLine(10f, 0f, 0f);
            set.setColor(lineColor);
            // set.setColors(lineColor);
            // set.setFillColor(lineColor);
            set.setFillDrawable(fillDrawable);
            set.setValueTextColor(lineColor);
            set.setValueTextSize(AXIS_TEXT_SIZE);

            set.setHighLightColor(Color.YELLOW);
            // set.setCircleColor(getResources().getColor(R.color.toolBarColor));
            set.setLineWidth(lineOnlyWidth);
            // set.getForm().getEntries(); // shapes

            set.setDrawCircles(false);
            set.setDrawFilled(fillDrawable != null);
            set.setDrawValues(false);

            set.setAxisDependency(yAxis);

            // set.setFormSize(axisTextSize);

            /*
            set.setDrawCircles(true);
            set.setCircleRadius(5f);
            set.setDrawCircleHole(true);
             */

            /*
            set.setValueTextSize(10f);
            set.setDrawFilled(true);
            set.setFormLineWidth(5f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(5.f);

            if (Utils.getSDKInt() >= 18) {
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.blue_bg);
//                set1.setFillDrawable(drawable);
                set1.setFillColor(Color.WHITE);
            } else {
                set1.setFillColor(Color.WHITE);
            }
            set1.setDrawValues(true);
            */
            List<ILineDataSet> dataSets;
            if (mpPlot.getData() != null && mpPlot.getData().getDataSetCount() != 0) {
                dataSets = mpPlot.getData().getDataSets();
            } else {
                dataSets = new ArrayList<>();
            }
            dataSets.add(set);
            LineData data = new LineData(dataSets);
            mpPlot.setData(data);

            int legendLen = mpPlot.getLegend().getEntries().length;
            if (legendLen > 0) {
                mpPlot.getLegend().setTextColor(LEGEND_TEXT_COLOR);
                mpPlot.getLegend().setTextSize(LEGEND_TEXT_SIZE);
            }
        }
    }


    // =============================================================================================
    static class CustomMarkerView extends MarkerView {

        protected final TextView tvContent;
        protected final MPPointF offset;
        protected final ValueFormatter xFormatter;
        protected final AxisBase xAxis;

        public CustomMarkerView(
                @NonNull Context context, int layoutResource,
                @NonNull ValueFormatter xFormatter,
                @NonNull AxisBase xAxis) {
            super(context, layoutResource);
            tvContent = findViewById(R.id.chart_live_label_tv);
            offset = new MPPointF(-getWidth() / 2f, -getHeight() / 2f);
            this.xFormatter = xFormatter;
            this.xAxis = xAxis;
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(@NonNull Entry e, @NonNull Highlight highlight) {
            // DateTime dt = getDateTimeForXAxis(e.getX(), interval, sampleBy);
            String xStr = xFormatter.getAxisLabel(e.getX(), xAxis);
            tvContent.setText(String.format("%.1f\nat %s", e.getY(),
                    xStr)); // set the entry-value as the display text
            super.refreshContent(e, highlight);     // Perform necessary layouting
        }

        @Override
        public MPPointF getOffset() {
            //return super.getOffset();
            return offset;
        }
    }
    //endregion
}