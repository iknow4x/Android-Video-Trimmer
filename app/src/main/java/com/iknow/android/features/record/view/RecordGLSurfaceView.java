package com.iknow.android.features.record.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2019/03/17 11:59 AM
 * version: 1.0
 * description:
 */
public class RecordGLSurfaceView extends GLSurfaceView implements
    GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {

  private Context mContext;
  private SurfaceTexture mSurface;

  public RecordGLSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    setEGLContextClientVersion(2);
    setRenderer(this);
    setRenderMode(RENDERMODE_WHEN_DIRTY);
  }

  @Override public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

  }

  @Override public void onSurfaceChanged(GL10 gl10, int i, int i1) {

  }

  @Override public void onDrawFrame(GL10 gl10) {

  }

  @Override public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    this.requestRender();
  }
}
