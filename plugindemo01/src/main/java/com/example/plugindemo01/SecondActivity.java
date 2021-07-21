package com.example.plugindemo01;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SecondActivity extends Activity {

  @SuppressLint("MissingSuperCall")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    Log.e("chenhan", "插件plugindemo01中的SecondActivity:onCreate方法被调用了");
    // setContentView(R.layout.activity_second);
  }
}
