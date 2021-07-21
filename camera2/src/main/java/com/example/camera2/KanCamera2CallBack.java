package com.example.camera2;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface KanCamera2CallBack {

  /** 以下三条是相机打开的回调 */
  void onOpened(@NonNull CameraDevice camera);

  void onDisconnected(@NonNull CameraDevice camera);

  void onError(@NonNull CameraDevice camera, int error);

  /** 以下n条是View变换的回调 */
  void onViewChange(int width, int height);

  /** 以下拍照数据回调 */
  void onDataBack(byte[] data, int width, int height);

  /** 以下拍照保存回调 */
  void onFileSaved(Uri uri, String path, @Nullable Bitmap thumbnail);

  void onFileSaveError(String msg);

  void onVideoSaved(Uri uri, String path);

  /** 以下视频录制状态的回调 */
  void onRecordStarted(boolean status);

  void onRecordStopped(String filePath, int width, int height);
}
