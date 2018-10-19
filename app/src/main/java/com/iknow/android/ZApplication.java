package com.iknow.android;

import android.app.Application;
import android.content.Context;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import iknow.android.utils.BaseUtils;

/**
 * Author：J.Chou
 * Date：  2016.09.27 10:44 AM
 * Email： who_know_me@163.com
 * Describe:
 */
public class ZApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();
    BaseUtils.init(this);
    initFFmpegBinary(this);
  }

  private void initFFmpegBinary(Context context) {

    try {
      FFmpeg.getInstance(context).loadBinary(new LoadBinaryResponseHandler() {
        @Override public void onFailure() {
        }
      });
    } catch (FFmpegNotSupportedException e) {
      e.printStackTrace();
    }
  }
}
