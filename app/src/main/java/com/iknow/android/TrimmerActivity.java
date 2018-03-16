package com.iknow.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import com.iknow.android.databinding.ActivityTrimmerBinding;
import com.iknow.android.interfaces.CompressVideoListener;
import com.iknow.android.interfaces.TrimVideoListener;
import com.iknow.android.utils.CompressVideoUtil;
import com.iknow.android.utils.TrimVideoUtil;
import java.io.File;

public class TrimmerActivity extends AppCompatActivity implements TrimVideoListener {

    private static final String TAG = "jason";
    public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
    private File tempFile;
    private ActivityTrimmerBinding binding;
    private ProgressDialog mProgressDialog;

    public static void go(FragmentActivity from, String videoPath){
        if(!TextUtils.isEmpty(videoPath)) {
            Bundle bundle = new Bundle();
            bundle.putString("path", videoPath);
            Intent intent = new Intent(from,TrimmerActivity.class);
            intent.putExtras(bundle);
            from.startActivityForResult(intent,VIDEO_TRIM_REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_trimmer);
        Bundle bd = getIntent().getExtras();
        String path = "";
        if(bd != null)
            path = bd.getString("path");

        if (binding.trimmerView != null) {
            binding.trimmerView.setMaxDuration(TrimVideoUtil.VIDEO_MAX_DURATION);
            binding.trimmerView.setOnTrimVideoListener(this);
            binding.trimmerView.setVideoURI(Uri.parse(path));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.trimmerView.onPause();
        binding.trimmerView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.trimmerView.destroy();
    }

    @Override
    public void onStartTrim() {
        buildDialog(getResources().getString(R.string.trimming)).show();
    }

    @Override
    public void onFinishTrim(String in) {
        //TODO: please handle your trimmed video url here!!!
        String out = "/storage/emulated/0/Android/data/com.iknow.android/cache/compress.mp4";
        buildDialog(getResources().getString(R.string.compressing)).show();
        CompressVideoUtil.compress(this, in, out, new CompressVideoListener(){
            @Override public void onSuccess(String message) {
            }

            @Override public void onFailure(String message) {
            }

            @Override public void onFinish() {
             if(mProgressDialog.isShowing()) mProgressDialog.dismiss();
             finish();
            }
        });
    }

    @Override
    public void onCancel() {
        binding.trimmerView.destroy();
        finish();
    }

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }
}
