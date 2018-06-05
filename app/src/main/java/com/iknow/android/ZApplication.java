package com.iknow.android;

import android.app.Application;
import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

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
    initImageLoader(this);
    initFFmpegBinary(this);
  }

  public static void initImageLoader(Context context) {
    int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 10);
    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).memoryCache(new LRULimitedMemoryCache(memoryCacheSize))
        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
        .tasksProcessingOrder(QueueProcessingType.LIFO)
        .build();
    // Initialize ImageLoader with configuration.
    ImageLoader.getInstance().init(config);
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
