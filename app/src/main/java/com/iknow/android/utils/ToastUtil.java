package com.iknow.android.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/01/21 6:01 PM
 * version: 1.0
 * description:
 */
public class ToastUtil {
  public static void show(final Context context,final CharSequence text) {
    if (context == null || TextUtils.isEmpty(text)) {
      return;
    }
    UIThreadUtil.runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
  }

  public static void show(final Context context, final int resId) {
    if (context == null) return;
    UIThreadUtil.runOnUiThread(() -> Toast.makeText(context, resId, Toast.LENGTH_SHORT).show());
  }

  public static void longShow(final Context context,final CharSequence text) {
    if (context == null || TextUtils.isEmpty(text)) {
      return;
    }
    UIThreadUtil.runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
  }
}
