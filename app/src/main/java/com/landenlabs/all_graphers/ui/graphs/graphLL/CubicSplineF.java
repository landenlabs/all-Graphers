/*
 *
 * Copyright © 2024 The Weather Company. All rights reserved.
 *
 */

package com.landenlabs.all_graphers.ui.graphs.graphLL;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Spline array of points (smooth data), used by line graph.
 */
public class CubicSplineF {

    // Number of intermediate additional points added to smooth curve.
    public static final int CNT_MULTIPLIER = 5;
    static final float RESOLUTION = 1f/CNT_MULTIPLIER;

    public static void splinePointFs(@NonNull ArrayList<PointF> inPts, @NonNull ArrayList<PointF> outPoints) {
        ArrayList<Float> slopes = getSlopes(inPts);
        ArrayList<PointF> wp = new ArrayList<>(inPts.size() * CNT_MULTIPLIER);

        PointF prev = new PointF(Float.MAX_VALUE, Float.MAX_VALUE);

        for (int i = 0; i < inPts.size() - 1; i++) {
            PointF p1, p2;
            p1 = inPts.get(i);
            p2 = inPts.get(i + 1);

            if (p1.x == p2.x) {
                float inc = (p2.y - p1.y) * RESOLUTION;
                if (p1.y <= p2.y) {
                    for (float j = p1.y; j <= p2.y; j += inc) {
                        wp.add(new PointF(p1.x, j));
                    }
                } else {
                    for (float j = p1.y; j >= p2.y; j += inc) {
                        wp.add(new PointF(p1.x, j));
                    }
                }
            } else {
                float slope1, slope2, x1, x2;
                if (prev.x == Float.MAX_VALUE) {
                    slope1 = slopes.get(i);
                } else {
                    slope1 = (inPts.get(i).y - prev.y) / (prev.x - inPts.get(i).x);
                }

                slope2 = slopes.get(i + 1);
                if (i + 2 < inPts.size()) {
                    boolean pos1 = (inPts.get(i + 1).x - inPts.get(i).x) >= 0;
                    boolean pos2 = (inPts.get(i + 2).x - inPts.get(i + 1).x) >= 0;

                    if (pos2 != pos1) {
                        slope2 = -slopes.get(i + 1);
                    }
                }

                if (prev.x == Float.MAX_VALUE) {
                    x1 = ((p2.x - p1.x) / 4) + p1.x;
                } else {
                    float mult;
                    if (p1.x - prev.x > 0) {
                        mult = 1;
                    } else {
                        mult = -1;
                    }
                    x1 = (float)(p1.x + ((Math.abs(p2.x - p1.x) / 4) * mult));
                }

                x2 = (3 * ((p2.x - p1.x) / 4)) + p1.x;
                PointF c0, c1, c2, c3;
                c0 = new PointF(p1.x, p1.y);
                c1 = new PointF(x1, (slope1 * (p1.x - x1) + p1.y));
                c2 = new PointF(x2, (slope2 * (p2.x - x2) + p2.y));
                c3 = new PointF(p2.x, p2.y);
                prev = c2;

                for (float t = 0; t <= 1; t += RESOLUTION) {
                    wp.add(new PointF((1 - t) * ((1 - t) * ((1 - t) * c0.x + t * c1.x) + t * ((1 - t) * c1.x + t * c2.x))
                            + t * ((1 - t) * ((1 - t) * c1.x + t * c2.x) + t * ((1 - t) * c2.x + t * c3.x)),
                            ((1 - t) * ((1 - t) * ((1 - t) * c0.y + t * c1.y) + t * ((1 - t) * c1.y + t * c2.y))
                            + t * ((1 - t) * ((1 - t) * c1.y + t * c2.y) + t * ((1 - t) * c2.y + t * c3.y)))));
                }
            }
        }

        outPoints.clear();
        outPoints.addAll(wp);
    }

    @NonNull
    private static ArrayList<Float> getSlopes(@NonNull ArrayList<PointF> inPts) {
        ArrayList<Float> slopes = new ArrayList<>(inPts.size()+2);

        for (int i = 0; i < inPts.size() - 1; i++) {
            if (inPts.get(i + 1).x == inPts.get(i).x) {
                slopes.add(-1.0f);
            } else {
                slopes.add(((inPts.get(i + 1).y - inPts.get(i).y) / (inPts.get(i).x - inPts.get(i + 1).x)));
            }
        }

        if (inPts.get(inPts.size() - 1).x == inPts.get(inPts.size() - 2).x) {
            slopes.add(-1.0f);
        } else {
            slopes.add((inPts.get(inPts.size() - 1).y - inPts.get(inPts.size() - 2).y) / (inPts.get(inPts.size() - 2).x - inPts.get(inPts.size() - 1).x));
        }
        return slopes;
    }

}
