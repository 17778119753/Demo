package com.example.tablayout;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnScrollChangeListener;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private NumTabLayout tabs;
  private ViewPager pager;

  private ArrayList<Fragment> fragments = new ArrayList<>();
  private Adapter pagerAdapter;
  private String[] titles =
      new String[] {
        "拍拍生活", "行业交流", "情绪分享", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "行业交流", "情绪分享",
        "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为"
        //                , "校园生活", "兴趣爱好", "情感大师", "分手大师",
        // "你来我往",
        //        "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师",
        // "分手大师",
        //        "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好",
        // "情感大师",
        //        "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活",
        // "兴趣爱好",
        //        "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为",
        // "校园生活",
        //        "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往",
        // "随性而为",
        //        "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师",
        // "你来我往",
        //        "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师",
        // "分手大师",
        //        "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好",
        // "情感大师",
        //        "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为", "校园生活",
        // "兴趣爱好",
        //        "情感大师", "分手大师", "你来我往", "随性而为", "校园生活", "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为",
        // "校园生活",
        //        "兴趣爱好", "情感大师", "分手大师", "你来我往", "随性而为"
      }; //
  private AdapterStatics statics;

  private Handler mHandler =
      new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          switch (msg.what) {
            case 0x01:
              statics.runRefresher();
              break;
          }
        }
      };

  @RequiresApi(api = VERSION_CODES.M)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    tabs = (NumTabLayout) findViewById(R.id.tabs);
    pager = (ViewPager) findViewById(R.id.pager);

    List<String> ms = new ArrayList<>();

    statics =
        new AdapterStatics<String>() {
          @Override
          public void reportActived(String s, int index) {
            Log.i("chenhan", "actived " + s + ";index=" + index);
            // ms.add(s);

            //            for (int i = 0; i < ms.size(); i++) {
            //              Log.i("chenhan", "actived " + ms.get(i));
            //            }
          }

          @Override
          public void reportRemoved(String s, int index, long time) {
            // Log.i("chenhan", "removed " + s);
            // ms.remove(s);
          }
        };

    for (int i = 0; i < titles.length; i++) {
      fragments.add(new FeedKanTopicFrag());
      Tab tab = tabs.newTab();
      statics.onBindData(tab.view, "tab" + i, i);
      tabs.addTab(tab);
    }

    // tabs.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_STRETCH);
    tabs.setOnScrollChangeListener(
        new OnScrollChangeListener() {
          @Override
          public void onScrollChange(
              View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            // Log.e("chenhan", "onScrollChange");
            // 不断发送事件，不断取消，当外面接收到事件时，表示没有取消，也就是停止滑动了
            //            mHandler.removeCallbacksAndMessages(null);
            //            mHandler.sendEmptyMessageDelayed(0x01, 200);
            statics.runRefresher();
          }
        });

    tabs.addOnTabSelectedListener(
        new OnTabSelectedListener() {
          @Override
          public void onTabSelected(Tab tab) {
            Log.e("chenhan", "onTabSelected");
            View customView = tab.getCustomView();
            if (customView == null) return;
            View tabView = customView.findViewById(R.id.divis);
            tabView.setVisibility(View.VISIBLE);
          }

          @Override
          public void onTabUnselected(Tab tab) {
            View customView = tab.getCustomView();
            if (customView == null) return;
            View tabView = customView.findViewById(R.id.divis);
            tabView.setVisibility(View.GONE);
            Log.e("chenhan", "onTabUnselected");
          }

          @Override
          public void onTabReselected(Tab tab) {
            Log.e("chenhan", "onTabReselected");
          }
        });

    pager.addOnPageChangeListener(
        new SimpleOnPageChangeListener() {
          @Override
          public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
              statics.runRefresher();
            }
          }
        });
    // pager.setCurrentItem(0);

    tabs.setupWithViewPager(pager, false);
    pagerAdapter = new Adapter(getSupportFragmentManager());
    pager.setAdapter(pagerAdapter);

    for (int i = 0; i < titles.length; i++) {
      // tabs.getTabAt(i).setText(titles[i]);
      tabs.getTabAt(i).setCustomView(makeTabView(i));
    }

    tabs.post(
        new Runnable() {
          @Override
          public void run() {
            statics.runRefresher();
          }
        });
  }

  /**
   * 引入布局设置图标和标题
   *
   * @param position
   * @return
   */
  private View makeTabView(int position) {
    View tabView = LayoutInflater.from(this).inflate(R.layout.tab_text_icon, null);
    TextView textView = tabView.findViewById(R.id.text);
    textView.setText(titles[position]);
    View views = tabView.findViewById(R.id.divis);
    if (position == 0) {
      views.setVisibility(View.VISIBLE);
    } else {
      views.setVisibility(View.GONE);
    }
    //    ImageView imageView = tabView.findViewById(R.id.imageview);
    //    textView.setText(titles[position]);
    //    imageView.setImageResource(pics[position]);
    // statics.runRefresher();
    return tabView;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mHandler.removeCallbacksAndMessages(null);
  }

  class Adapter extends FragmentPagerAdapter {

    public Adapter(@NonNull FragmentManager fm) {
      super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
      return fragments.get(position);
    }

    @Override
    public int getCount() {
      return fragments.size();
    }
  }
}
