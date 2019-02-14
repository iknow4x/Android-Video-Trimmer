package com.iknow.android.features.select;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import com.iknow.android.R;
import com.iknow.android.databinding.VideoSelectLayoutBinding;
import com.iknow.android.features.camera.CameraManager;
import com.iknow.android.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import iknow.android.utils.callback.SimpleCallback;
import java.io.IOException;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VideoSelectActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

  private VideoSelectLayoutBinding mBinding;
  private VideoSelectAdapter mVideoSelectAdapter;
  private VideoLoadManager mVideoLoadManager;
  private CameraManager cameraManager;
  private boolean isHasSurface = false;

  @SuppressLint("CheckResult")
  @Override protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    mVideoLoadManager = new VideoLoadManager();
    mVideoLoadManager.setLoader(new VideoCursorLoader());
    mBinding = DataBindingUtil.setContentView(this, R.layout.video_select_layout);
    mBinding.mBtnBack.setOnClickListener(this);
    mBinding.capturePreview.getHolder().addCallback(this);

    RxPermissions rxPermissions = new RxPermissions(this);
    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(granted -> {
      if (granted) { // Always true pre-M
        mVideoLoadManager.load(this, new SimpleCallback() {
          @Override public void success(Object obj) {
            if (mVideoSelectAdapter == null) {
              mVideoSelectAdapter = new VideoSelectAdapter(VideoSelectActivity.this, (Cursor) obj);
            } else {
              mVideoSelectAdapter.swapCursor((Cursor) obj);
            }
            if (mBinding.videoGridview.getAdapter() == null) {
              mBinding.videoGridview.setAdapter(mVideoSelectAdapter);
            }
            mVideoSelectAdapter.notifyDataSetChanged();
          }
        });
      } else {
        finish();
      }
    });
    if (rxPermissions.isGranted(Manifest.permission.CAMERA)) {
      initCameraManger();
    } else {
      mBinding.cameraPreviewLy.setVisibility(View.GONE);
      mBinding.openCameraPermissionLy.setVisibility(View.VISIBLE);
      mBinding.mOpenCameraPermission.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          rxPermissions.request(Manifest.permission.CAMERA).subscribe(granted -> {
            if (granted) {
              initCameraManger();
            }
          });
        }
      });
    }
  }

  private void initCameraManger() {
    mBinding.cameraPreviewLy.setVisibility(View.VISIBLE);
    mBinding.openCameraPermissionLy.setVisibility(View.GONE);
    cameraManager = new CameraManager(getApplication());
    mBinding.capturePreview.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ToastUtil.longShow(VideoSelectActivity.this, "start recoding...");
      }
    });
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    if (cameraManager == null || surfaceHolder == null) return;
    if (cameraManager.isOpen()) return;
    try {
      cameraManager.openDriver(surfaceHolder);
      cameraManager.startPreview();
    } catch (IOException | RuntimeException e) {
    }
  }

  @Override protected void onResume() {
    if (mBinding.capturePreview != null) {
      if (isHasSurface) {
        // The activity was paused but not stopped, so the surface still
        // exists. Therefore
        // surfaceCreated() won't be called, so init the camera here.
        initCamera(mBinding.capturePreview.getHolder());
      }
    }
    super.onResume();
  }

  @Override protected void onPause() {
    if (cameraManager != null) {
      cameraManager.closeDriver();
    }
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (mBinding.capturePreview != null) {
      mBinding.capturePreview.getHolder().removeCallback(this);
    }
    super.onDestroy();
  }

  @Override public void onClick(View v) {
    if (v.getId() == mBinding.mBtnBack.getId()) {
      finish();
    }
  }

  @Override public void surfaceCreated(SurfaceHolder holder) {
    isHasSurface = true;
    initCamera(holder);
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {
    isHasSurface = false;
  }
}
