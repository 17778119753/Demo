package com.example.myapplication;

import android.app.Application;
import com.example.jsoup_engine.JsoupEngine;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    JsoupEngine.getInstance().initSDK(this);
  }
}
