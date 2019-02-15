package com.iknow.android.features.camera.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import iknow.android.utils.DeviceUtil;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/15 3:01 PM
 * version: 1.0
 * description:
 */
public class CameraPreviewLayout extends RelativeLayout {

  private Context mContext;
  private int circleWidth = 0;//指定半径
  private int borderWidth = 0;//指定边框

  public CameraPreviewLayout(Context context) {
    super(context);
    init(context, null, -1, -1);
  }

  public CameraPreviewLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, -1, -1);
  }

  public CameraPreviewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, -1);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    mContext = context;
  }

  public void show(CameraPreviewSurfaceView surfaceView) {
    circleWidth = DeviceUtil.getDeviceWidth();
    RelativeLayout cameraRoot = new RelativeLayout(mContext);
    RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, 1300);
    rootParams.addRule(CENTER_IN_PARENT, TRUE);
    cameraRoot.setClipChildren(false);

    FrameLayout cameraLayout = new FrameLayout(mContext);
    Camera.Size preSize = surfaceView.getCameraSize();
    int cameraHeight = (int) ((float) preSize.width / (float) preSize.height * circleWidth);
    RelativeLayout.LayoutParams cameraParams = new RelativeLayout.LayoutParams(circleWidth, cameraHeight);
    cameraParams.addRule(CENTER_IN_PARENT, TRUE);
    cameraLayout.setLayoutParams(cameraParams);
    cameraLayout.addView(surfaceView);
    CameraPreviewMaskView maskView = new CameraPreviewMaskView(mContext);

    int margin = (cameraHeight - circleWidth) / 2 - borderWidth / 2;
    rootParams.setMargins(0, -margin, 0, -margin);
    cameraRoot.setLayoutParams(rootParams);
    cameraRoot.addView(cameraLayout);
    cameraRoot.addView(maskView);
    this.addView(cameraRoot);
  }
}
