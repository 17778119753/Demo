package com.example.jsoup_engine;

import static com.example.jsoup_engine.global.Contants.TAG_CONSTANT;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.example.jsoup_engine.global.Contants;
import com.example.jsoup_engine.global.TGlobalApplicationWrapper;
import com.example.jsoup_engine.utils.JsoupUtil;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JsoupEngineImpl extends JsoupEngine {

  private Application mApp;
  private boolean isStop = false;

  @Override
  public void initSDK(Application app) {
    TGlobalApplicationWrapper wrapper = new TGlobalApplicationWrapper(app);
    wrapper.onCreate();
    mApp = Contants.getTGlobalApplication().getApplication();
  }

  @Override
  public void startJsoup(String url, JsoupListener listener) {
    isStop = false;
    startJsoup(url, Contants.mDefaultRoot + Contants.fileName, listener);
  }

  @Override
  public void startJsoup(String url, String saveFolderPath, JsoupListener listener) {
    isStop = false;
    startJsoupimpl(url, saveFolderPath, listener);
  }

  @Override
  public void stopJsoup() {
    isStop = true;
    if (threadPoolExecutor != null && threadPoolExecutor.isShutdown()) {
      threadPoolExecutor.shutdownNow();
    }
  }

  private ThreadPoolExecutor threadPoolExecutor;
  private Map<Integer, List<String>> mDeepLinkMap = new HashMap<>();
  /** 以下代码为具体处理逻辑 */
  public void startJsoupimpl(
      final String url, final String saveFolderPath, final JsoupListener listener) {
    if (isStop) return;
    Log.e(TAG_CONSTANT, "开始:link=" + url);
    handler.post(
        new Runnable() {
          @Override
          public void run() {
            listener.jsoupStatus(url, 0, "开始连接网络资源");
          }
        });
    threadPoolExecutor = new ScheduledThreadPoolExecutor(20);
    threadPoolExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            Connection connect = Jsoup.connect(url);
            handler.post(
                new Runnable() {
                  @Override
                  public void run() {
                    listener.jsoupStatus(url, 1, "网页连接成功，开始收集网络资源");
                  }
                });
            try {
              Document document = connect.get();
              Elements imgs = document.select("img[src]");
              Elements links = document.select("a[href]");
              final List<String> srcList = imgs.eachAttr("src");
              final List<String> linkList = links.eachAttr("abs:href");
              Iterator itStr = srcList.iterator();
              while (itStr.hasNext()) {
                String src = (String) itStr.next();
                if (TextUtils.isEmpty(src)
                    || !src.startsWith("http")
                    || (!src.toLowerCase().endsWith(".png")
                        && !src.toLowerCase().endsWith(".jpg"))) {
                  itStr.remove();
                }
              }

              Iterator itLink = srcList.iterator();
              while (itLink.hasNext()) {
                String link = (String) itLink.next();
                if (!isValid(link)) {
                  itLink.remove();
                }
              }
              handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      listener.jsoupStatus(url, 2, "资源收集成功");
                    }
                  });
              handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      if (srcList.size() == 0) {
                        listener.jsoupResult(url, srcList, linkList, saveFolderPath, false);
                      }
                      listener.jsoupProgress(url, 0, "无", srcList);
                    }
                  });
              // 下载成功的才会引入到组中进行保存，并通过result传输出去
              final List<String> strings = new ArrayList<>();
              for (int i = 0; i < srcList.size(); i++) {
                // 暂停后，停止下载，将该次结果传输出去
                if (isStop) {
                  handler.post(
                      new Runnable() {
                        @Override
                        public void run() {
                          listener.jsoupResult(url, strings, linkList, saveFolderPath, true);
                        }
                      });
                  return;
                }
                final String srcUrl = srcList.get(i);
                // 下载图片
                JsoupUtil.downImages(saveFolderPath, srcUrl);
                strings.add(srcUrl);
                if (i == srcList.size() - 1) {
                  Log.e(TAG_CONSTANT, "结束:link=" + url);
                  threadPoolExecutor.shutdownNow();
                  // 刷新相册
                  File f = new File(saveFolderPath);
                  Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                  Uri uri = Uri.fromFile(f);
                  intent.setData(uri);
                  mApp.sendBroadcast(intent);
                  // 当前Link对标的Url全部下载完成，开始遍历第一层中所有的Link。作用第二层的源URL
                  // 这里需要获取总的Link进行遍历，当经历第二层深度时，每个Link都会对应一个Link组，需要将每个Link组集中起来，在第三层的时候一起遍历，第三层、四层....都是如此
                  handler.post(
                      new Runnable() {
                        @Override
                        public void run() {
                          listener.jsoupResult(url, strings, linkList, saveFolderPath, false);
                        }
                      });
                }
                final int finalI = i;
                handler.post(
                    new Runnable() {
                      @Override
                      public void run() {
                        listener.jsoupProgress(url, finalI + 1, srcUrl, srcList);
                      }
                    });
              }
            } catch (final IOException e) {
              e.printStackTrace();
              Log.e(TAG_CONSTANT, "链接Link过程出现异常:=>" + e.getMessage());
              handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      listener.jsoupStatus(url, -1, e.getMessage());
                    }
                  });
            }
          }
        });
  }

  private Handler handler = new Handler(Looper.getMainLooper());

  /** 判断链接是否有效 */
  public static boolean isValid(String strLink) {
    URL url;
    try {
      url = new URL(strLink);
      HttpURLConnection connt = (HttpURLConnection) url.openConnection();
      connt.setRequestMethod("HEAD");
      String strMessage = connt.getResponseMessage();
      if (strMessage.compareTo("Not Found") == 0) {
        return false;
      }
      connt.disconnect();
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
