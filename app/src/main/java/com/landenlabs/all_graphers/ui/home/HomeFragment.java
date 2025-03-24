package com.landenlabs.all_graphers.ui.home;

import static android.graphics.PathDashPathEffect.Style.TRANSLATE;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.databinding.FragmentHomeBinding;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphLineView;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private  GridLayout vgraphYaxisBg;
    private  GridLayout vgraphXaxis;
    private  GraphLineView vgraphChart;
    private static final int NUM_ROWS = 11; // 10000 9000 ... 10(0)
    private static final int NUM_COLS = 11; // 0 10 20 ... 100


    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ----- Build background y-axis, row lines
        vgraphYaxisBg = binding.vgraphYaxisBg;
        populateGridBackground();

        vgraphXaxis = binding.vgraphXaxis;

        // ----- Setup line graph
        vgraphChart = binding.vgraphChart;
        vgraphChart.linePaint.setColor(Color.WHITE);
        vgraphChart.linePaint.setStrokeWidth(10);
        vgraphChart.markerRadius = 15;

        boolean dottedLine = false;
        if (dottedLine) {
            Path pathShape = new Path();
            pathShape.addCircle(0, 0, 10, Path.Direction.CCW);
            vgraphChart.linePaint.setPathEffect( new PathDashPathEffect(pathShape,  50,  20, TRANSLATE));
        }

        // updateGraphData();

        // Snap graph to background of graph.
        binding.getRoot().post(() -> {
            ViewGroup.MarginLayoutParams glp = (ViewGroup.MarginLayoutParams) vgraphChart.getLayoutParams();
            int halfRowHeight = vgraphYaxisBg.getHeight() / NUM_ROWS / 2;
            glp.bottomMargin = halfRowHeight;
            glp.topMargin = halfRowHeight;
            vgraphChart.setLayoutParams(glp);
            updateGraphData();

            glp =  (ViewGroup.MarginLayoutParams) vgraphXaxis.getLayoutParams();
            int columnWidth = vgraphChart.getWidth() / NUM_COLS;
            glp.leftMargin = -columnWidth/2;
            glp.rightMargin = -columnWidth/2;
        });

        return root;
    }

    private void populateGridBackground() {
        Context context = getContext();
        vgraphYaxisBg.setUseDefaultMargins(false);

        vgraphYaxisBg.removeAllViews();
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        dpToPx = metrics.density;

        int rowHeightDp = getResources().getDimensionPixelSize(R.dimen.rowHeight);

        // int lblWidthPx = toPx(55);    // @dimen/yLabelWidth
        int lblPadRightPx = toPx(5);
        int lblWidthPx = getResources().getDimensionPixelSize(R.dimen.yLabelWidth) - lblPadRightPx;

        ViewGroup.LayoutParams txLp = new ViewGroup.LayoutParams(lblWidthPx, toPx(rowHeightDp));
        GridLayout.LayoutParams ivlp = new GridLayout.LayoutParams();
        ivlp.height = toPx(rowHeightDp);
        ivlp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        ivlp.setMarginStart(toPx(5));

        for (int level = 10; level >= 0; level--) {
            TextView tv = new TextView(new ContextThemeWrapper(context, R.style.text14));
            tv.setGravity(Gravity.CENTER_VERTICAL + Gravity.END);
            tv.setTextColor(0xffc0c0c0);
            tv.setTypeface(Typeface.create(Typeface.SANS_SERIF,500,false)); // TODO use Robo font
            tv.setText(level > 0 ? String.format("%d,000'", level) : "10'");
            tv.setPadding(0, 0, lblPadRightPx, 0);
            tv.setIncludeFontPadding(false);
            tv.setFirstBaselineToTopHeight(0);
            tv.setLineSpacing(0, 0);
            vgraphYaxisBg.addView(tv, txLp);

            ImageView iv = new ImageView(context);
            iv.setImageResource(R.drawable.vdashline);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setColorFilter(Color.argb(255, 128, 128, 128));
            iv.setBackgroundColor((level & 1) == 0 ? 0xff404060 : 0xff406040);
            vgraphYaxisBg.addView(iv,  new GridLayout.LayoutParams(ivlp));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    float dpToPx;
    private int toPx(int dp) {
        return Math.round(dp * dpToPx);
    }

}