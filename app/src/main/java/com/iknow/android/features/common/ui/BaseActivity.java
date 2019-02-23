package com.iknow.android.features.common.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.iknow.android.interfaces.IBaseUI;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/22 4:38 PM
 * version: 1.0
 * description:
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements IBaseUI {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      initUI();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @CallSuper
  @Override public void initUI() {
  }

  @Override public void loadData() {
  }
}
