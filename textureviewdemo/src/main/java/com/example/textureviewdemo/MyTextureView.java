package com.example.textureviewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.TextureView;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressLint("NewApi")
public class MyTextureView extends TextureView implements TextureView.SurfaceTextureListener {
  private Camera mCamera;
  private TextureView mTextureView;

  public MyTextureView(Context context, Camera camera) {
    super(context);
    mCamera = camera;
    // TODO Auto-generated constructor stub
  }

  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    //        mCamera = Camera.open();
    try {
      mCamera.setPreviewTexture(surface);
      Camera.Parameters parameters = mCamera.getParameters();
      Camera.Size bestPreviewSize =
          getOptimalSize(parameters.getSupportedPreviewSizes(), width, height);
      parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
      Camera.Size bestPictureSize =
          getOptimalSize(parameters.getSupportedPictureSizes(), width, height);
      parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
      // 自动聚焦
      parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
      mCamera.setParameters(parameters);
      mCamera.setPreviewCallback(
          new PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
              // YUV裸数据
              Log.e("chenhan", "onPreviewFrame:data=" + data.length);
            }
          });
      mCamera.startPreview();
      mCamera.autoFocus(
          new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
              Log.e("chenhan", "onAutoFocus:success=" + success);
              camera.cancelAutoFocus();
            }
          });

    } catch (IOException ioe) {
      // Something bad happened
    }
  }

  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    // Ignored, Camera does all the work for us
    // focus(width / 2, height / 2, true); // 自动对焦
  }

  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    mCamera.stopPreview();
    mCamera.release();
    return true;
  }

  public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    // Invoked every time there's a new Camera preview frame

  }

  private boolean focus(Camera.AutoFocusCallback callback) {

    if (mCamera == null) {
      return false;
    }
    mCamera.cancelAutoFocus();
    mCamera.autoFocus(callback);
    return true;
  }

  private static Camera.Size getOptimalSize(List<Size> supportList, int width, int height) {
    // camera的宽度是大于高度的，这里要保证expectWidth > expectHeight
    int expectWidth = Math.max(width, height);
    int expectHeight = Math.min(width, height);
    // 根据宽度排序（这里的宽度就是最长的那一边）
    Collections.sort(
        supportList,
        new Comparator<Size>() {
          @Override
          public int compare(Camera.Size pre, Camera.Size after) {
            if (pre.width > after.width) {
              return 1;
            } else if (pre.width < after.width) {
              return -1;
            }
            return 0;
          }
        });

    Camera.Size result = supportList.get(0);
    boolean widthOrHeight = false; // 判断存在宽或高相等的Size
    for (Camera.Size size : supportList) {
      // 如果宽高相等，则直接返回
      if (size.width == expectWidth && size.height == expectHeight) {
        result = size;
        break;
      }
      // 仅仅是宽度相等，计算高度最接近的size
      if (size.width == expectWidth) {
        widthOrHeight = true;
        if (Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight)) {
          result = size;
        }
      }
      // 高度相等，则计算宽度最接近的Size
      else if (size.height == expectHeight) {
        widthOrHeight = true;
        if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth)) {
          result = size;
        }
      }
      // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
      else if (!widthOrHeight) {
        if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth)
            && Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight)) {
          result = size;
        }
      }
    }
    return result;
  }
}
