package com.iknow.android.features.select;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import com.iknow.android.models.VideoInfo;
import iknow.android.utils.callback.SimpleCallback;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/10/04 1:50 PM
 * version: 1.0
 * description:
 */
public class VideoRxJavaLoader implements ILoader {

  @SuppressLint("CheckResult")
  @Override public void load(final Context mContext, final SimpleCallback listener) {
    Observable.create((ObservableOnSubscribe<List<VideoInfo>>) emitter -> {
      List<VideoInfo> videos = new ArrayList<>();
      try {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Video.Media.DATE_MODIFIED + " desc");
        if (cursor != null) {
          while (cursor.moveToNext()) {
            VideoInfo videoInfo = new VideoInfo();
            if (cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)) != 0) {
              videoInfo.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
              videoInfo.setVideoPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
              videoInfo.setCreateTime(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
              videoInfo.setVideoName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
              videos.add(videoInfo);
            }
          }
          cursor.close();
        }
        emitter.onNext(videos);
      } catch (Throwable t) {
        emitter.onError(t);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(videoInfos -> {
      if (listener != null) listener.success(videoInfos);
    }, throwable -> Log.e("jason", throwable.getMessage()));
  }
}
