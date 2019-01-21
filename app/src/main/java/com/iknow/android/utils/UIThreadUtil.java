package com.iknow.android.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/01/21 6:02 PM
 * version: 1.0
 * description:
 */
public class UIThreadUtil {
  private volatile static Handler mainHandler;

  @SuppressWarnings("WeakerAccess")
  public static Handler getMainHandler() {
    synchronized (UIThreadUtil.class) {
      if (mainHandler == null) {
        mainHandler = new Handler(Looper.getMainLooper());
      }
      return mainHandler;
    }
  }

  public static void runOnUiThread(Runnable runnable) {
    internalRunOnUiThread(runnable, 0);
  }

  public static void runOnUiThread(Runnable runnable, long delayMillis) {
    internalRunOnUiThread(runnable, delayMillis);
  }

  private static void internalRunOnUiThread(Runnable runnable, long delayMillis) {
    getMainHandler().postDelayed(runnable, delayMillis);
  }

  public static boolean isMainThread(){
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }
}
