/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.Checkable;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;
import java.util.Map;


/**
 * UI object manipulation.
 * @noinspection ConstantValue, unused, JavadocLinkAsPlainText, JavadocLinkAsPlainText, resource, RedundantSuppression
 */
public class Ui {

    public static float dpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;
    }

    @SuppressWarnings("unchecked")
    public static <E extends View> E viewById(View rootView, int id) {
        return (E) rootView.findViewById(id);
    }

    public static  <E extends View> E findParent(View view, String className) {
        ViewParent parent;
        while ( (parent = view.getParent()) != null) {
            if (parent.getClass().getName().equals(className)) {
                return (E)parent;
            }
            view = (View)parent;
        }
        return null;
    }

    public static <E extends View> E needViewById(View rootView, int id) {
        E foundView = rootView.findViewById(id);
        if (foundView == null)
            throw new NullPointerException("layout resource missing");
        return foundView;
    }

    /**
     * Set text if not null.
     */
    public static void setTextIf(@Nullable TextView tv, @Nullable CharSequence cs) {
        if (tv != null) {
            tv.setText( (cs != null) ? cs : "");
        }
    }

    public static void setTextIf(@Nullable View tv, @StringRes int strRes) {
        if (tv instanceof TextView) {
            ((TextView)tv).setText(strRes);
        }
    }

    /**
     * Set visibility if view is not null.
     */
    public static void setVisibleIf(int vis, View ... views) {
        for (View view : views) {
            if (view != null) view.setVisibility(vis);
        }
    }
    public static void setVisibleIf(boolean vis, View ... views) {
        for (View view : views) {
            if (view != null) view.setVisibility(vis?VISIBLE:GONE);
        }
    }


    public static void setImageResourceIf(@DrawableRes int imageRes, View... imageViews) {
        for (View view : imageViews) {
            if (view instanceof ImageView) ((ImageView)view).setImageResource(imageRes);
        }
    }

    public static void setImageBgIf(@DrawableRes int imageRes, View... imageViews) {
        for (View view : imageViews) {
            if (view instanceof ImageView) ((ImageView)view).setBackgroundResource(imageRes);
        }
    }
    public static void setImageTintIf(@ColorInt int tintColor, View... imageViews) {
        for (View view : imageViews) {
            if (view instanceof ImageView) ((ImageView)view).setImageTintList(ColorStateList.valueOf(tintColor));
        }
    }

    public static <TT extends View> TT setClick(TT view, View.OnClickListener clickListener) {
        view.setOnClickListener(clickListener);
        return view;
    }

    private static float screenScale() {
        // return Math.min(1.0f, displayWidthPx / screenWidthPx);
        return 1.0f; // TODO - fix this
    }

    public static int screenScale(int iValue) {
        return (iValue <= 0) ? iValue : Math.round(iValue * screenScale());
    }

    public static ViewGroup.LayoutParams layoutFromAnyStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.LayoutParams lp) {
        if (lp instanceof LinearLayout.LayoutParams) {
            return layoutFromStyle(context, style, (LinearLayout.LayoutParams)lp);
        } else  if (lp instanceof ViewGroup.MarginLayoutParams) {
            return layoutFromStyle(context, style, (ViewGroup.MarginLayoutParams)lp);
        } else {
            return layoutFromStyle(context, style, lp);
        }
    }

    public static ViewGroup.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.LayoutParams lp) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_width,
                        android.R.attr.layout_height,
                },
                0, style);
        try {
            lp.width = screenScale(ta.getLayoutDimension(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            lp.height = screenScale(ta.getLayoutDimension(1, ViewGroup.LayoutParams.WRAP_CONTENT));
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static ViewGroup.MarginLayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, ViewGroup.MarginLayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.LayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_marginTop,
                        android.R.attr.layout_marginBottom,
                        android.R.attr.layout_marginStart,
                        android.R.attr.layout_marginEnd,
                },
                0, style);
        try {
            float scale = screenScale();
            lp.topMargin = screenScale(ta.getLayoutDimension(0, lp.topMargin));
            lp.bottomMargin = screenScale(ta.getLayoutDimension(1, lp.bottomMargin));
            lp.leftMargin = screenScale(ta.getLayoutDimension(2, lp.leftMargin));
            lp.rightMargin = screenScale(ta.getLayoutDimension(3, lp.rightMargin));
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static LinearLayout.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, LinearLayout.LayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.MarginLayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_weight,
                },
                0, style);
        try {
            lp.weight = ta.getFloat(0, lp.weight);
        } finally {
            ta.recycle();
        }
        return lp;
    }

    public static GridLayout.LayoutParams layoutFromStyle(
            @NonNull Context context, @StyleRes int style, GridLayout.LayoutParams lp) {
        layoutFromStyle(context, style, (ViewGroup.LayoutParams) lp);
        TypedArray ta = context.getTheme().obtainStyledAttributes(null,
                new int[]{
                        android.R.attr.layout_row ,             // 0
                        android.R.attr.layout_rowSpan,          // 1
                        android.R.attr.layout_rowWeight ,       // 2
                        android.R.attr.layout_column ,          // 3
                        android.R.attr.layout_columnSpan,       // 4
                        android.R.attr.layout_columnWeight,     // 5
                        android.R.attr.layout_gravity,          // 6
                },
                0, style);

        try {
            // TODO - populate the other grid layoutParams listed above

            lp.rowSpec =  GridLayout.spec(GridLayout.UNDEFINED,   ta.getFloat(2, 0f));
            lp.columnSpec =  GridLayout.spec(GridLayout.UNDEFINED,   ta.getFloat(5, 0f));
        } finally {
            ta.recycle();
        }
        return lp;
    }


    /**
     * Helper to remove listener using appropriate API.
     *
     * @param view     object
     * @param forceRemove Set to true to force removal of listener.
     *                    <p>
     *                    There is a bug in the
     *                    globalLayout, it has a custom copy-on-write implementation which fails
     *                    to remove the listener until the list is used.
     *                    See http://landenlabs.com/android/info/leaks/android-memory-leaks.html#viewtree-listener
     *                    <p>
     *                    Set this to true to force removal.
     *                    <p>
     *                    Set to false if called from globalLayout listener because can't perform
     *                    cleanup while list is active.
     */
    public static void removeOnGlobalLayoutListener(View view,
            ViewTreeObserver.OnGlobalLayoutListener listener,
            boolean forceRemove) {
        if (null != view) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);

            if (forceRemove) {
                // Following call to dispatchOnGlobalLayout causes the internal
                // listener copyOnWriteArray to update freeing the old listener.

                // Need one or more items in list before calling dispatchOnGlobalLayout to
                // fore copy-on-write to execute.
                view.getViewTreeObserver().addOnGlobalLayoutListener(dummyListener);
                view.getViewTreeObserver().dispatchOnGlobalLayout();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(dummyListener);
            }
        }
    }

    private static final ViewTreeObserver.OnGlobalLayoutListener dummyListener = () -> {  };

    public static void setUniqueIds(View view, int level, Map<Integer, Integer> beforeAfter) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int idx = 0; idx < viewGroup.getChildCount(); idx++) {
                View child = viewGroup.getChildAt(idx);
                int beforeId = view.getId();
                int afterId = beforeAfter.containsKey(beforeId) ? beforeAfter.get(beforeId) : View.generateViewId();
                beforeAfter.put(beforeId, afterId);
                child.setId(afterId);
                setUniqueIds(child, level +1, beforeAfter);
            }
            /* TODO - reset constraint attachments
            if (view instanceof ConstraintLayout) {
                ConstraintLayout cl = (ConstraintLayout)view;
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(cl);
                int[] ids1 = constraintSet.getKnownIds();
                int[] ids2 = constraintSet.getReferencedIds(0);
            }
             */
        }
    }

    /**
     * Dump info about view and its children into StringBuffer returned.
     */
    public static StringBuilder dumpViews(View view, int level) {
        StringBuilder sb  = new StringBuilder();
        String prefix;
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        prefix = sb.toString();

        sb = new StringBuilder();
        if (false) {
            sb.append(String.format(Locale.US, "hasOnClick=%-5b focusable=%-5b inTouch=%-5b shown=%-5b ",
                    view.hasOnClickListeners(),
                    view.isFocusable(),
                    view.isFocusableInTouchMode(),
                    view.isShown()
            ));
        }
        if (true) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            sb.append(String.format(Locale.US, "%s[w=%4d h=%5d] ",
                    lp.getClass().getName().replaceAll("[$].*", "").replaceAll("[a-z.]+", ""),
                    lp.width,
                    lp.height  ));
            if (lp instanceof ConstraintLayout.LayoutParams)  {
                ConstraintLayout.LayoutParams cp = (ConstraintLayout.LayoutParams)lp;
                // int tt = cp.topToTop;
            }
        }
        if (true) {
            sb.append(String.format(Locale.US, "Sz[w=%4d h=%5d] ",
                    view.getWidth(),
                    view.getHeight()  ));
        }
        if (true) {
            sb.append(String.format(Locale.US, "Pad[%2d %2d %2d %2d] ",
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()  ));
        }
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
            sb.append(String.format(Locale.US, "Margin[%2d %2d %2d %2d] ",
                    mp.leftMargin,
                    mp.topMargin,
                    mp.rightMargin,
                    mp.bottomMargin));
        }

        sb.append(String.format(Locale.US, "Vis=%-5b ", view.getVisibility() == VISIBLE ));
        sb.append(String.format("%2d ", level));
        sb.append(prefix).append(view.getClass().getSimpleName());
        if ((int)view.getId() > 0) {
            sb.append(" ID=").append(view.getId());
            try {
                String resName = view.getResources().getResourceName(view.getId());
                sb.append("=").append(resName);
            } catch (Exception ex) {
                Log.e("TWC", "dumpViews ex=", ex);
            }
        }
        if (view.getTag() != null) {
            // sb.append(" tag=").append(ALogUtils.getString(null, view.getTag()));
            sb.append(" tag=").append(view.getTag());
        }
        if (!TextUtils.isEmpty(view.getContentDescription())) {
            sb.append(" desc=").append(view.getContentDescription());
        }
        if (view instanceof TextView) {
            sb.append(" text=").append(((TextView)view).getText());
        }

        sb.append("\n");

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            for (int idx = 0; idx < viewGroup.getChildCount(); idx++){
                View child = viewGroup.getChildAt(idx);
                sb.append(dumpViews(child, level+1));
            }

            if (view instanceof ListView) {
                ExpandableListView expView = (ExpandableListView) view;
                Adapter adapter = expView.getAdapter();
                int cnt = adapter.getCount();
                for (int idx = 0; idx < cnt; idx++) {
                    Object obj = adapter.getItem(idx);
                    if (obj instanceof View) {
                        sb.append(dumpViews((View) obj, level + 1));
                    }
                }
            }
        }

        return sb;
    }

    public static String getTag(@NonNull View view) {
        return (view.getTag() instanceof String) ? view.getTag().toString() : "";
    }

    public static String getTag(@NonNull View view, @IdRes int idRes, String defValue) {
        return (view.getTag(idRes) instanceof String) ? view.getTag(idRes).toString() : defValue;
    }

    public static boolean tagHas(@NonNull View view, @NonNull String want) {
        return getTag(view).contains(want);
    }

    @Nullable
    public static <TT> TT checkedChild(ViewGroup group) {
        for (int idx = 0; idx < group.getChildCount(); idx++) {
            View item =  group.getChildAt(idx);
            if (item instanceof Checkable && ((Checkable)item).isChecked())
                return (TT)item;
        }
        return null;
    }

    public static void setCheckedIf(boolean isChecked, Checkable ... checkables) {
        for (Checkable view : checkables) {
            if (view != null)
                view.setChecked(isChecked);
        }
    }


    /**
     * Execute ripple effect (v5.0, api 21)
     */
    public static void runRippleAnimation(View view) {
        Drawable background = view.getBackground();
        if (background instanceof RippleDrawable) {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;
            rippleDrawable.setState(
                    new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
            rippleDrawable.setState(new int[]{});
        }
    }
}
