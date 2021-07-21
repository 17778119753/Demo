package com.example.camera2;

import android.app.Application;
import android.content.Context;

public class ContextHolder {

  public static boolean hadInit = false;
  public static Context context;

  public static void init(Context context) {
    if (context instanceof Application) {
      ContextHolder.context = context;
    } else {
      ContextHolder.context = context.getApplicationContext();
    }
    hadInit = true;
  }

  public static Context context() {
    if (hadInit) {
      return context;
    } else {
      throw new RuntimeException("you should call init first!");
    }
  }
}
