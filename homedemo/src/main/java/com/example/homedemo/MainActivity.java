package com.example.homedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends Activity {

  private String APK_PATH;
  private String PLUGIN_ACTIIVTY_CLASS_NAME = "com.example.plugindemo01.MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /** 基本类型所占的字节数是固定的，int占用4个字节，double占用8个字节，char占用2个字节 */

    /** 中文字符，UTF-8，一个中文字符占3个字节。gbk编码，一个中文字符占2个字节 */
    String str = "我们的花朵";
    // 下面打印结果5/15
    try {
      Log.e("chenhan", "中文字符:字符串长度：" + str.length() + ";占用字节数：" + str.getBytes("gbk").length);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    /** 英文字符，UTF-8，一个英文字符占1个字节 */
    String str1 = "abcde";
    // 下面打印结果5/5
    Log.e("chenhan", "英文字符:字符串长度：" + str1.length() + ";占用字节数：" + str1.getBytes().length);

    XXPermissions.with(this)
        .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
        .request(
            new OnPermission() {
              @Override
              public void hasPermission(List<String> granted, boolean isAll) {
                Log.d("chenhan", "授权成功");
                String root = getRootDir(MainActivity.this);
                Log.d("chenhan", "root=" + root);

                File[] rootFiles = new File(root).listFiles();
                if (rootFiles == null) {
                  Log.d("chenhan", "rootFiles == null");
                  return;
                }

                File[] files = new File(root + "/plugin").listFiles();
                if (files == null) {
                  Log.d("chenhan", "files == null");
                  return;
                }
                APK_PATH = files[0].getAbsolutePath();
                Log.d("chenhan", "APK_PATH = " + APK_PATH);
              }

              @Override
              public void noPermission(List<String> denied, boolean quick) {}
            });
  }

  public void goSecond(View view) {
    Intent intent = new Intent();
    intent.putExtra(ProxyActivity.PLUGIN_DEX_PATH, APK_PATH);
    intent.putExtra(ProxyActivity.PLUGIN_ACTIIVTY_CLASS_NAME, PLUGIN_ACTIIVTY_CLASS_NAME);
    intent.setClass(MainActivity.this, ProxyActivity.class); // 调起代理Activity
    startActivity(intent);
  }

  public static String getRootDir(Context context) {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      // 优先获取SD卡根目录[/storage/sdcard0]
      return Environment.getExternalStorageDirectory().getAbsolutePath();
    } else {
      // 应用缓存目录[/data/data/应用包名/cache]
      return context.getCacheDir().getAbsolutePath();
    }
  }
}
