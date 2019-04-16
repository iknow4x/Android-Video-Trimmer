package com.iknow.android.features.record;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.iknow.android.R;
import com.iknow.android.features.common.ui.BaseActivity;
import com.iknow.android.features.record.view.PreviewSurfaceView;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/22 4:24 PM
 * version: 1.0
 * description:
 */
public class VideoRecordActivity extends BaseActivity{
  @BindView(R.id.glView) PreviewSurfaceView mGLView;
  @BindView(R.id.ivRecord) ImageView mIvRecordBtn;
  @BindView(R.id.ivSwitch) ImageView mIvSwitchCameraBtn;

  @OnClick(R.id.ivRecord) void onRecord() {
    mGLView.startRecord();
  }

  @OnClick(R.id.ivSwitch) void onSwitchCamera() {

  }

  public static void call(Context context) {
    context.startActivity(new Intent(context, VideoRecordActivity.class));
  }

  @Override public void initUI() {
    setContentView(R.layout.activity_video_recording);
    ButterKnife.bind(this);
    mGLView.startPreview();
  }
}
