package com.example.jsoup_engine.callback;

import java.util.List;

public interface JsoupListener {

  /**
   * 当前爬虫进度
   *
   * @param link 当前爬虫的link
   * @param progress 当前link，爬虫的进度
   * @param currentUrl 当前link下，正在操作的网络图片URL
   * @param srcList 当前link对应的网络图片URL总量
   */
  void jsoupProgress(String link, int progress, String currentUrl, List<String> srcList);

  /**
   * 当前Link爬虫结束的回调 触发时机：开发者调用了stopJsoup、当前Link下无网络图片及网络图片全部下载完毕
   *
   * @param link 当前爬虫的link
   * @param srcList 当前link下，已爬虫成功的网络图片组
   * @param linkList 当前link对应的link总量
   * @param saveFolderPath 保存地址
   */
  void jsoupResult(String link, List<String> srcList, List<String> linkList, String saveFolderPath);

  /**
   * 状态回调
   *
   * @param link 当前爬虫的link
   * @param code 状态码 0:开始连接网络资源 -1:网页连接失败 1:网页连接成功，开始收集网络资源 2:资源收集完成（爬虫真实开始）
   * @param msg 回调信息字符串
   */
  void jsoupStatus(String link, int code, String msg);
}
