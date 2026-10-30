package com.gathin.tvhome;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** Opt-in Neon Premium surfaces. No blur, timers, new windows or focus listeners. */
final class NeonStyle {
    static final int BLUE = 0xFF4385FF;
    static final int VIOLET = 0xFFA46BFF;
    static final int INK = 0xFF080D24;

    private NeonStyle() { }

    static int accent(int position) {
        return position % 2 == 0 ? BLUE : VIOLET;
    }

    /** Render the two soft lights once, at low resolution; never redraw gradients per frame. */
    static BitmapDrawable backdrop(Resources resources) {
        Bitmap texture = Bitmap.createBitmap(480, 270, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(texture);
        canvas.drawColor(INK);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(80, 70, 280,
                new int[]{0x804385FF, 0x004385FF}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 480, 270, paint);
        paint.setShader(new RadialGradient(420, 200, 300,
                new int[]{0x66A46BFF, 0x00A46BFF}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 480, 270, paint);
        BitmapDrawable drawable = new BitmapDrawable(resources, texture);
        drawable.setGravity(android.view.Gravity.FILL);
        return drawable;
    }

    static GradientDrawable surface(View view, boolean focused, float radius) {
        float density = view.getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                focused ? new int[]{0xFF244E9C, 0xFF56338B} : new int[]{0xF0182343, 0xF015142F});
        shape.setCornerRadius(radius * density);
        shape.setStroke(Math.max(1, Math.round(density)), focused ? 0xFF9CCBFF : 0x665D79BC);
        return shape;
    }

    /** Called after rows are populated; does not replace their actions or navigation. */
    static void controls(View view) {
        if (view.isFocusable()) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_focused}, surface(view, true, 16));
            states.addState(new int[]{android.R.attr.state_pressed}, surface(view, true, 16));
            states.addState(new int[]{android.R.attr.state_activated}, surface(view, true, 16));
            states.addState(new int[]{}, surface(view, false, 16));
            view.setBackground(states);
        }
        if (view instanceof TextView && (view.isFocusable() || view.isDuplicateParentStateEnabled())) {
            TextView text = (TextView) view;
            ColorStateList white = ColorStateList.valueOf(0xFFF1F5FF);
            text.setTextColor(white);
            // top_bar_apps deliberately clears tint so the app's own logo keeps its colours.
            if (text.getCompoundDrawableTintList() != null) text.setCompoundDrawableTintList(white);
        }
        if (view instanceof ImageView && view.isDuplicateParentStateEnabled()) {
            ((ImageView) view).setImageTintList(ColorStateList.valueOf(0xFFF1F5FF));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) controls(group.getChildAt(i));
        }
    }

    static void panel(View panel) {
        panel.setBackground(surface(panel, false, 20));
        controls(panel);
    }
}
