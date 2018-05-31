package com.iknow.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/05/30/4:20 PM
 * version: 1.0
 * description:
 */
public class VideoTrimmerAdapter extends RecyclerView.Adapter {

  private LayoutInflater inflater;
  private int itemW;
  private Context context;


  public VideoTrimmerAdapter(Context context) {
    this.context = context;
    this.inflater = LayoutInflater.from(context);
    this.itemW = itemW;
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new TrimmerViewHolder(inflater.inflate(R.layout.video_thumb_itme_layout, parent, false));
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

  }

  @Override public int getItemCount() {
    return 0;
  }

  private final class TrimmerViewHolder extends RecyclerView.ViewHolder {
    public ImageView img;
    TrimmerViewHolder(View itemView) {
      super(itemView);
      img = itemView.findViewById(R.id.thumb);
      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
      layoutParams.width = itemW;
      img.setLayoutParams(layoutParams);
    }
  }

}
