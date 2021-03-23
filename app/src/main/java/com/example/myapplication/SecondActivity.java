package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jsoup_engine.JsoupEngine;
import com.example.jsoup_engine.callback.JsoupListener;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

  private ProgressBar bar;
  private Button btnStart;
  private TextView count;
  private TextView linkTv;
  private TextView allCount;
  private EditText edit;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_second);
    bar = (ProgressBar) findViewById(R.id.bar);
    btnStart = (Button) findViewById(R.id.btn_start);
    count = (TextView) findViewById(R.id.count);
    linkTv = (TextView) findViewById(R.id.link);
    allCount = (TextView) findViewById(R.id.allCount);
    edit = (EditText) findViewById(R.id.edit);
    XXPermissions.with(this)
        .permission(Permission.Group.STORAGE)
        .request(
            new OnPermission() {

              @Override
              public void hasPermission(List<String> granted, boolean isAll) {
                Log.d("chenhan", "授权成功");
              }

              @Override
              public void noPermission(List<String> denied, boolean quick) {
                Toast.makeText(SecondActivity.this, "请务必授权", Toast.LENGTH_LONG).show();
                SecondActivity.this.finish();
              }
            });
  }

  public void stop(View view) {
    stoped = true;
    JsoupEngine.getInstance().stopJsoup();
    btnStart.setEnabled(true);
    btnStart.setText("开始爬虫");
  }

  private boolean stoped = false;

  public void start(View view) {
    stoped = false;
    edit.setText(
        "https://image.baidu.com/search/wisemidresult?tn=wisemidresult&word=%E6%97%85%E6%B8%B8&pn=0&rn=6&size=mid&sp=5&iswiseala=1&ie=utf8&fmpage=index&pos=jingcaitj");

    if (TextUtils.isEmpty(edit.getText().toString())) {
      Toast.makeText(this, "链接资源为空", Toast.LENGTH_SHORT).show();
      return;
    }
    mOldLink.clear();
    mNewLink.clear();
    btnStart.setEnabled(false);
    btnStart.setText("爬虫中...");
    JsoupEngine.getInstance().startJsoup(edit.getText().toString(), listener);
  }

  // 已爬虫完的Link组，防止重复爬虫
  private List<String> mOldLink = new ArrayList<>();
  // 未爬虫的Link组，爬虫过程中收集
  private List<String> mNewLink = new ArrayList<>();
  // 前几次Link总体爬虫量
  private int mAllCount = 0;

  private JsoupListener listener =
      new JsoupListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void jsoupProgress(
            String link, int progress, String currentUrl, List<String> srcList) {
          count.setText(
              "当前Link: "
                  + link
                  + "\n"
                  + "当前Link中Img总量: "
                  + srcList.size()
                  + "\n"
                  + "当前Link中爬虫个数: "
                  + progress);
          bar.setMax(srcList.size());
          bar.setProgress(progress);
          if (!TextUtils.equals(linkTv.getText().toString(), currentUrl)) {
            linkTv.setText("正在爬虫对象: " + currentUrl);
          }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void jsoupResult(
            String link, List<String> srcList, List<String> linkList, String saveFolderPath) {
          mAllCount = mAllCount + srcList.size();
          allCount.setText("总爬虫个数: " + mAllCount);
          mOldLink.add(link);
          if (mNewLink.contains(link)) mNewLink.remove(link);
          mNewLink.addAll(linkList);
          if (!stoped) handler.sendEmptyMessageDelayed(100, 100);
        }

        @Override
        public void jsoupStatus(String link, int code, String msg) {
          if (code == -1) {
            // 无效的链接不存储到Old组中，不做记录
            if (mNewLink.contains(link)) mNewLink.remove(link);
            handler.sendEmptyMessageDelayed(100, 100);
          }
          if (code == 0) {
            btnStart.setText("连接网络资源...");
          }
          if (code == 1) {
            btnStart.setText("收集网络资源...");
          }

          if (code == 2) {
            btnStart.setText("爬虫中...");
          }
        }
      };

  private Handler handler =
      new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
          super.handleMessage(msg);
          if (mNewLink.size() > 0) {
            String nextLink = getNextLink();
            JsoupEngine.getInstance().startJsoup(nextLink, listener);
          } else {
            Toast.makeText(SecondActivity.this, "爬虫结束", Toast.LENGTH_SHORT).show();
            btnStart.setEnabled(true);
            btnStart.setText("开始爬虫");
          }
        }
      };

  private String getNextLink() {
    Iterator nextLink = mNewLink.iterator();
    while (nextLink.hasNext()) {
      String link = (String) nextLink.next();
      if (mOldLink.contains(link)) {
        nextLink.remove();
      } else {
        return link;
      }
    }
    return null;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
  }
}
