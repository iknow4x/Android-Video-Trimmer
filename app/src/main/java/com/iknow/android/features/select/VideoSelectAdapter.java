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
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.iknow.android.R;
import com.iknow.android.features.trim.VideoTrimmerActivity;
import iknow.android.utils.DateUtil;
import iknow.android.utils.DeviceUtil;
import java.io.File;

/**
 * Author：J.Chou
 * Date：  2016.08.01 3:45 PM
 * Email： who_know_me@163.com
 * Describe:
 */
public class VideoSelectAdapter extends CursorAdapter {

  private int videoCoverSize = DeviceUtil.getDeviceWidth() / 3;
  private MediaMetadataRetriever mMetadataRetriever;
  private Context mContext;

  VideoSelectAdapter(Context context, Cursor c) {
    super(context, c);
    this.mContext = context;
    mMetadataRetriever = new MediaMetadataRetriever();
  }

  @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View itemView = inflater.inflate(R.layout.video_select_gridview_item, null);
    VideoGridViewHolder holder = new VideoGridViewHolder();
    holder.videoItemView = itemView.findViewById(R.id.video_view);
    holder.videoCover = itemView.findViewById(R.id.cover_image);
    holder.durationTv = itemView.findViewById(R.id.video_duration);
    itemView.setTag(holder);
    return itemView;
  }

  @Override public void bindView(View view, Context context, final Cursor cursor) {
    final VideoGridViewHolder holder = (VideoGridViewHolder) view.getTag();
    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
    if (!checkDataValid(cursor)) {
      return;
    }
    final String duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    holder.durationTv.setText(DateUtil.convertSecondsToTime(Integer.parseInt(duration) / 1000));
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.videoCover.getLayoutParams();
    params.width = videoCoverSize;
    params.height = videoCoverSize;
    holder.videoCover.setLayoutParams(params);
    Glide.with(context)
        .load(getVideoUri(cursor))
        .centerCrop()
        .override(videoCoverSize, videoCoverSize)
        .into(holder.videoCover);
    holder.videoItemView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        VideoTrimmerActivity.call((FragmentActivity) mContext, path);
      }
    });
  }

  private boolean checkDataValid(final Cursor cursor) {
    final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
    if (TextUtils.isEmpty(path) || !new File(path).exists()) {
      return false;
    }
    try {
      mMetadataRetriever.setDataSource(path);
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
    final String duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    return !TextUtils.isEmpty(duration);
  }

  private Uri getVideoUri(Cursor cursor) {
    String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
    return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
  }

  private static class VideoGridViewHolder {
    ImageView videoCover;
    View videoItemView;
    TextView durationTv;
  }
}
