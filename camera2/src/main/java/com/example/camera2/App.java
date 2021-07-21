package com.example.camera2;

import android.app.Application;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    ContextHolder.init(this);
  }
}
