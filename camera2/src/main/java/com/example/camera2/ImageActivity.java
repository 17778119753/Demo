package com.example.camera2;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;

public class ImageActivity extends AppCompatActivity {
  private ImageView image;

  private String path;
  String width, height;

  public static void args(Context context, String path, int width, int height) {
    Intent intent = new Intent(context, ImageActivity.class);
    intent.putExtra("path", path);
    intent.putExtra("width", String.valueOf(width));
    intent.putExtra("height", String.valueOf(height));
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image);
    image = (ImageView) findViewById(R.id.image);
    path = getIntent().getStringExtra("path");
    width = getIntent().getStringExtra("width");
    height = getIntent().getStringExtra("height");

    ConstraintLayout.LayoutParams params =
        new LayoutParams(Integer.parseInt(width), Integer.parseInt(height));
    image.setLayoutParams(params);
    image.setImageBitmap(BitmapFactory.decodeFile(path));
  }
}
