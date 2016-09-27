package com.iknow.android;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import com.iknow.android.databinding.VideoSelectLayoutBinding;
import com.iknow.android.models.VideoInfo;
import com.iknow.android.utils.TrimVideoUtil;
import com.iknow.android.widget.SpacesItemDecoration;

import java.util.ArrayList;

import iknow.android.utils.callback.SingleCallback;

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoSelectActivity extends AppCompatActivity implements View.OnClickListener {

    private VideoSelectLayoutBinding binding;
    private ArrayList<VideoInfo> allVideos = new ArrayList<>();
    private String videoPath;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.video_select_layout);

        allVideos = TrimVideoUtil.getAllVideoFiles(this);
        GridLayoutManager manager = new GridLayoutManager(this, 4);
        binding.videoSelectRecyclerview.addItemDecoration(new SpacesItemDecoration(5));
        binding.videoSelectRecyclerview.setHasFixedSize(true);
        VideoGridViewAdapter videoGridViewAdapter;
        binding.videoSelectRecyclerview.setAdapter(videoGridViewAdapter = new VideoGridViewAdapter(this, allVideos));
        binding.videoSelectRecyclerview.setLayoutManager(manager);

        binding.videoShoot.setOnClickListener(this);
        binding.mBtnBack.setOnClickListener(this);
        binding.nextStep.setOnClickListener(this);

        binding.nextStep.setTextAppearance(this, R.style.gray_text_18_style);
        binding.nextStep.setEnabled(false);

        videoGridViewAdapter.setItemClickCallback(new SingleCallback<Boolean, VideoInfo>() {
            @Override
            public void onSingleCallback(Boolean isSelected, VideoInfo video) {
                if (video != null)
                    videoPath = video.getVideoPath();
                binding.nextStep.setEnabled(isSelected);
                binding.nextStep.setTextAppearance(VideoSelectActivity.this, isSelected ? R.style.blue_text_18_style : R.style.gray_text_18_style);
            }
        });
//        FileUtils.createVideoTempFolder();//Create temp file folder
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        allVideos = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.mBtnBack.getId()) {
            finish();
        } else if (v.getId() == binding.nextStep.getId()) {
            TrimmerActivity.go(VideoSelectActivity.this, videoPath);
        }
    }
}
