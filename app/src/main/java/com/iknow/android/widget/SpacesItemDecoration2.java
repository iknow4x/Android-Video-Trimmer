package com.iknow.android.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration2 extends RecyclerView.ItemDecoration{

  private int space;
  private int thumbnailsCount;

  public SpacesItemDecoration2(int space, int thumbnailsCount) {
    this.space = space;
    this.thumbnailsCount = thumbnailsCount;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    if (position == 0) {
      outRect.left = space;
      outRect.right = 0;
    } else if (thumbnailsCount > 10 && position == thumbnailsCount - 1) {
      outRect.left = 0;
      outRect.right = space;
    } else {
      outRect.left = 0;
      outRect.right = 0;
    }
  }
}
