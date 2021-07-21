package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.IOException;
import java.io.InputStream;

public class TestBitmapAct extends AppCompatActivity {
  private ImageView mImg;
  private ConstraintLayout mLlContent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_bitmap);
    mImg = (ImageView) findViewById(R.id.img);
    mLlContent = (ConstraintLayout) findViewById(R.id.ll_content);
    mImg.post(
        () -> {
          getBitmap();
        });
  }

  public void getBitmap() {
    try {
      int totalHeight = mImg.getMeasuredHeight();
      int totalWidth = mImg.getMeasuredWidth();
      InputStream inputStream = getAssets().open("icon_large_height.jpeg");

      // 获得图片的宽、高
      BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
      tmpOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(inputStream, null, tmpOptions);
      int width = tmpOptions.outWidth;
      int height = tmpOptions.outHeight;

      int perfectHeight = height * totalWidth / width;

      Log.e("chenhan", "width=" + width + ";height=" + height);

      Log.e("chenhan", "totalWidth=" + totalWidth + ";totalHeight=" + totalHeight);

      Log.e("chenhan", "perfectHeight=" + perfectHeight);

      // 设置显示图片的中心区域:其中left是矩形左侧的X坐标，top是矩形顶部的Y坐标，right是矩形右侧的X坐标，bottom是矩形底部的Y坐标
      BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inPreferredConfig = Bitmap.Config.RGB_565;
      Bitmap bitmap =
          bitmapRegionDecoder.decodeRegion(
              new Rect(
                  0, height / 2 - perfectHeight / 2, totalWidth, height / 2 + perfectHeight / 2),
              options);
      mImg.setImageBitmap(bitmap);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
