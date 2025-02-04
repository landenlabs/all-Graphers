package com.landenlabs.all_graphers.ui.home;

import static android.graphics.PathDashPathEffect.Style.TRANSLATE;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.landenlabs.all_graphers.databinding.FragmentHomeBinding;
import com.landenlabs.all_graphers.ui.graph.GraphLineView;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final GraphLineView graphLine = binding.lineGraph;

        graphLine.linePaint.setColor(Color.RED);
        graphLine.linePaint.setStrokeWidth(5);
        graphLine.markRadius = 20;

        final int MAX_POINTS = 20;
        ArrayList<PointF> points = new ArrayList<>(MAX_POINTS);
        for (int x = 0; x < MAX_POINTS; x++) {
            float y = (float) Math.random() * 20.0f;
            graphLine.addPoint(x, y);
            // points.add(new PointF(x, y));
            // if ( (x % 2) == 0) {
                 graphLine.addMarker(x, y);
            // }
        }

        boolean dottedLine = false;
        if (dottedLine) {
            Path pathShape = new Path();
            pathShape.addCircle(0, 0, 10, Path.Direction.CCW);
            graphLine.linePaint.setPathEffect( new PathDashPathEffect(pathShape,  50,  20, TRANSLATE));
        }

        /*
        PointF p1 = points.get(0);
        for (int idx = 0; idx < MAX_POINTS; idx++) {
            PointF p2 = points.get(idx);
            graphLine.addPoint(p1.x, p1.y, p2.x, p2.y);
            p1 = p2;
        }
        graphLine.addPoint(p1.x, p1.y, p1.x+1, p1.y);
        */

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}