package com.android.internal.util;

import android.content.Context;

public class AllianceUtils {

    public static int dpToPx(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static int pxToDp(Context context, int px) {
        return (int) ((px / context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static boolean isNavBarDefault(Context context) {
        return context.getResources().getBoolean(com.android.internal.R.bool_config_showNavigationBar);
    }
}