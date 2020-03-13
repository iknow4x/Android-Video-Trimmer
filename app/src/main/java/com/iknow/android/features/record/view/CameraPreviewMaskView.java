package com.iknow.android.features.record.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/02/15 3:01 PM
 * version: 1.0
 * description:
 */
public class CameraPreviewMaskView extends View {

  public CameraPreviewMaskView(Context context) {
    super(context);
    init();
  }

  public CameraPreviewMaskView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CameraPreviewMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setBackgroundColor(Color.parseColor("#cc000000"));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }
}
