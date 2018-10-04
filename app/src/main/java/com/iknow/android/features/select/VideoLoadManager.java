package com.iknow.android.features.select;

import android.content.Context;
import iknow.android.utils.callback.SimpleCallback;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/10/04 1:50 PM
 * version: 1.0
 * description:
 */
public class VideoLoadManager {

  private ILoader mLoader;

  public void setLoader(ILoader loader) {
    this.mLoader = loader;
  }

  public void load(final Context context, final SimpleCallback listener) {
    mLoader.load(context, listener);
  }
}
