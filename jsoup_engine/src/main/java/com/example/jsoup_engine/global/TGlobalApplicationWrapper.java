package com.example.jsoup_engine.global;

import android.app.Application;
import com.example.jsoup_engine.utils.JsoupUtil;

public class TGlobalApplicationWrapper implements TGlobalApplication {

  private Application mApp;

  public TGlobalApplicationWrapper(Application app) {
    mApp = app;
  }

  @Override
  public Application getApplication() {
    return mApp;
  }

  /** 在此方法中做SDK工具相关的初始化，例如日志手机回捞、网络线程初始化等 */
  public void onCreate() {
    Contants.setTGlobalApplication(this);
    Contants.mDefaultRoot = JsoupUtil.getRootDir(mApp);
  }
}
