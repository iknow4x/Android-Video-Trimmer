package com.iknow.android.features.select;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.iknow.android.R;
import iknow.android.utils.DateUtil;
import iknow.android.utils.DeviceUtil;
import iknow.android.utils.callback.SingleCallback;
import java.io.File;
import java.util.ArrayList;

/**
 * Author：J.Chou
 * Date：  2016.08.01 3:45 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoSelectAdapter extends CursorAdapter {

  private int videoCoverSize = DeviceUtil.getDeviceWidth() / 3;
  private ArrayList<String> mVideoSelected = new ArrayList<>();
  private ArrayList<ImageView> mSelectIconList = new ArrayList<>();
  private SingleCallback<Boolean, String> mSingleCallback;
  private MediaMetadataRetriever mMetadataRetriever;
  private boolean isSelected = false;

  VideoSelectAdapter(Context context, Cursor c) {
    super(context, c);
    mMetadataRetriever = new MediaMetadataRetriever();
  }

  @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View itemView = inflater.inflate(R.layout.video_select_gridview_item, null);
    VideoGridViewHolder holder = new VideoGridViewHolder();
    holder.videoItemView = itemView.findViewById(R.id.video_view);
    holder.videoCover = itemView.findViewById(R.id.cover_image);
    holder.durationTv = itemView.findViewById(R.id.video_duration);
    holder.videoSelectPanel = itemView.findViewById(R.id.video_select_panel);
    holder.selectIcon = itemView.findViewById(R.id.select);
    itemView.setTag(holder);
    return itemView;
  }

  @Override public void bindView(View view, Context context, final Cursor cursor) {
    final VideoGridViewHolder holder = (VideoGridViewHolder) view.getTag();
    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
    if (TextUtils.isEmpty(path) || !new File(path).exists()) {
      return;
    }
    try {
      mMetadataRetriever.setDataSource(path);
    } catch (Throwable e) {
      e.printStackTrace();
      return;
    }
    final String duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    if (TextUtils.isEmpty(duration)) return;

    holder.durationTv.setText(DateUtil.convertSecondsToTime(Integer.parseInt(duration) / 1000));
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.videoCover.getLayoutParams();
    params.width = videoCoverSize;
    params.height = videoCoverSize;
    holder.videoCover.setLayoutParams(params);
    Glide.with(context)
        .load(getVideoUri(cursor))
        .crossFade()
        .into(holder.videoCover);
    if(mVideoSelected.size() > 0)
      holder.selectIcon.setImageResource(path.equals(mVideoSelected.get(0)) ? R.drawable.icon_video_selected : R.drawable.icon_video_unselected);
    else
      holder.selectIcon.setImageResource(R.drawable.icon_video_unselected);
    holder.videoSelectPanel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (mVideoSelected.size() > 0) {
          if (path.equals(mVideoSelected.get(0))) {
            holder.selectIcon.setImageResource(R.drawable.icon_video_unselected);
            clearAll();
            isSelected = false;
          } else {
            mSelectIconList.get(0).setImageResource(R.drawable.icon_video_unselected);
            clearAll();
            addData(path, holder.selectIcon);
            holder.selectIcon.setImageResource(R.drawable.icon_video_selected);
            isSelected = true;
          }
        } else {
          clearAll();
          addData(path, holder.selectIcon);
          holder.selectIcon.setImageResource(R.drawable.icon_video_selected);
          isSelected = true;
        }
        mSingleCallback.onSingleCallback(isSelected, path);
      }
    });
  }

  @Override
  public Object getItem(int position) {
    return super.getItem(position);
  }

  private Uri getVideoUri(Cursor cursor) {
    String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
    return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
  }

  void setItemClickCallback(final SingleCallback<Boolean, String> singleCallback) {
    this.mSingleCallback = singleCallback;
  }

  private void addData(String videoPath, ImageView imageView) {
    mVideoSelected.add(videoPath);
    mSelectIconList.add(imageView);
  }

  private void clearAll() {
    mVideoSelected.clear();
    mSelectIconList.clear();
  }

  private static class VideoGridViewHolder {
    ImageView videoCover, selectIcon;
    View videoItemView, videoSelectPanel;
    TextView durationTv;
  }
}
