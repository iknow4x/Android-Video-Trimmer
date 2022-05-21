package com.iknow.android.features.record;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.iknow.android.R;
import com.iknow.android.features.common.ui.BaseActivity;
import com.iknow.android.features.record.view.PreviewSurfaceView;
import com.iknow.android.utils.ToastUtil;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/22 4:24 PM
 * version: 1.0
 * description:
 */
public class VideoRecordActivity extends BaseActivity implements View.OnClickListener {
  private PreviewSurfaceView mGLView;
  private ImageView mIvRecordBtn;
  private ImageView mIvSwitchCameraBtn;
  private ViewGroup mCameraSurfaceViewLy;

  public static void call(Context context) {
    context.startActivity(new Intent(context, VideoRecordActivity.class));
  }

  @Override public void initUI() {
    setContentView(R.layout.activity_video_recording);
    mIvRecordBtn = this.findViewById(R.id.ivRecord);
    mIvSwitchCameraBtn = this.findViewById(R.id.ivSwitch);
    ImageView ivBack = this.findViewById(R.id.iv_back);
    mIvRecordBtn.setOnClickListener(this);
    mIvSwitchCameraBtn.setOnClickListener(this);
    ivBack.setOnClickListener(this);

    mCameraSurfaceViewLy = findViewById(R.id.layout_surface_view);
    mGLView = new PreviewSurfaceView(this);
    mCameraSurfaceViewLy.addView(mGLView);
    mGLView.startPreview();
  }

  @Override public void onClick(View view) {
    if (R.id.ivRecord == view.getId()) {
      ToastUtil.longShow(this, "Features are under development, pls stay tuned...");
      //mGLView.startPreview();
    } else if (R.id.iv_back == view.getId()) {
      this.finish();
    }
  }

  @Override public void onBackPressed() {
    finish();
  }
}
