package com.example.camera2;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/** User: molikto Date: 12/29/14 Time: 04:43 */
public class MetricsUtil {

  public static final int DP_1 = dp(1);
  public static final int DP_2 = dp(2);
  public static final int DP_3 = dp(3);
  public static final int DP_4 = dp(4);
  public static final int DP_5 = dp(5);
  public static final int DP_6 = dp(6);
  public static final int DP_8 = dp(8);
  public static final int DP_10 = dp(10);
  public static final int DP_12 = dp(12);
  public static final int DP_14 = dp(14);
  public static final int DP_15 = dp(15);
  public static final int DP_16 = dp(16);
  public static final int DP_18 = dp(18);
  public static final int DP_20 = dp(20);
  public static final int DP_22 = dp(22);
  public static final int DP_24 = dp(24);
  public static final int DP_26 = dp(26);
  public static final int DP_28 = dp(28);
  public static final int DP_30 = dp(30);
  public static final int DP_31 = dp(31);
  public static final int DP_32 = dp(32);
  public static final int DP_34 = dp(34);
  public static final int DP_36 = dp(36);
  public static final int DP_40 = dp(40);
  public static final int DP_42 = dp(42);
  public static final int DP_48 = dp(48);
  public static final int DP_50 = dp(50);
  public static final int DP_54 = dp(54);
  public static final int DP_55 = dp(55);
  public static final int DP_56 = dp(56);
  public static final int DP_60 = dp(60);
  public static final int DP_64 = dp(64);
  public static final int DP_65 = dp(65);
  public static final int DP_70 = dp(70);
  public static final int DP_72 = dp(72);
  public static final int DP_80 = dp(80);
  public static final int DP_82 = dp(82);
  public static final int DP_86 = dp(86);
  public static final int DP_88 = dp(86);
  public static final int DP_90 = dp(90);
  public static final int DP_96 = dp(96);
  public static final int DP_98 = dp(98);
  public static final int DP_105 = dp(105);
  public static final int DP_120 = dp(120);
  public static final int DP_132 = dp(132);
  public static final int DP_156 = dp(156);
  public static final int DP_160 = dp(160);
  public static final int DP_176 = dp(176);
  public static final int DP_206 = dp(206);
  public static final int DP_214 = dp(214);
  public static final int DP_255 = dp(255);

  public static DisplayMetrics displayMetrics() {
    return ContextHolder.context().getResources().getDisplayMetrics();
  }

  private static Point displayRealSize;

  public static Point displayRealSize() {
    if (displayRealSize == null) {
      displayRealSize = new Point();
    }

    WindowManager wm =
        (WindowManager) ContextHolder.context().getSystemService(Context.WINDOW_SERVICE);
    if (Build.VERSION.SDK_INT < 17) {
      DisplayMetrics metrics = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(metrics);
      displayRealSize.x = metrics.widthPixels;
      displayRealSize.y = metrics.heightPixels;
    } else {
      wm.getDefaultDisplay().getRealSize(displayRealSize);
    }
    return displayRealSize;
  }

  public static int dp(float p) {
    return (int) (displayMetrics().density * p);
  }

  public static int sp(int p) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, p, displayMetrics());
  }
}
