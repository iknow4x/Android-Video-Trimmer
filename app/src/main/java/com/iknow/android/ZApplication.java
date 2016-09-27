package com.iknow.android;

import android.app.Application;

import iknow.android.utils.BaseUtils;

/**
 * Author：J.Chou
 * Date：  2016.09.27 10:44 AM
 * Email： who_know_me@163.com
 * Describe:
 */
public class ZApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BaseUtils.init(this);
    }
}
