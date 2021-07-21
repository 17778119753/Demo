package com.example.textureviewdemo;

import static android.hardware.Camera.getCameraInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
  private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
  private Camera mCamera;
  TextureView textureView;
  FrameLayout preview;
  // 当前camera的Id
  private int mCameraID;
  private static final String TAG = "ERROR";
  private int mPreviewWidth, mPreviewHeight;
  private int mPictureWidth, mPictureHeight;
  private PictureCallback mPicture =
      new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
          Log.e("chenhan", "onPictureTaken");
          File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
          if (pictureFile == null) {
            Log.d(
                "chenhan",
                "Error creating media file, check storage permissions: " + "e.getMessage()");
            return;
          }
          try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            mCamera.stopPreview();
            mCamera.startPreview();
            mCamera.startFaceDetection();
          } catch (FileNotFoundException e) {
            Log.d("chenhan", "File not found: " + e.getMessage());
          } catch (IOException e) {
            Log.d("chenhan", "Error accessing file: " + e.getMessage());
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    XXPermissions.with(this)
        .permission(
            Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
        .request(
            new OnPermission() {

              @Override
              public void hasPermission(List<String> granted, boolean isAll) {
                Log.d("chenhan", "授权成功");
              }

              @Override
              public void noPermission(List<String> denied, boolean quick) {
                Log.d("chenhan", "授权失败");
              }
            });
    mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    // 创建Camera实例
    mCamera = getCameraInstance(mCameraID);
    preview = (FrameLayout) findViewById(R.id.camera_preview);
    // 创建Preview view并将其设为activity中的内容
    textureView = new TextureView(this);
    textureView.setSurfaceTextureListener(
        new SurfaceTextureListener() {
          @Override
          public void onSurfaceTextureAvailable(
              @NonNull SurfaceTexture surface, int width, int height) {
            // Log.e("chenhan", "onSurfaceTextureAvailable");
            try {
              // 设置预览角度，解决预览画面倾斜的问题
              mCamera.setDisplayOrientation(
                  getCameraPreviewOrientation(MainActivity.this, mCameraID));
              mCamera.setPreviewTexture(surface);
              Camera.Parameters parameters = mCamera.getParameters();
              // 预览和图片大小
              Camera.Size bestPreviewSize =
                  getOptimalSize(parameters.getSupportedPreviewSizes(), width, height);
              mPreviewWidth = bestPreviewSize.width;
              mPreviewHeight = bestPreviewSize.height;
              Log.e(
                  "chenhan",
                  "mPreviewWidth=" + mPreviewWidth + ";mPreviewHeight=" + mPreviewHeight);
              parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
              Camera.Size bestPictureSize =
                  getOptimalSize(parameters.getSupportedPictureSizes(), width, height);
              mPictureWidth = bestPictureSize.width;
              mPictureHeight = bestPictureSize.height;
              parameters.setPictureSize(mPictureWidth, mPictureHeight);
              // 自动聚焦
              parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
              mCamera.setParameters(parameters);
              mCamera.startPreview();
              mCamera.autoFocus(
                  new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                      // Log.e("chenhan", "onAutoFocus:success=" + success);
                      camera.cancelAutoFocus();
                    }
                  });
              mCamera.setPreviewCallback(
                  new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                      // YUV裸数据
                      // Log.e("chenhan", "onPreviewFrame:data=" + data.length);
                    }
                  });

            } catch (IOException ioe) {
              // Something bad happened
            }
          }

          @Override
          public void onSurfaceTextureSizeChanged(
              @NonNull SurfaceTexture surface, int width, int height) {
            // Log.e("chenhan", "onSurfaceTextureSizeChanged");
          }

          @Override
          public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            // Log.e("chenhan", "onSurfaceTextureDestroyed");
            if (mCamera != null) {
              mCamera.stopPreview();
              mCamera.release();
            }
            return true;
          }

          @Override
          public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            // Log.e("chenhan", "onSurfaceTextureUpdated");
          }
        });
    textureView.setAlpha(1f);
    preview.addView(textureView);
  }

  public void startPreview() {

    if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
      mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    } else {
      mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    }
    mCamera = getCameraInstance(mCameraID);
    try {
      mCamera.setPreviewTexture(textureView.getSurfaceTexture());
      Camera.Parameters parameters = mCamera.getParameters();
      // 预览和图片大小
      Log.e("chenhan", "mPreviewWidth=" + mPreviewWidth + ";mPreviewHeight=" + mPreviewHeight);
      // parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
      // parameters.setPictureSize(mPictureWidth, mPictureHeight);
      // 自动聚焦
      parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
      // mCamera.setParameters(parameters);
      mCamera.setDisplayOrientation(getCameraPreviewOrientation(MainActivity.this, mCameraID));
    } catch (IOException e) {
      e.printStackTrace();
    }

    mCamera.startPreview();
    mCamera.autoFocus(
        new Camera.AutoFocusCallback() {
          @Override
          public void onAutoFocus(boolean success, Camera camera) {
            Log.e("chenhan", "onAutoFocus:success=" + success);
            camera.cancelAutoFocus();
          }
        });
  }

  public void click(View view) {
    switch (view.getId()) {
      case R.id.button_capture:
        mCamera.takePicture(null, null, mPicture);
        break;
      case R.id.button_switch:
        if (mCamera != null) {
          mCamera.setPreviewCallback(null);
          mCamera.stopPreview();
          mCamera.release();

          startPreview();
        }
        break;
    }
  }

  /** 获取合适的分辨率大小 */
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

  /** 获取画面旋转角度 */
  public static int getCameraPreviewOrientation(Activity activity, int cameraId) {
    Camera.CameraInfo info = new Camera.CameraInfo();
    getCameraInfo(cameraId, info);
    int result;
    int degrees = getRotation(activity);
    // 前置
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;
    }
    // 后置
    else {
      result = (info.orientation - degrees + 360) % 360;
    }
    return result;
  }

  public static int getRotation(Activity activity) {
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }
    return degrees;
  }

  /** 安全获取Camera对象实例的方法 */
  public static Camera getCameraInstance(int mCameraID) {
    int numberOfCameras = Camera.getNumberOfCameras();
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    for (int i = 0; i < numberOfCameras; i++) {
      getCameraInfo(i, cameraInfo);
      if (cameraInfo.facing == mCameraID) {
        return Camera.open(i);
      }
    }
    return null; // 不可用则返回null
  }

  /** 为保存图片或视频创建File */
  private static File getOutputMediaFile(int type) {
    // 安全起见，在使用前应该
    // 用Environment.getExternalStorageState()检查SD卡是否已装入
    File mediaStorageDir =
        new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp");
    // 如果期望图片在应用程序卸载后还存在、且能被其它应用程序共享，
    // 则此保存位置最合适
    // 如果不存在的话，则创建存储目录
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("MyCameraApp", "failed to create directory");
        return null;
      }
      Log.d("MyCameraApp", "failed to create directory");
    }
    // 创建媒体文件名
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE) {
      mediaFile =
          new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    } else if (type == MEDIA_TYPE_VIDEO) {
      mediaFile =
          new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
    } else {
      return null;
    }
    return mediaFile;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        // 捕获的图像保存到Intent指定的fileUri
        Toast.makeText(this, "Image saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
      } else if (resultCode == RESULT_CANCELED) {
        // 用户取消了图像捕获
      } else {
        // 图像捕获失败，提示用户
      }
    }

    if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        // 捕获的视频保存到Intent指定的fileUri
        Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
      } else if (resultCode == RESULT_CANCELED) {
        // 用户取消了视频捕获
      } else {
        // 视频捕获失败，提示用户
      }
    }
  }

  @Override
  public void onBackPressed() {
    Log.e("chenhan", "返回键onBackPressed()触发");
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera(); // 在暂停事件中立即释放摄像头
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release(); // 为其它应用释放摄像头
      mCamera = null;
    }
  }
}
