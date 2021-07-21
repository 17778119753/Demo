package com.example.tablayout;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.tabs.TabLayout;

public class NumTabLayout extends TabLayout {
  /** 一行显示的Tab数量，多余部分通过scrollable滑动查看 */
  private static final int TabViewNumber = 5;
  /** API28以上对应字段变为scrollableTabMinWidth */
  private static final String SCROLLABLE_TAB_MIN_WIDTH = "mScrollableTabMinWidth";

  private static final String SCROLLABLE_TAB_MIN_WIDTH_28 = "scrollableTabMinWidth";

  public NumTabLayout(Context context) {
    super(context);
    initTabMinWidth();
  }

  public NumTabLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initTabMinWidth();
  }

  public NumTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initTabMinWidth();
  }

  private void initTabMinWidth() {
    //    int screenWidth = getResources().getDisplayMetrics().widthPixels;
    //    int tabMinWidth = screenWidth / TabViewNumber;
    //    Field field;
    //    try {
    //      field = TabLayout.class.getDeclaredField(SCROLLABLE_TAB_MIN_WIDTH_28);
    //      // todo:不要忘记打开注释..........
    //      /*if (Au.targetSdkVersion() < 28) {
    //        field = TabLayout.class.getDeclaredField(SCROLLABLE_TAB_MIN_WIDTH);
    //      } else {
    //        field = TabLayout.class.getDeclaredField(SCROLLABLE_TAB_MIN_WIDTH_28);
    //      }*/
    //      field.setAccessible(true); // 去除私有属性
    //      field.set(this, tabMinWidth);
    //    } catch (NoSuchFieldException e) {
    //      e.printStackTrace();
    //    } catch (IllegalAccessException e) {
    //      e.printStackTrace();
    //    }
  }
}
