package com.example.jsoup_engine.global;

public class Contants {

  public static final String TAG_CONSTANT = "jsoup";
  public static final String fileName = "/jsoup";
  public static String mDefaultRoot = null;

  private static TGlobalApplication mApp;

  public static void setTGlobalApplication(TGlobalApplication app) {
    mApp = app;
  }

  public static TGlobalApplication getTGlobalApplication() {
    if (mApp == null) {
      throw new RuntimeException("please init sdk first!");
    }
    return mApp;
  }
}
