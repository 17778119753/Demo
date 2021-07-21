package com.example.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraDevice;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.camera2.FeedKanRecordButton.RecordButtonListener;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements TextureView.SurfaceTextureListener, KanCamera2CallBack {
  private static final String TAG = MainActivity.class.getSimpleName();

  private Handler mMainHandler;
  private JobExecutor mJobExecutor;
  private KanDeviceManager deviceManager;
  private Point mDisplaySize;
  private int mVirtualKeyHeight;
  private GestureTextureView texturePreview;
  private RelativeLayout reContent;
  private FeedKanRecordButton recordBt;

  private Chronometer recordTime;
  private LinearLayout llRecordTimer;

  FileSaver mFileSaver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    XXPermissions.with(this)
        .permission(
            Permission.CAMERA,
            Permission.RECORD_AUDIO,
            Permission.READ_EXTERNAL_STORAGE,
            Permission.WRITE_EXTERNAL_STORAGE)
        .request(
            new OnPermission() {

              @Override
              public void hasPermission(List<String> granted, boolean isAll) {
                setContentView(R.layout.activity_main);
                initView();
              }

              @Override
              public void noPermission(List<String> denied, boolean quick) {
                MainActivity.this.finish();
              }
            });
  }

  public void initView() {
    llRecordTimer = (LinearLayout) findViewById(R.id.ll_record_timer);
    recordTime = (Chronometer) findViewById(R.id.record_time);
    recordBt = (FeedKanRecordButton) findViewById(R.id.recordBt);
    texturePreview = findViewById(R.id.texture_preview);
    texturePreview.setSurfaceTextureListener(this);
    reContent = findViewById(R.id.re_content);
    mJobExecutor = new JobExecutor();
    mMainHandler = new Handler(Looper.getMainLooper());
    deviceManager = new KanDeviceManager(MainActivity.this, mJobExecutor, MainActivity.this);
    sessionManager = new KanSessionManager(MainActivity.this, mMainHandler, MainActivity.this);
    mFileSaver = new FileSaver(this, this, mMainHandler);
    mDisplaySize = CameraUtil.getDisplaySize(MainActivity.this);
    Log.e("chenhan", "mDisplaySize:width=" + mDisplaySize.x + ";height=" + mDisplaySize.y);
    mVirtualKeyHeight = CameraUtil.getVirtualKeyHeight(MainActivity.this);
    setOrientationListener();
    recordBt.setRecordButtonListener(
        new RecordButtonListener() {
          @Override
          public void onClick() {
            sessionManager.applyRequest(Session.RQ_TAKE_PICTURE, mRotation);
          }

          @Override
          public void onLongClick() {
            sessionManager.applyRequest(Session.RQ_START_RECORD, mRotation);
            //            mJobExecutor.execute(
            //                new JobExecutor.Task<Void>() {
            //                  @Override
            //                  public Void run() {
            //                    sessionManager.applyRequest(Session.RQ_START_RECORD, mRotation);
            //                    return super.run();
            //                  }
            //                });
          }

          @Override
          public void onLongClickFinish(int result) {
            llRecordTimer.setVisibility(View.GONE);
            recordTime.stop();
            recordTime.setBase(SystemClock.elapsedRealtime());

            sessionManager.applyRequest(Session.RQ_STOP_RECORD);
            sessionManager.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRotation);
            if (result == FeedKanRecordButton.RECORD_SHORT) {
              ToastUtil.showShort(MainActivity.this, "录制时间过短");
            }
          }
        });
  }

  private String userCameraId;

  private void switchCamera() {
    int currentId = Integer.parseInt(deviceManager.getCameraId());
    int cameraCount = deviceManager.getCameraIdList().length;
    currentId++;
    if (cameraCount < 2) {
      return;
    } else if (currentId >= cameraCount) {
      currentId = 0;
    }
    String switchId = String.valueOf(currentId);
    deviceManager.setCameraId(switchId);
    userCameraId = switchId;

    camera = null;
    sessionManager.release();
    deviceManager.releaseCamera();

    String cameraId = userCameraId == null ? deviceManager.getCameraIdList()[0] : userCameraId;
    deviceManager.setCameraId(cameraId);
    deviceManager.openCamera(mMainHandler);
  }

  public void switchCamera(View view) {
    switchCamera();
  }

  @Override
  protected void onResume() {
    super.onResume();
    String cameraId = userCameraId == null ? deviceManager.getCameraIdList()[0] : userCameraId;
    deviceManager.setCameraId(cameraId);
    deviceManager.openCamera(mMainHandler);
  }

  @Override
  protected void onPause() {
    super.onPause();
    //    if (stateEnabled(Controller.CAMERA_STATE_START_RECORD)) {
    //      stopVideoRecording();
    //    }
    camera = null;
    sessionManager.release();
    deviceManager.releaseCamera();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mFileSaver != null) {
      mFileSaver.release();
    }
    mOrientationListener.disable();
    mJobExecutor.destroy();
    recordBt.release();
  }

  private SurfaceTexture mSurfaceTexture;
  private KanSessionManager sessionManager;
  private CameraDevice camera;

  @Override
  public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
    Log.e("chenhan", "onSurfaceTextureAvailable，SurfaceTexture创建好了");
    mSurfaceTexture = surface;
    // todo:接下来最好执行打开相机获取示例操作，因为直接放在resume中，有没有可能resume先执行了，然后才执行到该回调
    // 申请预览需要获取Camera Device
    // sessionManager.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRequestCallback);
    if (camera != null)
      sessionManager.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRotation);
  }

  @Override
  public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
    Log.e("chenhan", "onSurfaceTextureSizeChanged:width=" + width + ";height=" + height);
  }

  @Override
  public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}

  /** =============================以下是Camera2的自定义的回调================================= */
  @Override
  public void onOpened(@NonNull CameraDevice camera) {
    this.camera = camera;
    Log.e("chenhan", "onOpened，开始使用SurfaceTexture");
    // 设置相机对象，获取相机特征点
    sessionManager.applyRequest(Session.RQ_SET_DEVICE, camera);
    // 申请打开预览对象
    if (mSurfaceTexture != null)
      sessionManager.applyRequest(Session.RQ_START_PREVIEW, mSurfaceTexture, mRotation);
  }

  @Override
  public void onDisconnected(@NonNull CameraDevice camera) {}

  @Override
  public void onError(@NonNull CameraDevice camera, int error) {}

  @Override
  public void onViewChange(int widths, int height) {
    runOnUiThread(
        () -> {
          int width = (widths * mDisplaySize.y) / height;
          Log.e(
              "chenhan",
              "reContent:width=" + reContent.getWidth() + ";height=" + reContent.getHeight());
          Log.e("chenhan", "onViewChange:width=" + width + ";height=" + height);
          ConstraintLayout.LayoutParams previewParams =
              new ConstraintLayout.LayoutParams(width, mDisplaySize.y);
          reContent.setLayoutParams(previewParams);

          //            ConstraintLayout.LayoutParams previewParams =
          //                new ConstraintLayout.LayoutParams(widths, height);
          //            reContent.setLayoutParams(previewParams);

        });
  }

  @Override
  public void onDataBack(byte[] data, int width, int height) {
    Log.e("chenhan", "width=" + width + ";height=" + height);
    mJobExecutor.execute(
        new JobExecutor.Task<Void>() {
          @Override
          public Void run() {

            mFileSaver.saveFile(
                width, height, mRotation, data, "CAMERA", MediaFunc.MEDIA_TYPE_IMAGE);
            return super.run();
          }
        });
    // sessionManager.applyRequest(Session.RQ_RESTART_PREVIEW);
  }

  @Override
  public void onFileSaved(Uri uri, String path, @Nullable Bitmap thumbnail) {
    Log.e("chenhan", "保存图片成功：uri=" + uri + ";path=" + path);
    ImageActivity.args(
        MainActivity.this, path, texturePreview.getWidth(), texturePreview.getHeight());
  }

  @Override
  public void onFileSaveError(String msg) {
    Log.e("chenhan", "保存图片失败：msg=" + msg);
  }

  @Override
  public void onVideoSaved(Uri uri, String path) {
    Log.e("chenhan", "视频保存成功：uri=" + uri + ";path=" + path);
  }

  @Override
  public void onRecordStarted(boolean status) {
    Log.e("chenhan", "onRecordStarted:" + (status ? "开始录制" : "mediacorder失败"));
    llRecordTimer.setVisibility(View.VISIBLE);
    recordTime.setBase(SystemClock.elapsedRealtime());
    recordTime.start();
  }

  @Override
  public void onRecordStopped(String filePath, int width, int height) {
    mJobExecutor.execute(
        new JobExecutor.Task<Bitmap>() {
          @Override
          public Bitmap run() {
            Log.e("chenhan", "onRecordStopped:run");
            return ThumbnailUtils.createVideoThumbnail(
                filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
          }

          @Override
          public void onJobThread(Bitmap result) {
            Log.e("chenhan", "onRecordStopped:onJobThread");
            mFileSaver.saveVideoFile(
                width, height, mRotation, filePath, MediaFunc.MEDIA_TYPE_VIDEO);
          }

          @Override
          public void onMainThread(Bitmap result) {
            Log.e("chenhan", "onRecordStopped:onMainThread");
          }
        });
  }

  private MyOrientationListener mOrientationListener;
  private int mRotation = 0;

  private class MyOrientationListener extends OrientationEventListener {

    MyOrientationListener(Context context, int rate) {
      super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
      mRotation = (orientation + 45) / 90 * 90;
    }
  }

  private void setOrientationListener() {
    mOrientationListener =
        new MyOrientationListener(MainActivity.this, SensorManager.SENSOR_DELAY_UI);
    if (mOrientationListener.canDetectOrientation()) {
      mOrientationListener.enable();
    } else {
      mOrientationListener.disable();
    }
  }
}
