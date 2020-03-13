package com.iknow.android.widget;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author: Jason.Chou
 * Email: who_know_me@163.com
 * Created: 2016年05月04日 11:27 AM
 * Description:
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration{

  private int halfSpace;

  public SpacesItemDecoration(int space) {
    this.halfSpace = space / 2;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

    if (parent.getPaddingLeft() != halfSpace) {
      parent.setPadding(halfSpace, halfSpace, halfSpace, halfSpace);
      parent.setClipToPadding(false);
    }

    outRect.top = halfSpace;
    outRect.bottom = halfSpace;
    outRect.left = halfSpace;
    outRect.right = halfSpace;
  }
}
