package com.iknow.android.features.record.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/15 3:01 PM
 * version: 1.0
 * description:
 */

@SuppressLint("ViewConstructor")
public class PreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
  private static final String TAG = "PreviewSurfaceView";

  private Camera mCamera;
  private SurfaceHolder mHolder;
  private Context mContext;
  private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
  private int displayDegree = 90;

  public PreviewSurfaceView(Context context) {
    super(context);
    mContext = context;
    init();
  }

  public PreviewSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    init();
  }

  private void init() {
    mCamera = Camera.open(cameraId);
    mHolder = getHolder();
    mHolder.addCallback(this);
  }

  public void startPreview() {
    mCamera.startPreview();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    try {
      startCamera(holder);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (mHolder.getSurface() == null) {
      return;
    }
    try {
      mCamera.stopPreview();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      startCamera(mHolder);
    } catch (Exception e) {
      Log.e(TAG, e.toString());
    }
  }

  private void startCamera(SurfaceHolder holder) throws IOException {
    mCamera.setPreviewDisplay(holder);
    setCameraDisplayOrientation((Activity) mContext, cameraId, mCamera);

    Camera.Size preSize = getCameraSize();

    Camera.Parameters parameters = mCamera.getParameters();
    parameters.setPreviewSize(preSize.width, preSize.height);
    parameters.setPictureSize(preSize.width, preSize.height);
    parameters.setJpegQuality(100);
    try {
      mCamera.setParameters(parameters);
    } catch (Exception e) {
      try {
        parameters.setPictureSize(1920, 1080);
        mCamera.setParameters(parameters);
      } catch (Exception ignored) {
      }
    }
    mCamera.startPreview();
  }

  public Camera.Size getCameraSize() {
    if (null != mCamera) {
      Camera.Parameters parameters = mCamera.getParameters();
      DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
      Camera.Size preSize = getCloselyPreSize(true, metrics.widthPixels, metrics.heightPixels, parameters.getSupportedPreviewSizes());
      return preSize;
    }
    return null;
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    release();
  }

  /**
   * Android API: Display Orientation Setting
   * Just change screen display orientation,
   * the rawFrame data never be changed.
   */
  private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
    Camera.CameraInfo info = new Camera.CameraInfo();
    Camera.getCameraInfo(cameraId, info);
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
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      displayDegree = (info.orientation + degrees) % 360;
      displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
    } else {
      displayDegree = (info.orientation - degrees + 360) % 360;
    }
    camera.setDisplayOrientation(displayDegree);
  }

  public synchronized void release() {
    try {
      if (null != mCamera) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
      }
      if (null != mHolder) {
        mHolder.removeCallback(this);
        mHolder = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
    int reqTmpWidth;
    int reqTmpHeight;
    // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
    if (isPortrait) {
      reqTmpWidth = surfaceHeight;
      reqTmpHeight = surfaceWidth;
    } else {
      reqTmpWidth = surfaceWidth;
      reqTmpHeight = surfaceHeight;
    }
    //先查找preview中是否存在与surfaceview相同宽高的尺寸
    for (Camera.Size size : preSizeList) {
      if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
        return size;
      }
    }
    // 得到与传入的宽高比最接近的size
    float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
    float curRatio, deltaRatio;
    float deltaRatioMin = Float.MAX_VALUE;
    Camera.Size retSize = null;
    for (Camera.Size size : preSizeList) {
      curRatio = ((float) size.width) / size.height;
      deltaRatio = Math.abs(reqRatio - curRatio);
      if (deltaRatio < deltaRatioMin) {
        deltaRatioMin = deltaRatio;
        retSize = size;
      }
    }
    return retSize;
  }
}