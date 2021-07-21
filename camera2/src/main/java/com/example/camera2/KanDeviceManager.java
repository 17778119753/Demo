package com.example.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Title:KanDeviceManager
 *
 * <p>Description:用于处理Camera2 Device，例如打开相机，获取相机ID示例等操作 Author Han.C Date 2021/6/25 2:55 PM
 */
public class KanDeviceManager {
  private final String TAG = KanDeviceManager.class.getSimpleName();

  CameraManager cameraManager;
  private JobExecutor mJobExecutor;
  private String mCameraId = Config.MAIN_ID;
  private KanCamera2CallBack callBack;
  private CameraDevice mDevice;

  public KanDeviceManager(Context context, JobExecutor executor, KanCamera2CallBack callBack) {
    this.callBack = callBack;
    mJobExecutor = executor;
    cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
  }

  public void setCameraId(@NonNull String id) {
    mCameraId = id;
  }

  public String getCameraId() {
    return mCameraId;
  }

  public void openCamera(final Handler mainHandler) {
    mJobExecutor.execute(
        new JobExecutor.Task<Void>() {
          @Override
          public Void run() {
            openDevice(mainHandler);
            return super.run();
          }
        });
  }

  @SuppressLint("MissingPermission")
  private synchronized void openDevice(Handler handler) {
    // no need to check permission, because we check permission in onStart() every time
    try {
      cameraManager.openCamera(mCameraId, stateCallback, handler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  public CameraCharacteristics getCharacteristics(String cameraId) {
    try {
      return cameraManager.getCameraCharacteristics(cameraId);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] getCameraIdList() {
    try {
      return cameraManager.getCameraIdList();
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void releaseCamera() {
    mJobExecutor.execute(
        new JobExecutor.Task<Void>() {
          @Override
          public Void run() {
            closeDevice();
            return super.run();
          }
        });
  }

  private synchronized void closeDevice() {
    if (mDevice != null) {
      mDevice.close();
      mDevice = null;
    }
  }

  public StreamConfigurationMap getConfigMap(String cameraId) {
    try {
      CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraId);
      return c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private CameraDevice.StateCallback stateCallback =
      new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
          Log.d(TAG, "device opened :" + camera.getId());
          mDevice = camera;
          callBack.onOpened(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
          Log.w(TAG, "onDisconnected");
          camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
          Log.e(TAG, "error occur when open camera :" + camera.getId() + " error code:" + error);
          camera.close();
        }
      };
}
