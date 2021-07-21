package com.example.camera2;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class KanSessionManager extends Session {

  private static final String TAG = KanSessionManager.class.getSimpleName();
  private SurfaceTexture surfaceTexture;
  private Surface mSurface;
  private CaptureRequest.Builder mPreviewBuilder;
  private KanRequestManager requestManager;
  private KanCamera2CallBack callBack;
  private Handler mainHandler;
  private Point mRealDisplaySize = new Point();

  public KanSessionManager(Context context, Handler mainHandler, KanCamera2CallBack callBack) {
    super(context);
    this.callBack = callBack;
    this.mainHandler = mainHandler;
    requestManager = new KanRequestManager(context);
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getRealSize(mRealDisplaySize);
  }

  private void createPreviewSession(@NonNull SurfaceTexture texture) {
    if (cameraSession != null) {
      cameraSession.close();
      cameraSession = null;
    }
    mCaptureBuilder = null;
    mPreviewBuilder = null;
    surfaceTexture = texture;
    mSurface = new Surface(surfaceTexture);
    try {
      cameraDevice.createCaptureSession(
          setPreviewOutputSize(surfaceTexture), sessionPreview, mainHandler);
    } catch (CameraAccessException | IllegalStateException e) {
      e.printStackTrace();
    }
  }

  private MediaRecorder mMediaRecorder;
  private File mCurrentRecordFile;

  private void initMediaRecorder(int deviceRotation) {
    if (mMediaRecorder == null) {
      mMediaRecorder = new MediaRecorder();
    }

    mCurrentRecordFile = MediaFunc.getOutputMediaFile(MediaFunc.MEDIA_TYPE_VIDEO, "VIDEO");
    if (mCurrentRecordFile == null) {
      Log.e(TAG, " get video file failed");
      return;
    }

    //    if (VERSION.SDK_INT >= VERSION_CODES.M) {
    //      mMediaRecorder.setInputSurface(mSurface);
    //    }

    mMediaRecorder.setVideoSource(VideoSource.SURFACE);

    mMediaRecorder.setOutputFile(mCurrentRecordFile.getPath());

    mMediaRecorder.setAudioSource(AudioSource.MIC);

    mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);

    mMediaRecorder.setVideoEncodingBitRate(10000000);
    mMediaRecorder.setVideoFrameRate(30);
    mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
    mMediaRecorder.setVideoEncoder(VideoEncoder.H264);
    mMediaRecorder.setAudioEncoder(AudioEncoder.AAC);
    int rotation = CameraUtil.getJpgRotation(characteristics, deviceRotation);
    mMediaRecorder.setOrientationHint(rotation);
    try {
      mMediaRecorder.prepare();
    } catch (IOException e) {
      Log.e(TAG, "error prepare video record:" + e.getMessage());
    }
  }

  private void StopMediaRecorder() {
    try {
      mMediaRecorder.stop();
      mMediaRecorder.reset();
      callBack.onRecordStopped(
          mCurrentRecordFile.getPath(), mVideoSize.getWidth(), mVideoSize.getHeight());
    } catch (Exception e) {
      mMediaRecorder.reset();
      if (mCurrentRecordFile.exists() && mCurrentRecordFile.delete()) {
        Log.w(TAG, "video file delete success");
      }
      Log.e(TAG, e.getMessage());
    }
  }

  private ImageReader mImageReader;
  private CaptureRequest.Builder mCaptureBuilder;

  private void sendCapturePictureRequest() {
    int jpegRotation = CameraUtil.getJpgRotation(characteristics, mDeviceRotation);
    CaptureRequest.Builder builder = getCaptureBuilder(mImageReader.getSurface());
    Integer aeFlash = getPreviewBuilder().get(CaptureRequest.CONTROL_AE_MODE);
    Integer afMode = getPreviewBuilder().get(CaptureRequest.CONTROL_AF_MODE);
    Integer flashMode = getPreviewBuilder().get(CaptureRequest.FLASH_MODE);
    builder.set(CaptureRequest.CONTROL_AE_MODE, aeFlash);
    builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
    builder.set(CaptureRequest.FLASH_MODE, flashMode);
    CaptureRequest request = requestManager.getCapturePictureRequest(builder, jpegRotation);
    sendCaptureRequestWithStop(request, mCaptureCallback, mainHandler);
  }

  private CaptureRequest.Builder getCaptureBuilder(Surface surface) {
    if (mCaptureBuilder == null) {
      mCaptureBuilder = createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
    }
    return mCaptureBuilder;
  }

  private int mDeviceRotation;

  @Override
  public void applyRequest(int msg, @Nullable Object value1, @Nullable Object value2) {
    switch (msg) {
      case RQ_SET_DEVICE:
        // 初始化相机设置
        cameraDevice = (CameraDevice) value1;
        initCharacteristics();
        requestManager.setCharacteristics(characteristics);
        mPreviewBuilder = null;
        mCaptureBuilder = null;
        break;
      case RQ_START_PREVIEW:
        mDeviceRotation = (Integer) value2;
        // 创建预览Session
        createPreviewSession((SurfaceTexture) value1);
        break;
      case RQ_TAKE_PICTURE:
        mDeviceRotation = (Integer) value1;
        sendCapturePictureRequest();
        break;
      case RQ_START_RECORD:
        try {
          mMediaRecorder.start();
          callBack.onRecordStarted(true);
        } catch (RuntimeException e) {
          callBack.onRecordStarted(false);
          Log.e(TAG, "start record failed msg:" + e.getMessage());
        }

        break;
      case RQ_STOP_RECORD:
        if (mMediaRecorder != null) {
          StopMediaRecorder();
        }
        break;
    }
  }

  @Override
  public void setRequest(int msg, @Nullable Object value1, @Nullable Object value2) {}

  private Size mVideoSize, mPreviewSize;
  // config picture size and preview size
  private List<Surface> setPreviewOutputSize(SurfaceTexture texture) {
    StreamConfigurationMap map =
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    // parameters key

    mVideoSize = CameraUtil.getDefaultVideoSize(map, mRealDisplaySize);
    double videoRatio = mVideoSize.getWidth() / (double) (mVideoSize.getHeight());
    mPreviewSize = CameraUtil.getPreviewSizeByRatio(map, mRealDisplaySize, videoRatio);
    texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
    int format = Integer.parseInt(Config.IMAGE_FORMAT);
    Size pictureSize = CameraUtil.getDefaultPictureSize(map, format);
    Surface surface = new Surface(texture);

    initMediaRecorder(mDeviceRotation);

    if (mImageReader != null) {
      mImageReader.close();
      mImageReader = null;
    }
    mImageReader =
        ImageReader.newInstance(pictureSize.getWidth(), pictureSize.getHeight(), format, 1);
    mImageReader.setOnImageAvailableListener(
        new ImageReader.OnImageAvailableListener() {
          @Override
          public void onImageAvailable(ImageReader reader) {
            Log.e("chenhan", "onImageAvailable");
            callBack.onDataBack(getByteFromReader(reader), reader.getWidth(), reader.getHeight());
          }
        },
        null);

    Size uiSize = CameraUtil.getPreviewUiSize(mContext, mPreviewSize);
    callBack.onViewChange(uiSize.getHeight(), uiSize.getWidth());
    // return Collections.singletonList(surface);
    return Arrays.asList(surface, mImageReader.getSurface(), mMediaRecorder.getSurface());
  }

  @Override
  public void release() {
    if (cameraSession != null) {
      cameraSession.close();
      cameraSession = null;
    }
    if (mImageReader != null) {
      mImageReader.close();
      mImageReader = null;
    }
    if (mMediaRecorder != null) {
      mMediaRecorder.release();
      mMediaRecorder = null;
    }
    if (mCurrentRecordFile.exists() && mCurrentRecordFile.delete()) {
      Log.w(TAG, "video file delete success");
    }
  }

  private void sendPreviewRequest() {
    CaptureRequest.Builder builder = getPreviewBuilder();
    CaptureRequest request = requestManager.getPreviewRequest(builder);
    sendRepeatingRequest(request, mPreviewCallback, mainHandler);
  }

  private CaptureRequest.Builder getPreviewBuilder() {
    // if is in video recording, request send to use VideoBuilder
    if (mPreviewBuilder == null) {
      mPreviewBuilder = createBuilder(CameraDevice.TEMPLATE_PREVIEW, mSurface);
    }
    // mPreviewBuilder.addTarget(mImageReader.getSurface());
    mPreviewBuilder.addTarget(mMediaRecorder.getSurface());
    return mPreviewBuilder;
  }

  // 请求预览的Seesion回调
  private CameraCaptureSession.StateCallback sessionPreview =
      new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
          Log.d(TAG, " session onConfigured id:" + session.getDevice().getId());
          cameraSession = session;
          // 发起预览请求
          sendPreviewRequest();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
          Log.d(TAG, "create session fail id:" + session.getDevice().getId());
        }
      };

  // 请求预览的回调:Camera2是一帧帧的回调
  private CameraCaptureSession.CaptureCallback mPreviewCallback =
      new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull CaptureResult partialResult) {
          super.onCaptureProgressed(session, request, partialResult);
          // Log.e("chenhan", "CameraCaptureSession.CaptureCallback:onCaptureProgressed");
          // Focus相关
          // updateAfState(partialResult);
        }

        @Override
        public void onCaptureCompleted(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {
          super.onCaptureCompleted(session, request, result);
          // Log.e("chenhan", "CameraCaptureSession.CaptureCallback:onCaptureCompleted");
          // Focus相关
          // updateAfState(result);
        }
      };

  private CameraCaptureSession.CaptureCallback mCaptureCallback =
      new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull CaptureResult partialResult) {
          super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(
            @NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {
          super.onCaptureCompleted(session, request, result);
          Log.i(TAG, "capture complete");
          resetTriggerState();
        }
      };

  private void resetTriggerState() {
    CaptureRequest.Builder builder = getPreviewBuilder();
    builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    builder.set(
        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
    sendRepeatingRequest(builder.build(), mPreviewCallback, mainHandler);
    sendCaptureRequest(builder.build(), mPreviewCallback, mainHandler);
  }
}
