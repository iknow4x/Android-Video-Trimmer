package com.iknow.android.features.select;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.view.View;
import com.iknow.android.R;
import com.iknow.android.databinding.ActivityVideoSelectBinding;
import com.iknow.android.features.record.VideoRecordActivity;
import com.iknow.android.features.record.view.CameraPreviewLayout;
import com.iknow.android.features.record.view.PreviewSurfaceView;
import com.iknow.android.features.common.ui.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import iknow.android.utils.callback.SimpleCallback;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VideoSelectActivity extends BaseActivity implements View.OnClickListener{

  private ActivityVideoSelectBinding mBinding;
  private VideoSelectAdapter mVideoSelectAdapter;
  private VideoLoadManager mVideoLoadManager;
  private PreviewSurfaceView mSurfaceView;
  private CameraPreviewLayout cameraPreviewLayout;

  @SuppressLint("CheckResult")
  @Override public void initUI() {
    mVideoLoadManager = new VideoLoadManager();
    mVideoLoadManager.setLoader(new VideoCursorLoader());
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_select);
    cameraPreviewLayout = findViewById(R.id.capturePreview);

    mBinding.mBtnBack.setOnClickListener(this);

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
      initCameraPreview();
    } else {
      mBinding.cameraPreviewLy.setVisibility(View.GONE);
      mBinding.openCameraPermissionLy.setVisibility(View.VISIBLE);
      mBinding.mOpenCameraPermission.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          rxPermissions.request(Manifest.permission.CAMERA).subscribe(granted -> {
            if (granted) {
              initCameraPreview();
            }
          });
        }
      });
    }
  }

  private void initCameraPreview() {
    mSurfaceView = new PreviewSurfaceView(this);
    mBinding.cameraPreviewLy.setVisibility(View.VISIBLE);
    mBinding.openCameraPermissionLy.setVisibility(View.GONE);
    cameraPreviewLayout.show(mSurfaceView);
    mSurfaceView.startPreview();
    mBinding.cameraPreviewLy.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        VideoRecordActivity.call(VideoSelectActivity.this);
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override public void onClick(View v) {
    if (v.getId() == mBinding.mBtnBack.getId()) {
      finish();
    }
  }
}
