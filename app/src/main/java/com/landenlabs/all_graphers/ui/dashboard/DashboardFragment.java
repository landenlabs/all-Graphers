package com.landenlabs.all_graphers.ui.dashboard;

import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.landenlabs.all_graphers.R;
import com.landenlabs.all_graphers.databinding.FragmentDashboardBinding;
import com.landenlabs.all_graphers.ui.data.WxDataHolder;
import com.landenlabs.all_graphers.ui.graphs.GraphData;
import com.landenlabs.all_graphers.ui.graphs.GraphType;
import com.landenlabs.all_graphers.ui.graphs.graphLL.GraphLineMgr;
import com.landenlabs.all_graphers.ui.logger.ALog;

import java.util.ArrayList;

public class DashboardFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private FragmentDashboardBinding binding;
    private GraphLineMgr graphMgr;
    private GraphLineMgr.Cfg cfg;
    private GraphData graphData;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        binding.graphNormCb.setOnClickListener(this);
        binding.graphData.setOnCheckedChangeListener(this);

        ALog.init(getContext(), "Dashboard");
        graphData = new GraphData(getContext(), new WxDataHolder());
        graphData.setType(GraphType.Temperature);

        makeChartCfg();
        graphMgr = new GraphLineMgr();
        graphMgr.testData = TEST_DATA1;
        graphMgr.init(binding.getRoot(), cfg, graphData);
        return root;
    }

    private void makeChartCfg() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        dpToPx = metrics.density;

        cfg = new GraphLineMgr.Cfg();
        cfg.numRows = graphData.yLabels.length;     // 11
        cfg.numCols = graphData.xLabels.length;     // ~ 11  (0, 10, 20... 100)

        cfg.resYaxisBg = R.id.vgraph_yaxis_bg;
        cfg.resChart = R.id.vgraph_chart;
        cfg.resXaxis = R.id.vgraph_xaxis;
        cfg.resXtitle = R.id.vgraph_bottom_title;

        cfg.resYaxisStyle = R.style.text14;
        cfg.resXaxisStyle = R.style.vgraphXaxis;
        cfg.resBgRowDrawable = R.drawable.vdashline;

        cfg.rowHeightPx  = toPx(30);
        cfg.lblPadRightPx  = toPx(10);
        cfg.lblWidthPx  = getResources().getDimensionPixelSize(R.dimen.yLabelWidth) - cfg.lblPadRightPx;
        cfg.yAxisBgStartMarginPx = toPx(5);

        cfg.lineWidthPx = 10;
        cfg.lineColor = 0xc0ffffff;

        cfg.markerRadiusPx = 15;
        cfg.markerColor = 0xffd0d0ff;
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        boolean checked = isChecked(view);
        if (id == R.id.graph_norm_cb) {
            GraphData.NORMALIZE_X_AXIS = checked;
            graphMgr.refresh(graphData);
        }
    }

    public static boolean isChecked(View view) {
        if (view instanceof Checkable) {
            return ((Checkable)view).isChecked();
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int idx) {
        if (idx == R.id.graph_data_1) {
            graphMgr.testData = TEST_DATA1;
        } else if (idx == R.id.graph_data_2) {
            graphMgr.testData = TEST_DATA2;
        } else if (idx == R.id.graph_data_3) {
            graphMgr.testData = TEST_DATA3;
        } else if (idx == R.id.graph_data_4) {
            graphMgr.testData = TEST_DATA4;
        }
        graphMgr.refresh(graphData);
    }


    private static final ArrayList<PointF> TEST_DATA1 = new ArrayList<>();
    static {
        TEST_DATA1.add(new PointF(-20, 1000));
        TEST_DATA1.add(new PointF(-10, 2000));
        TEST_DATA1.add(new PointF(0, 3000));
        TEST_DATA1.add(new PointF(10, 4000));
        TEST_DATA1.add(new PointF(20, 5000));
        TEST_DATA1.add(new PointF(30, 6000));
        TEST_DATA1.add(new PointF(20, 7000));
        TEST_DATA1.add(new PointF(10, 8000));
        TEST_DATA1.add(new PointF(30, 9000));
        TEST_DATA1.add(new PointF(40, 10000));
    }

    private static final ArrayList<PointF> TEST_DATA2 = new ArrayList<>();
    static {
        TEST_DATA2.add(new PointF(-45, 1000));
        TEST_DATA2.add(new PointF(-10, 2000));
        TEST_DATA2.add(new PointF(0, 3000));
        TEST_DATA2.add(new PointF(30, 4000));
        TEST_DATA2.add(new PointF(10, 5000));
        TEST_DATA2.add(new PointF(30, 6000));
        TEST_DATA2.add(new PointF(10, 7000));
        TEST_DATA2.add(new PointF(40, 8000));
        TEST_DATA2.add(new PointF(60, 9000));
        TEST_DATA2.add(new PointF(75, 10000));
    }

    private static final ArrayList<PointF> TEST_DATA3 = new ArrayList<>();
    static {
        TEST_DATA3.add(new PointF(35, 1000));
        TEST_DATA3.add(new PointF(40, 2000));
        TEST_DATA3.add(new PointF(50, 3000));
        TEST_DATA3.add(new PointF(60, 4000));
        TEST_DATA3.add(new PointF(50, 5000));
        TEST_DATA3.add(new PointF(40, 6000));
        TEST_DATA3.add(new PointF(55, 7000));
        TEST_DATA3.add(new PointF(60, 8000));
        TEST_DATA3.add(new PointF(52, 9000));
        TEST_DATA3.add(new PointF(45, 10000));
    }

    private static final ArrayList<PointF> TEST_DATA4 = new ArrayList<>();
    static {
        TEST_DATA4.add(new PointF(55, 1000));
        TEST_DATA4.add(new PointF(60, 2000));
        TEST_DATA4.add(new PointF(70, 3000));
        TEST_DATA4.add(new PointF(80, 4000));
        TEST_DATA4.add(new PointF(90, 5000));
        TEST_DATA4.add(new PointF(100, 6000));
        TEST_DATA4.add(new PointF(120, 7000));
        TEST_DATA4.add(new PointF(100, 8000));
        TEST_DATA4.add(new PointF(92, 9000));
        TEST_DATA4.add(new PointF(85, 10000));
    }
}