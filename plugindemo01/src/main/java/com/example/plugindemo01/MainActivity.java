package com.example.plugindemo01;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class MainActivity extends Activity {

  Activity mProxyActivity = null;

  public void setProxyActivity(Activity proxyActivity) {
    Log.e("chenhan", "插件plugindemo01中的MainActivity:setProxyActivity方法被调用了");
    mProxyActivity = proxyActivity;
  }

  @SuppressLint("MissingSuperCall")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    Log.e("chenhan", "插件plugindemo01中的MainActivity:onCreate方法被调用了");
    // mProxyActivity.setContentView(getContextView());
    mProxyActivity.setContentView(R.layout.activity_main);
    Button button = mProxyActivity.findViewById(R.id.jump);
    button.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.e("chenhan", "插件plugindemo01中的MainActivity:Button方法被调用了");
            mProxyActivity.startActivity(new Intent(mProxyActivity, SecondActivity.class));
          }
        });
  }

  public View getContextView() {
    Button button = new Button(mProxyActivity);
    button.setLayoutParams(new LayoutParams(300, 100));
    button.setText("跳转按钮");
    button.setBackgroundColor(Color.parseColor("#111111"));
    button.setTextColor(Color.parseColor("#ffffff"));
    button.setOnClickListener(
        v -> {
          mProxyActivity.startActivity(new Intent(mProxyActivity, SecondActivity.class));
        });
    return button;

    //    TextView textView = new TextView(mProxyActivity);
    //    textView.setText("我是Plugin的MainActivity");
    //    return textView;
  }

  @Override
  public Resources getResources() {
    if (mProxyActivity != null) {
      return mProxyActivity.getResources();
    }
    return super.getResources();
  }

  public void goSecond(View view) {}
}
