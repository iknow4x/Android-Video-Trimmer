package com.iknow.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.iknow.android.models.VideoInfo;
import com.iknow.android.utils.TrimVideoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import iknow.android.utils.DateUtil;
import iknow.android.utils.DeviceUtil;
import iknow.android.utils.callback.SingleCallback;


/**
 * Author：J.Chou
 * Date：  2016.08.01 3:45 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoGridViewAdapter extends RecyclerView.Adapter<VideoGridViewAdapter.VideoViewHolder> {

    private ArrayList<VideoInfo> videoListData;
    private Context context;
    private SingleCallback<Boolean, VideoInfo> mSingleCallback;
    private ArrayList<VideoInfo> videoSelect = new ArrayList<>();
    private ArrayList<ImageView> selectIconList = new ArrayList<>();

    VideoGridViewAdapter(Context context, ArrayList<VideoInfo> dataList) {
        this.context = context;
        this.videoListData = dataList;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = null;
        view = inflater.inflate(R.layout.video_select_gridview_item, null);

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

        VideoInfo video = videoListData.get(position);
        holder.durationTv.setText(DateUtil.convertSecondsToTime(video.getDuration() / 1000));
        ImageLoader.getInstance().displayImage(TrimVideoUtil.getVideoFilePath(video.getVideoPath()),holder.videoCover);
//        Bitmap bitmap = createVideoThumbnail(ImageDownloader.Scheme.FILE.crop(TrimVideoUtil.getVideoFilePath(video.getVideoPath())), MediaStore.Images.Thumbnails.MICRO_KIND);
//        holder.videoCover.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return videoListData.size();
    }

    void setItemClickCallback(final SingleCallback singleCallback) {
        this.mSingleCallback = singleCallback;
    }

    private boolean isSelected = false;

    class VideoViewHolder extends RecyclerView.ViewHolder {

        ImageView videoCover, selectIcon;
        View videoItemView, videoSelectPanel;
        TextView durationTv;

        VideoViewHolder(final View itemView) {
            super(itemView);
            videoItemView = itemView.findViewById(R.id.video_view);
            videoCover = (ImageView) itemView.findViewById(R.id.cover_image);
            durationTv = (TextView) itemView.findViewById(R.id.video_duration);
            videoSelectPanel = itemView.findViewById(R.id.video_select_panel);
            selectIcon = (ImageView) itemView.findViewById(R.id.select);

            int size = DeviceUtil.getDeviceWidth() / 4;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoCover.getLayoutParams();
            params.width = size;
            params.height = size;
            videoCover.setLayoutParams(params);
            videoSelectPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoInfo videoInfo = videoListData.get(getAdapterPosition());
                    if(videoSelect.size() > 0 ){
                        if(videoInfo.equals(videoSelect.get(0))){
                            selectIcon.setImageResource(R.drawable.icon_video_unselected);
                            clearAll();
                            isSelected = false;
                        }else{
                            selectIconList.get(0).setImageResource(R.drawable.icon_video_unselected);
                            clearAll();
                            addData(videoInfo);
                            selectIcon.setImageResource(R.drawable.icon_video_selected);
                            isSelected = true;
                        }

                    }else{
                        clearAll();
                        addData(videoInfo);
                        selectIcon.setImageResource(R.drawable.icon_video_selected);
                        isSelected = true;
                    }
                    mSingleCallback.onSingleCallback(isSelected, videoListData.get(getAdapterPosition()));
                }
            });
        }

        private void addData(VideoInfo videoInfo) {
            videoSelect.add(videoInfo);
            selectIconList.add(selectIcon);
        }
    }

    private void clearAll() {
        videoSelect.clear();
        selectIconList.clear();
    }
}
