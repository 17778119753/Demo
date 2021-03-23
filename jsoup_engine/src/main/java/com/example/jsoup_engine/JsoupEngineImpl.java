package com.example.jsoup_engine;

import static com.example.jsoup_engine.global.Contants.TAG_CONSTANT;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.example.jsoup_engine.callback.ElementsCallBack;
import com.example.jsoup_engine.callback.JsoupListener;
import com.example.jsoup_engine.global.Contants;
import com.example.jsoup_engine.global.TGlobalApplicationWrapper;
import com.example.jsoup_engine.utils.JsoupUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
  private volatile boolean isStop = false;

  public static final Map<String, String> HOME_HEARD_MAP = new HashMap<>();

  public static Map<String, String> getHomeHeardMap() {
    HOME_HEARD_MAP.put(
        "Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    HOME_HEARD_MAP.put("Accept-Encoding", "gzip, deflate, br");
    HOME_HEARD_MAP.put("Accept-Language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6");
    HOME_HEARD_MAP.put("Connection", "keep-alive");
    if (!HOME_HEARD_MAP.containsKey("Cookie")) {
      HOME_HEARD_MAP.put(
          "Cookie",
          "BIDUPSID=45401D87AD2D1AC10DC8EF4AF5BF2AAD; PSTM=1595318555; BAIDUID=45401D87AD2D1AC1B7A11A202D1726BA:FG=1; bdshare_firstime=1595501258246; H_WISE_SIDS=154034_154770_153759_151993_155858_149355_150967_156818_156286_155320_154259_155984_148867_155683_156096_154804_156622_153444_152409_131861_154772_155436_153755_151016_127969_154413_154175_155962_155331_152981_155908_150346_155803_146732_131423_154037_155394_154189_156945_155344_157024_154953_157075_151872_144966_153657_154214_154118_154801_154902_156726_155931_154145_147551_157028_153446_156606_152310_155388_154357_155864_110085_157006; MCITY=-187:; __yjs_duid=1_39c522ada6e30df532f8d767834b2a8e1614307604471; top_list=4244232993-7182834579; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; H_PS_PSSID=33514_33272_33570_33392_33460_22158; st_key_id=17; wise_device=0; delPer=0; PSINO=6; Hm_lvt_98b9d8c2fd6608d564bf2ac2ae642948=1614598932,1614598938,1614651172,1614656659; BCLID=6696522698307635931; BDSFRCVID=-xLOJeC62AC9at3eh_4A8PV7WjpqhyTTH6aoV9hsteac5gjTXm08EG0PfM8g0Ku-qw2ZogKK3gOTHxKF_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF=tR-qVIK5tIK3H48k-4QEbbQH-UnLq-RBtgOZ04n-ah05SCb5-4oYqjk3eb3pXt3-W20j0h7m3UTdfh76Wh35K5tTQP6rLtbpKeO4KKJxbp5sShOv5t5rDx_AhUJiB5OMBan7_qvIXKohJh7FM4tW3J0ZyxomtfQxtNRJ0DnjtpChbC8RejKBDj5Mbxv0K-vJ--o2LPoV-TrjDnCrqJ7dXUI8LNDH3xt8K6Pe0Rn7JpDWVML63P62Ktk-3bO7ttoyQJ53Q-bHKR8henc2W-F2eML1Db3hW6vMtg3ts4j5tfcoepvoDPJc3MkbyPjdJJQOBKQB0KnGbUQkeq8CQft20b0EeMtjW6LEK5r2SC_MJCP53j; BCLID_BFESS=6696522698307635931; BDSFRCVID_BFESS=-xLOJeC62AC9at3eh_4A8PV7WjpqhyTTH6aoV9hsteac5gjTXm08EG0PfM8g0Ku-qw2ZogKK3gOTHxKF_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF_BFESS=tR-qVIK5tIK3H48k-4QEbbQH-UnLq-RBtgOZ04n-ah05SCb5-4oYqjk3eb3pXt3-W20j0h7m3UTdfh76Wh35K5tTQP6rLtbpKeO4KKJxbp5sShOv5t5rDx_AhUJiB5OMBan7_qvIXKohJh7FM4tW3J0ZyxomtfQxtNRJ0DnjtpChbC8RejKBDj5Mbxv0K-vJ--o2LPoV-TrjDnCrqJ7dXUI8LNDH3xt8K6Pe0Rn7JpDWVML63P62Ktk-3bO7ttoyQJ53Q-bHKR8henc2W-F2eML1Db3hW6vMtg3ts4j5tfcoepvoDPJc3MkbyPjdJJQOBKQB0KnGbUQkeq8CQft20b0EeMtjW6LEK5r2SC_MJCP53j; tb_as_data=f91194c6824894d324c39c29837c6b9c50ec65fab018aeb4e474b20db842845825c96f71f4c6eb6aea2f61716f232e787570f1fcab17da5132635e67fb49ac3a2ee926ac26ab414b8dc1f022a26b6af0be5ec5feb08e47ecea40e7c3ac42af63418eb176202b934e8a5d65a31f7f67c9; Hm_lpvt_98b9d8c2fd6608d564bf2ac2ae642948=1614661234; st_sign=46af36f3; st_data=305c1867cacda6bc575bc022c406a997445a569b4e6fd53fec92a0642aee94c5d695d65fe4c5360c0f99c161476ba8dc7fb649742c1c4278775ae474ae817ef284ce4f5f60d1c0ad9b4c2c7002d944758ca3e2766e503b929c2f411069f9848b4e9bf304bec24493f1d19c3b7526fc3ce49228569294a5afc07905bed78d5368; BAIDUID_BFESS=CDDCC97066F658F1A310835A932F3477:FG=1; BA_HECTOR=04ahah0ga1a5800ko11g3ri0j0r; ZD_ENTRY=baidu; ab_sr=1.0.0_MmE3OWNlMWI1NjEwM2RiYTNmNmUwNjRiZTZiOWUxZjNhMWVjOTU5Y2ZjYzM3YTdiNWNhMTU1ZjRiZTFhNzRhZDU3NTk1Y2RkMGU4MzQyMzkzM2U4OTYzZTYxMDE3OGQ1");
    }
    HOME_HEARD_MAP.put(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36");
    return HOME_HEARD_MAP;
  }

  @Override
  public void initSDK(Application app) {
    TGlobalApplicationWrapper wrapper = new TGlobalApplicationWrapper(app);
    wrapper.onCreate();
    mApp = Contants.getTGlobalApplication().getApplication();
  }

  @Override
  public void startJsoup(String url, JsoupListener listener) {
    isStop = false;
    isChangeCookie = false;
    startJsoup(url, Contants.mDefaultRoot + Contants.fileName, listener);
  }

  @Override
  public void startJsoup(String url, String saveFolderPath, JsoupListener listener) {
    isStop = false;
    isChangeCookie = false;
    startJsoupimpl(url, saveFolderPath, listener);
  }

  @Override
  public void stopJsoup() {
    isStop = true;
    if (threadPoolExecutor != null && threadPoolExecutor.isShutdown()) {
      threadPoolExecutor.shutdown();
    }
  }

  @Override
  public void getElements(
      final String link, final String charsetName, final ElementsCallBack callBack) {
    threadPoolExecutor = new ScheduledThreadPoolExecutor(20);
    threadPoolExecutor.execute(
        new Runnable() {
          @Override
          public void run() {

            try {
              Connection conn = Jsoup.connect(link);
              byte[] body = conn.execute().bodyAsBytes();
              InputStream in = new ByteArrayInputStream(body);
              Document doc = Jsoup.parse(in, charsetName, link);
              callBack.result(doc);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }

  @Override
  public String getUTF8BytesFromGBKString(String gbkStr) throws UnsupportedEncodingException {
    int n = gbkStr.length();
    byte[] utfBytes = new byte[3 * n];
    int k = 0;
    for (int i = 0; i < n; i++) {
      int m = gbkStr.charAt(i);
      if (m < 128 && m >= 0) {
        utfBytes[k++] = (byte) m;
        continue;
      }
      utfBytes[k++] = (byte) (0xe0 | (m >> 12));
      utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
      utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
    }
    if (k < utfBytes.length) {
      byte[] tmp = new byte[k];
      System.arraycopy(utfBytes, 0, tmp, 0, k);
      utfBytes = tmp;
    }
    return new String(utfBytes, "UTF-8");
  }

  private boolean isChangeCookie = false;
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
            Connection connect = Jsoup.connect(url).headers(getHomeHeardMap());
            handler.post(
                new Runnable() {
                  @Override
                  public void run() {
                    listener.jsoupStatus(url, 1, "网页连接成功，开始收集网络资源");
                  }
                });
            try {
              Document document = connect.get();
              Log.e("chenhan", "document=" + document);
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
              if (isStop) return;
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
                        listener.jsoupResult(url, srcList, linkList, saveFolderPath);
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
                          listener.jsoupResult(url, strings, linkList, saveFolderPath);
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
                          listener.jsoupResult(url, strings, linkList, saveFolderPath);
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
