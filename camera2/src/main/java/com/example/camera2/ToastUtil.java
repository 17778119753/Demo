package com.example.camera2;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Title:ToastUtil
 *
 * <p>Description:Toast提示工具类 Author Han.C Date 2017/8/9 9:07
 */
public class ToastUtil {
  private static Toast mToast;

  public static void showShortBottom(Context context, String msg) {
    try {
      if (context != null && !TextUtils.isEmpty(msg)) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_normal, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(msg);
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showShort(Context context, String msg) {
    try {
      if (context != null && !TextUtils.isEmpty(msg)) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_normal, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(msg);
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showShort(Context context, int msg) {
    try {
      if (context != null && !TextUtils.isEmpty(context.getResources().getString(msg))) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_normal, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(context.getResources().getString(msg));
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showShortId(Context context, int msgId) {
    try {
      if (context != null && !TextUtils.isEmpty(context.getResources().getString(msgId))) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_normal, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(context.getResources().getString(msgId));
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showShortTextSize(Context context, String msg, int size) {
    try {
      if (context != null && !TextUtils.isEmpty(msg)) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_normal, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(msg);
        textView.setTextSize(size);
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showShortImg(Context context, String msg, int icon) {
    try {
      if (context != null && !TextUtils.isEmpty(msg)) {
        if (mToast != null) {
          mToast.cancel();
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_hint, null);
        TextView textView = view.findViewById(R.id.message);
        textView.setText(msg);
        ImageView imageView = view.findViewById(R.id.icon);
        imageView.setImageDrawable(context.getResources().getDrawable(icon));
        view.getBackground().setAlpha(188);
        mToast.setView(view);
        mToast.show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
