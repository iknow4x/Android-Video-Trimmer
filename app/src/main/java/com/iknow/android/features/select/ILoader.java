package com.iknow.android.features.select;

import android.content.Context;
import iknow.android.utils.callback.SimpleCallback;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/10/04 1:49 PM
 * version: 1.0
 * description:
 */
public interface ILoader {
  void load(final Context mContext, final SimpleCallback listener);
}
