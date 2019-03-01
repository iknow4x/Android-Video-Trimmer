package com.iknow.android.features.camera.record;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import com.iknow.android.R;
import com.iknow.android.databinding.ActivityRecordingLayoutBinding;
import com.iknow.android.features.common.ui.BaseActivity;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/22 4:24 PM
 * version: 1.0
 * description:
 */
public class VideoRecordActivity extends BaseActivity {

  private ActivityRecordingLayoutBinding mBinding;

  public static void call(Context context) {
    context.startActivity(new Intent(context, VideoRecordActivity.class));
  }

  @Override public void initUI() {
    super.initUI();
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_recording_layout);
  }
}
