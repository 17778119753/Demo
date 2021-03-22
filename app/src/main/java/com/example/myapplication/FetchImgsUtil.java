package com.example.myapplication;

import android.content.Context;
import android.os.Environment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class FetchImgsUtil {

  /** 下载文件 */
  public static void downImages(String filePath, String imgUrl) {
    File dir = new File(filePath);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    // 截取图片文件名
    String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());

    try {
      // 文件名里面可能有中文或者空格，所以这里要进行处理。但空格又会被URLEncoder转义为加号
      String urlTail = URLEncoder.encode(fileName, "UTF-8");
      // 因此要将加号转化为UTF-8格式的%20
      imgUrl =
          imgUrl.substring(0, imgUrl.lastIndexOf('/') + 1) + urlTail.replaceAll("\\+", "\\%20");

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    String time = String.valueOf(System.currentTimeMillis());
    // 写出的路径
    File file = new File(filePath + File.separator + time.substring(5) + "_" + fileName);

    try {
      // 获取图片URL
      // Log.d("chenhan", "imgUrl = " + imgUrl);
      URL url = new URL(imgUrl);
      // 获得连接
      URLConnection connection = url.openConnection();
      // 设置10秒的相应时间
      connection.setConnectTimeout(10 * 1000);
      // 获得输入流
      InputStream in = connection.getInputStream();
      // 获得输出流
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      // 构建缓冲区
      byte[] buf = new byte[1024];
      int size;
      // 写入到文件
      while (-1 != (size = in.read(buf))) {
        out.write(buf, 0, size);
      }
      out.close();
      in.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** 获取手机根目录 */
  public static String getRootDir(Context context) {
    String path;
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      // 优先获取SD卡根目录[/storage/sdcard0]
      path = Environment.getExternalStorageDirectory().getAbsolutePath();
      return path;
    } else {
      // 应用缓存目录[/data/data/应用包名/cache]
      path = context.getCacheDir().getAbsolutePath();
      return path;
    }
  }
}
