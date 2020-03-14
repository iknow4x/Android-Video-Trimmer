package com.iknow.android.features.select;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import iknow.android.utils.callback.SimpleCallback;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
    Observable.create((ObservableOnSubscribe<Cursor>) emitter -> {
      try {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursors = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MEDIA_PROJECTION,
            SELECTION,
            null/*SELECTION_ARGS*/,
            ORDER_BY);
        emitter.onNext(cursors);
      } catch (Throwable t) {
        emitter.onError(t);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(cursors -> {
      if (listener != null) listener.success(cursors);
    }, throwable -> Log.e("jason", throwable.getMessage()));
  }
}
