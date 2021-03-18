package com.example.myapplication;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

  private ThreadPoolExecutor threadPoolExecutor;
  private ProgressBar bar;
  private Button btnStart;
  private TextView count;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_second);
    bar = (ProgressBar) findViewById(R.id.bar);
    btnStart = (Button) findViewById(R.id.btn_start);
    count = (TextView) findViewById(R.id.count);
    threadPoolExecutor = new ScheduledThreadPoolExecutor(20);
    XXPermissions.with(this)
        // .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
        // .permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES)
        // //支持请求6.0悬浮窗权限8.0请求安装权限
        .permission(Permission.Group.STORAGE) // 不指定权限则自动获取清单中的危险权限
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

  public void update(View view) {
    File f = new File(FetchImgsUtil.getRootDir(SecondActivity.this) + "/chenhan");
    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    Uri uri = Uri.fromFile(f);
    intent.setData(uri);
    SecondActivity.this.sendBroadcast(intent);
  }

  public void start(View view) {

    btnStart.setEnabled(false);
    btnStart.setText("爬虫中...");
    threadPoolExecutor = new ScheduledThreadPoolExecutor(20);
    threadPoolExecutor.execute(
        new Runnable() {
          @Override
          public void run() {

            Connection connect = Jsoup.connect("https://www.csdn.net/");
            try {
              Document document = connect.get();
              Elements imgs = document.getElementsByTag("img");
              Log.d("chenhan", "共检测到下列图片URL：" + imgs.size());
              runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
                      bar.setMax(imgs.size());
                    }
                  });

              for (int i = 0; i < imgs.size(); i++) {
                String imgSrc = imgs.get(i).attr("abs:src");

                int finalI = i;
                runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        if (finalI == imgs.size() - 1) {
                          threadPoolExecutor.shutdownNow();
                          bar.setMax(imgs.size());
                          btnStart.setEnabled(true);
                          btnStart.setText("开始爬虫");
                          File f =
                              new File(FetchImgsUtil.getRootDir(SecondActivity.this) + "/chenhan");
                          Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                          Uri uri = Uri.fromFile(f);
                          intent.setData(uri);
                          SecondActivity.this.sendBroadcast(intent);
                        } else {
                          bar.setProgress(finalI + 1);
                        }
                      }
                    });

                if (TextUtils.isEmpty(imgSrc)) {
                  continue;
                }

                Log.d("chenhan", "开始下载 imgSrc = " + imgSrc);
                FetchImgsUtil.downImages(
                    FetchImgsUtil.getRootDir(SecondActivity.this) + "/chenhan", imgSrc);
                Log.e("chenhan", "下载完成");
              }

            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }
}
