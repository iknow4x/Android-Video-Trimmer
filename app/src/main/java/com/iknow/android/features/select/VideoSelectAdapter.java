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
import com.iknow.android.models.VideoInfo;
import iknow.android.utils.DateUtil;
import iknow.android.utils.DeviceUtil;
import iknow.android.utils.callback.SingleCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author：J.Chou
 * Date：  2016.08.01 3:45 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoSelectAdapter extends CursorAdapter {

  private int videoCoverSize = DeviceUtil.getDeviceWidth() / 3;
  private Context context;
  private List<VideoInfo> mVideoListData = new ArrayList<>();
  private SingleCallback<Boolean, Cursor> mSingleCallback;
  private ArrayList<Cursor> videoSelected = new ArrayList<>();
  private ArrayList<ImageView> selectIconList = new ArrayList<>();
  private boolean isSelected = false;
  private MediaMetadataRetriever mMetadataRetriever;

  VideoSelectAdapter(Context context, Cursor c) {
    super(context, c);
    this.context = context;
    mMetadataRetriever = new MediaMetadataRetriever();
  }

  @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View itemView = inflater.inflate(R.layout.video_select_gridview_item, null);
    VideoViewHolder holder = new VideoViewHolder();
    holder.videoItemView = itemView.findViewById(R.id.video_view);
    holder.videoCover = itemView.findViewById(R.id.cover_image);
    holder.durationTv = itemView.findViewById(R.id.video_duration);
    holder.videoSelectPanel = itemView.findViewById(R.id.video_select_panel);
    holder.selectIcon = itemView.findViewById(R.id.select);
    itemView.setTag(holder);
    return itemView;
  }

  @Override public void bindView(View view, Context context, Cursor cursor) {
    final VideoViewHolder holder = (VideoViewHolder) view.getTag();
    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
    if (TextUtils.isEmpty(path) || !new File(path).exists()) {
      return;
    }
    try {
      mMetadataRetriever.setDataSource(path);
    } catch (Exception e) {
      e.printStackTrace();
      holder.videoSelectPanel.setOnClickListener(null);
      return;
    }

    String duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    if (TextUtils.isEmpty(duration) || "null".equals(duration)) {
      return;
    }
    int dur = Integer.parseInt(duration);
    holder.durationTv.setText(DateUtil.convertSecondsToTime(dur / 1000));

    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.videoCover.getLayoutParams();
    params.width = videoCoverSize;
    params.height = videoCoverSize;
    holder.videoCover.setLayoutParams(params);
    holder.videoSelectPanel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (videoSelected.size() > 0) {
          if (cursor.equals(videoSelected.get(0))) {
            holder.selectIcon.setImageResource(R.drawable.icon_video_unselected);
            clearAll();
            isSelected = false;
          } else {
            selectIconList.get(0).setImageResource(R.drawable.icon_video_unselected);
            clearAll();
            holder.selectIcon.setImageResource(R.drawable.icon_video_selected);
            isSelected = true;
          }
        } else {
          clearAll();
          addData(cursor);
          holder.selectIcon.setImageResource(R.drawable.icon_video_selected);
          isSelected = true;
        }
        mSingleCallback.onSingleCallback(isSelected, cursor);
      }
    });

    Glide.with(context)
        .load(getVideoUri(cursor))
        .crossFade()
        .into(holder.videoCover);
  }

  @Override
  public Object getItem(int position) {
    return super.getItem(position);
  }

  private Uri getVideoUri(Cursor cursor) {
    String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
    return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
  }

  void setItemClickCallback(final SingleCallback<Boolean, Cursor> singleCallback) {
    this.mSingleCallback = singleCallback;
  }

  class VideoViewHolder {
    ImageView videoCover, selectIcon;
    View videoItemView, videoSelectPanel;
    TextView durationTv;
  }

  private void addData(Cursor cursor) {
    videoSelected.add(cursor);
    //selectIconList.add(selectIcon);
  }

  private void clearAll() {
    videoSelected.clear();
    selectIconList.clear();
  }
}
