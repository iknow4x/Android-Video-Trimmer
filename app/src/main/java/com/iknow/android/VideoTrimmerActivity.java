package com.iknow.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import com.iknow.android.databinding.ActivityTrimmerLayoutBinding;
import com.iknow.android.interfaces.CompressVideoListener;
import com.iknow.android.interfaces.TrimVideoListener;
import com.iknow.android.utils.CompressVideoUtil;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoTrimmerActivity extends AppCompatActivity implements TrimVideoListener {

  private static final String TAG = "jason";
  private static final String VIDEO_PATH_KEY = "path";
  public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
  private ActivityTrimmerLayoutBinding binding;
  private ProgressDialog mProgressDialog;

  public static void call(FragmentActivity from, String videoPath) {
    if (!TextUtils.isEmpty(videoPath)) {
      Bundle bundle = new Bundle();
      bundle.putString(VIDEO_PATH_KEY, videoPath);
      Intent intent = new Intent(from, VideoTrimmerActivity.class);
      intent.putExtras(bundle);
      from.startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
    }
  }

  @Override protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer_layout);
    Bundle bd = getIntent().getExtras();
    String path = "";
    if (bd != null) path = bd.getString(VIDEO_PATH_KEY);
    if (binding.trimmerView != null) {
      binding.trimmerView.setOnTrimVideoListener(this);
      binding.trimmerView.initVideoByURI(Uri.parse(path));
    }
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onPause() {
    super.onPause();
    binding.trimmerView.onPause();
    binding.trimmerView.setRestoreState(true);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    binding.trimmerView.destroy();
  }

  @Override public void onStartTrim() {
    buildDialog(getResources().getString(R.string.trimming)).show();
  }

  @Override public void onFinishTrim(String in) {
    //TODO: please handle your trimmed video url here!!!
    String out = "/storage/emulated/0/Android/data/com.iknow.android/cache/compress.mp4";
    buildDialog(getResources().getString(R.string.compressing)).show();
    CompressVideoUtil.compress(this, in, out, new CompressVideoListener() {
      @Override public void onSuccess(String message) {
      }

      @Override public void onFailure(String message) {
      }

      @Override public void onFinish() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        finish();
      }
    });
  }

  @Override public void onCancel() {
    binding.trimmerView.destroy();
    finish();
  }

  private ProgressDialog buildDialog(String msg) {
    if (mProgressDialog == null) {
      mProgressDialog = ProgressDialog.show(this, "", msg);
    }
    mProgressDialog.setMessage(msg);
    return mProgressDialog;
  }
}
