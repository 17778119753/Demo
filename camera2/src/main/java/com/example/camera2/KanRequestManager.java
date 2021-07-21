package com.example.camera2;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;

public class KanRequestManager {

  private static final String TAG = KanRequestManager.class.getSimpleName();
  private Context context;
  private CameraCharacteristics mCharacteristics;

  public KanRequestManager(Context context) {
    this.context = context;
  }

  public void setCharacteristics(CameraCharacteristics characteristics) {
    mCharacteristics = characteristics;
  }

  public CaptureRequest getCapturePictureRequest(CaptureRequest.Builder builder, int rotation) {
    builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
    return builder.build();
  }

  public CaptureRequest getPreviewRequest(CaptureRequest.Builder builder) {
    int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
    int antiBMode = getValidAntiBandingMode(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
    builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
    builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBMode);
    builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
    builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    return builder.build();
  }

  /* ------------------------- private function------------------------- */
  private int getValidAFMode(int targetMode) {
    int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
    for (int mode : allAFMode) {
      if (mode == targetMode) {
        return targetMode;
      }
    }
    Log.i(TAG, "not support af mode:" + targetMode + " use mode:" + allAFMode[0]);
    return allAFMode[0];
  }

  private int getValidAntiBandingMode(int targetMode) {
    int[] allABMode =
        mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
    for (int mode : allABMode) {
      if (mode == targetMode) {
        return targetMode;
      }
    }
    Log.i(TAG, "not support anti banding mode:" + targetMode + " use mode:" + allABMode[0]);
    return allABMode[0];
  }
}
