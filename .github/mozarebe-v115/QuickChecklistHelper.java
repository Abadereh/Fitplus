package com.aoto.mozarebe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class QuickChecklistHelper {
    private static final String PREFS = "mozarebe_prefs";
    private static final String PREF_QUICK = "detail_checklist_quick_view_v115";
    private static final int MAX_ITEMS = 20;
    private static final int COLS = 4;

    private QuickChecklistHelper() {}

    public static boolean render(final Activity activity, final LinearLayout content,
                                 final long profileId, final List<?> checks) {
        if (activity == null || content == null || checks == null || checks.isEmpty()) return false;

        final SharedPreferences prefs = activity.getSharedPreferences(PREFS, 0);
        final boolean quick = prefs.getBoolean(PREF_QUICK, false);

        LinearLayout selector = new LinearLayout(activity);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        selector.setGravity(Gravity.CENTER_VERTICAL);

        final Button listButton = modeButton(activity, "لیستی", !quick);
        final Button quickButton = modeButton(activity, "سریع", quick);
        LinearLayout.LayoutParams modeLp1 = new LinearLayout.LayoutParams(0, dp(activity, 38), 1f);
        LinearLayout.LayoutParams modeLp2 = new LinearLayout.LayoutParams(0, dp(activity, 38), 1f);
        modeLp1.setMargins(dp(activity, 3), 0, dp(activity, 3), 0);
        modeLp2.setMargins(dp(activity, 3), 0, dp(activity, 3), 0);
        selector.addView(listButton, modeLp1);
        selector.addView(quickButton, modeLp2);
        LinearLayout.LayoutParams selectorLp = new LinearLayout.LayoutParams(-1, -2);
        selectorLp.setMargins(0, 0, 0, dp(activity, 6));
        content.addView(selector, selectorLp);

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!quick) return;
                prefs.edit().putBoolean(PREF_QUICK, false).apply();
                reopen(activity, profileId);
            }
        });
        quickButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (quick) return;
                prefs.edit().putBoolean(PREF_QUICK, true).apply();
                reopen(activity, profileId);
            }
        });

        if (!quick) return false;

        if (content.getChildCount() > 1) content.getChildAt(1).setVisibility(View.GONE);
        if (content.getChildCount() > 2) content.getChildAt(2).setVisibility(View.GONE);
        if (content.getChildCount() > 3 && content.getChildAt(3) instanceof TextView) {
            TextView title = (TextView) content.getChildAt(3);
            title.setText("نمای سریع موارد تشکیل پرونده");
            title.setTextSize(17f);
        }

        final LinearLayout grid = new LinearLayout(activity);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int count = Math.min(checks.size(), MAX_ITEMS);
        int rows = Math.max(1, (count + COLS - 1) / COLS);
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int reserved = dp(activity, 220);
        int gaps = dp(activity, 4) * Math.max(0, rows - 1);
        int available = Math.max(dp(activity, 290), dm.heightPixels - reserved - gaps);
        int rowHeight = available / rows;
        rowHeight = Math.max(dp(activity, 58), Math.min(dp(activity, 88), rowHeight));

        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            row.setGravity(Gravity.CENTER);
            for (int c = 0; c < COLS; c++) {
                int index = r * COLS + c;
                LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(0, rowHeight, 1f);
                cellLp.setMargins(dp(activity, 2), dp(activity, 2), dp(activity, 2), dp(activity, 2));
                if (index < count) {
                    final Object item = checks.get(index);
                    final TextView card = makeCard(activity, item);
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            toggle(activity, content, profileId, item, card);
                        }
                    });
                    card.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override public boolean onLongClick(View v) {
                            Toast.makeText(activity, stringField(item, "label"), Toast.LENGTH_LONG).show();
                            return true;
                        }
                    });
                    row.addView(card, cellLp);
                } else {
                    row.addView(new View(activity), cellLp);
                }
            }
            grid.addView(row, new LinearLayout.LayoutParams(-1, rowHeight + dp(activity, 4)));
        }
        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(-1, -2);
        gridLp.setMargins(0, 0, 0, dp(activity, 4));
        content.addView(grid, gridLp);
        return true;
    }

    private static Button modeButton(Activity a, String text, boolean selected) {
        Button b = new Button(a);
        b.setAllCaps(false); b.setText(text); b.setTextSize(13f);
        b.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        b.setMinHeight(0); b.setMinimumHeight(0); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setPadding(dp(a, 8), 0, dp(a, 8), 0);
        b.setTextColor(selected ? Color.WHITE : Color.rgb(31, 78, 61));
        b.setBackground(rounded(selected ? Color.rgb(31, 78, 61) : Color.WHITE,
                Color.rgb(31, 78, 61), selected ? 0 : dp(a, 1), dp(a, 10)));
        return b;
    }

    private static TextView makeCard(Activity a, Object item) {
        TextView card = new TextView(a);
        card.setGravity(Gravity.CENTER);
        card.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        card.setTextDirection(View.TEXT_DIRECTION_RTL);
        card.setTextSize(9f);
        card.setIncludeFontPadding(false);
        card.setPadding(dp(a, 3), dp(a, 4), dp(a, 3), dp(a, 4));
        card.setMaxLines(5);
        card.setEllipsize(TextUtils.TruncateAt.END);
        styleCard(a, card, item);
        return card;
    }

    private static void styleCard(Activity a, TextView card, Object item) {
        boolean done = booleanField(item, "done");
        String label = stringField(item, "label");
        card.setText((done ? "✓\n" : "□\n") + label);
        card.setTypeface(Typeface.DEFAULT, done ? Typeface.BOLD : Typeface.NORMAL);
        card.setTextColor(done ? Color.rgb(31, 78, 61) : Color.rgb(45, 45, 45));
        card.setBackground(rounded(done ? Color.rgb(229, 244, 232) : Color.WHITE,
                done ? Color.rgb(77, 140, 103) : Color.rgb(205, 210, 215), dp(a, 1), dp(a, 9)));
    }

    private static void toggle(Activity a, LinearLayout content, long profileId, Object item, TextView card) {
        try {
            Object db = privateField(a, "db");
            Class<?> dbClass = db.getClass();
            String label = stringField(item, "label");
            long itemId = longField(item, "id");
            boolean current = booleanField(item, "done");

            Method specialMethod = declaredMethod(dbClass, "isInitialOnlyChecklistLabel", String.class);
            boolean special = ((Boolean) specialMethod.invoke(null, label)).booleanValue();
            boolean next;
            if (special) {
                Method initialMethod = declaredMethod(dbClass, "isInitialStage", long.class);
                boolean initial = ((Boolean) initialMethod.invoke(db, profileId)).booleanValue();
                next = !initial;
                Toast.makeText(a,
                        next ? "این مورد بعد از تکمیل اولیه به‌صورت خودکار تکمیل است."
                             : "این مورد در مرحله تکمیل اولیه باید انجام‌نشده بماند.",
                        Toast.LENGTH_SHORT).show();
            } else {
                next = !current;
            }

            setBooleanField(item, "done", next);
            Method setCheck = declaredMethod(dbClass, "setCheck", long.class, long.class, boolean.class);
            setCheck.invoke(db, profileId, itemId, next);
            styleCard(a, card, item);
            refreshHero(content, db, dbClass, profileId);
        } catch (Throwable t) {
            Toast.makeText(a, "خطا در ثبت تیک؛ دوباره تلاش کنید.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void refreshHero(LinearLayout content, Object db, Class<?> dbClass, long profileId) {
        try {
            Method getProfile = declaredMethod(dbClass, "getProfile", long.class);
            Object profile = getProfile.invoke(db, profileId);
            if (profile == null || content.getChildCount() == 0) return;
            Object hero = content.getChildAt(0);
            for (Method m : hero.getClass().getDeclaredMethods()) {
                if ("bind".equals(m.getName()) && m.getParameterTypes().length == 1) {
                    m.setAccessible(true); m.invoke(hero, profile); return;
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void reopen(Activity activity, long profileId) {
        try {
            Method m = activity.getClass().getDeclaredMethod("showProfileDetail", long.class);
            m.setAccessible(true); m.invoke(activity, profileId);
        } catch (Throwable t) {
            activity.recreate();
        }
    }

    private static Method declaredMethod(Class<?> c, String name, Class<?>... args) throws Exception {
        Method m = c.getDeclaredMethod(name, args); m.setAccessible(true); return m;
    }
    private static Object privateField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    private static Field field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f;
    }
    private static String stringField(Object target, String name) {
        try { Object v = field(target, name).get(target); return v == null ? "" : String.valueOf(v); }
        catch (Throwable t) { return ""; }
    }
    private static boolean booleanField(Object target, String name) {
        try { return field(target, name).getBoolean(target); } catch (Throwable t) { return false; }
    }
    private static long longField(Object target, String name) {
        try { return field(target, name).getLong(target); } catch (Throwable t) { return 0L; }
    }
    private static void setBooleanField(Object target, String name, boolean value) throws Exception {
        field(target, name).setBoolean(target, value);
    }
    private static GradientDrawable rounded(int fill, int stroke, int strokeWidth, int radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(fill); g.setCornerRadius(radius);
        if (strokeWidth > 0) g.setStroke(strokeWidth, stroke); return g;
    }
    private static int dp(Activity a, int value) {
        return Math.round(value * a.getResources().getDisplayMetrics().density);
    }
}