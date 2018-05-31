package com.iknow.android.interfaces;

public interface TrimVideoListener {
    void onStartTrim();
    void onFinishTrim(String url);
    void onCancel();
}
