package vn.baoto.doc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

class Ui {
    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static Button btn(Context c, String text, View.OnClickListener l) {
        Button b = new Button(c);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setTextColor(Color.BLACK);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setStroke(dp(c, 2), Color.BLACK);
        g.setCornerRadius(dp(c, 6));
        b.setBackground(g);
        b.setMinHeight(dp(c, 56));
        b.setMinimumHeight(dp(c, 56));
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(c, 6), 0, dp(c, 6), 0);
        b.setOnClickListener(l);
        return b;
    }

    static LinearLayout.LayoutParams weight(Context c, float w) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, w);
        int m = dp(c, 3);
        p.setMargins(m, m, m, m);
        return p;
    }

    static LinearLayout.LayoutParams weight(Context c) {
        return weight(c, 1f);
    }
}
