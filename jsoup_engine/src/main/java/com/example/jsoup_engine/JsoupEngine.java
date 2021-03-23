package com.example.jsoup_engine;

import android.app.Application;
import com.example.jsoup_engine.callback.ElementsCallBack;
import com.example.jsoup_engine.callback.JsoupListener;
import java.io.UnsupportedEncodingException;

public abstract class JsoupEngine {

  private static volatile JsoupEngineImpl mEngineImpl = null;

  public static synchronized JsoupEngine getInstance() {
    if (mEngineImpl == null) {
      mEngineImpl = new JsoupEngineImpl();
    }
    return mEngineImpl;
  }

  public abstract void initSDK(Application app);

  public abstract void startJsoup(String url, JsoupListener listener);

  /**
   * 爬虫API，只能爬取网页端的相关资源
   *
   * @param link 爬虫网址
   * @param saveFolderPath 爬虫后图片保存地址，不传默认保存在根目录jsoup下
   * @param listener 爬虫结果
   */
  public abstract void startJsoup(String link, String saveFolderPath, JsoupListener listener);

  /** 停止本次爬虫 */
  public abstract void stopJsoup();

  /**
   * 返回Document，开发者自行获取所需资源
   *
   * @param link 爬虫对象URL
   * @param charsetName 编码格式
   * @param callBack 回调接口
   */
  public abstract void getElements(String link, String charsetName, ElementsCallBack callBack);

  /**
   * GBK转UTF-8的方法
   *
   * @param gbkStr
   * @return
   * @throws UnsupportedEncodingException
   */
  public abstract String getUTF8BytesFromGBKString(String gbkStr)
      throws UnsupportedEncodingException;
}
