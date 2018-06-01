package com.iknow.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;
import com.iknow.android.R;
import com.iknow.android.VideoTrimmerAdapter;
import com.iknow.android.interfaces.ProgressVideoListener;
import com.iknow.android.interfaces.TrimVideoListener;
import com.iknow.android.utils.TrimVideoUtil;
import iknow.android.utils.DeviceUtil;
import iknow.android.utils.UnitConverter;
import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;
import iknow.android.utils.thread.UiThreadExecutor;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VideoTrimmerView2 extends FrameLayout {

  /**
   * 计算公式:
   * PixRangeMax = (视频总长 * SCREEN_WIDTH) / 视频最长的裁剪时间(15s)
   * 视频总长/PixRangeMax = 当前视频的时间/游标当前所在位置
   */
  private static boolean isDebugMode = false;

  private static final long MIN_CUT_DURATION = 3 * 1000L;// 最小剪辑时间3s
  private static final long MAX_CUT_DURATION = 10 * 1000L;//视频最多剪切多长时间10s
  private static final int MAX_COUNT_RANGE = 10;//seekBar的区域内一共有多少张图片

  private static final String TAG = VideoTrimmerView2.class.getSimpleName();
  private static final int margin = UnitConverter.dpToPx(6);
  private static final int SCREEN_WIDTH = (DeviceUtil.getDeviceWidth() - margin * 2);
  private static final int SCREEN_WIDTH_FULL = DeviceUtil.getDeviceWidth();
  private static final int SHOW_PROGRESS = 2;

  private Context mContext;
  private RelativeLayout mLinearVideo;
  private VideoView mVideoView;
  private ImageView mPlayView;
  private RecyclerView mVideoThumbRecyclerView;
  private RangeSeekBarView2 mRangeSeekBarView;
  private LinearLayout mSeekBarLayout;
  private ImageView mPositionIcon;
  private int mMaxWidth;
  private float averageMsPx;//每毫秒所占的px
  private float averagePxMs;//每px所占用的ms毫秒

  private Uri mSrc;
  private String mFinalPath;

  private long mMaxDuration;
  private ProgressVideoListener mListeners;

  private TrimVideoListener mOnTrimVideoListener;
  private int mDuration = 0;
  private long mTimeVideo = 0;
  private int mStartPosition = 0, mEndPosition = 0;

  private VideoTrimmerAdapter mVideoThumbAdapter;
  private long pixelRangeMax;
  private int currentPixMax;  //用于处理红色进度条
  private int mScrolledOffset;
  private float leftThumbValue, rightThumbValue;
  private boolean isFromRestore = false;
  //new
  private long leftProgress, rightProgress;
  private long scrollPos = 0;
  private int mScaledTouchSlop;
  private int lastScrollX;
  private boolean isSeeking;
  private boolean isOverScaledTouchSlop;
  private int mTotalThumbsCount;

  private final MessageHandler mMessageHandler = new MessageHandler(this);

  public VideoTrimmerView2(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoTrimmerView2(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    this.mContext = context;
    LayoutInflater.from(context).inflate(R.layout.video_trimmer_view2, this, true);

    mLinearVideo = findViewById(R.id.layout_surface_view);
    mVideoView = findViewById(R.id.video_loader);
    mPlayView = findViewById(R.id.icon_video_play);
    mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
    mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
    mVideoThumbAdapter = new VideoTrimmerAdapter(mContext);
    mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
    mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener);
    setUpListeners();
  }

  private void initRangeSeekBarView() {
    long endPosition = mDuration;
    int rangeWidth;
    boolean isOver_60_s;
    if (endPosition <= MAX_CUT_DURATION) {
      isOver_60_s = false;
      mTotalThumbsCount = MAX_COUNT_RANGE;
      rangeWidth = mMaxWidth;
    } else {
      isOver_60_s = true;
      mTotalThumbsCount = (int) (endPosition * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE);
      rangeWidth = mMaxWidth / MAX_COUNT_RANGE * mTotalThumbsCount;
    }
    mVideoThumbRecyclerView.addItemDecoration(new SpacesItemDecoration2(UnitConverter.dpToPx(35), mTotalThumbsCount));
    mSeekBarLayout = findViewById(R.id.seekBarLayout);
    mPositionIcon = findViewById(R.id.positionIcon);
    if (isOver_60_s) {
      mRangeSeekBarView = new RangeSeekBarView2(mContext, 0L, MAX_CUT_DURATION);
      mRangeSeekBarView.setSelectedMinValue(0L);
      mRangeSeekBarView.setSelectedMaxValue(MAX_CUT_DURATION);
    } else {
      mRangeSeekBarView = new RangeSeekBarView2(mContext, 0L, endPosition);
      mRangeSeekBarView.setSelectedMinValue(0L);
      mRangeSeekBarView.setSelectedMaxValue(endPosition);
    }
    mRangeSeekBarView.setMin_cut_time(MIN_CUT_DURATION);
    mRangeSeekBarView.setNotifyWhileDragging(true);
    mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
    mSeekBarLayout.addView(mRangeSeekBarView);

    averageMsPx = mDuration * 1.0f / rangeWidth * 1.0f;
    //init pos icon start
    leftProgress = 0;
    if (isOver_60_s) {
      rightProgress = MAX_CUT_DURATION;
    } else {
      rightProgress = endPosition;
    }
    averagePxMs = (mMaxWidth * 1.0f / (rightProgress - leftProgress));
  }

  public void initVideoByURI(final Uri videoURI) {
    mSrc = videoURI;
    mVideoView.setVideoURI(mSrc);
    mVideoView.requestFocus();
  }

  private void startShootVideoThumbs(final Context context, final Uri videoUri, int totalThumbsCount, long startPosition, long endPosition) {
    TrimVideoUtil.backgroundShootVideoThumb(context, videoUri, totalThumbsCount, startPosition, endPosition, new SingleCallback<ArrayList<Bitmap>, Integer>() {
      @Override public void onSingleCallback(final ArrayList<Bitmap> bitmaps, final Integer interval) {
        UiThreadExecutor.runTask("", new Runnable() {
          @Override public void run() {
            mVideoThumbAdapter.addBitmaps(bitmaps);
          }
        }, 0L);
      }
    });
  }

  private void onCancelClicked() {
    mOnTrimVideoListener.onCancel();
  }

  private void onPlayerIndicatorSeekStart() {
    mMessageHandler.removeMessages(SHOW_PROGRESS);
    mVideoView.pause();
    notifyProgressUpdate();
  }

  private void onPlayerIndicatorSeekStop(SeekBar seekBar) {
    mVideoView.pause();
  }

  private void onVideoPrepared(MediaPlayer mp) {
    ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
    int videoWidth = mp.getVideoWidth();
    int videoHeight = mp.getVideoHeight();
    float videoProportion = (float) videoWidth / (float) videoHeight;
    int screenWidth = mLinearVideo.getWidth();
    int screenHeight = mLinearVideo.getHeight();
    float screenProportion = (float) screenWidth / (float) screenHeight;

    if (videoProportion > screenProportion) {
      lp.width = screenWidth;
      lp.height = (int) ((float) screenWidth / videoProportion);
    } else {
      lp.width = (int) (videoProportion * (float) screenHeight);
      lp.height = screenHeight;
    }
    mVideoView.setLayoutParams(lp);
    mDuration = mVideoView.getDuration();
    if (!getRestoreState()) {
      //initSeekBarPosition();
      mVideoView.seekTo(mStartPosition);
    } else {
      setRestoreState(false);
      //initSeekBarFromRestore();
    }
    initRangeSeekBarView();
    startShootVideoThumbs(mContext, mSrc, mTotalThumbsCount, 0, mDuration);

  }

  private void onVideoCompleted() {
    seekTo(mStartPosition);
    setPlayPauseViewIcon(false);
  }

  private void onVideoReset() {
    mVideoView.pause();
    setPlayPauseViewIcon(false);
  }

  public void onPause() {
    if (mVideoView.isPlaying()) {
      mMessageHandler.removeMessages(SHOW_PROGRESS);
      mVideoView.pause();
      seekTo(mStartPosition);//复位
      setPlayPauseViewIcon(false);
    }
  }

  public void setOnTrimVideoListener(TrimVideoListener onTrimVideoListener) {
    mOnTrimVideoListener = onTrimVideoListener;
  }

  /**
   * Cancel trim thread execut action when finish
   */
  public void destroy() {
    BackgroundExecutor.cancelAll("", true);
    UiThreadExecutor.cancelAll("");
  }

  public void setMaxDuration(int maxDuration) {
    mMaxDuration = maxDuration * 1000;
  }

  private void setUpListeners() {
    mListeners = new ProgressVideoListener() {
      @Override public void updateProgress(int time, int max, float scale) {
        updateVideoProgress(time);
      }
    };
    findViewById(R.id.cancelBtn).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        onCancelClicked();
      }
    });

    findViewById(R.id.finishBtn).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        onSaveClicked();
      }
    });
    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        onVideoPrepared(mp);
      }
    });
    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        onVideoCompleted();
      }
    });
    mPlayView.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        onClickVideoPlayPause();
      }
    });
  }

  private void onSaveClicked() {
    if (mEndPosition / 1000 - mStartPosition / 1000 < TrimVideoUtil.MIN_TIME_FRAME) {
      Toast.makeText(mContext, "视频长不足5秒,无法上传", Toast.LENGTH_SHORT).show();
    } else {
      mVideoView.pause();
      TrimVideoUtil.trim(mContext, mSrc.getPath(), getTrimmedVideoPath(), mStartPosition, mEndPosition, mOnTrimVideoListener);
    }
  }

  private String getTrimmedVideoPath() {
    if (mFinalPath == null) {
      File file = mContext.getExternalCacheDir();
      if (file != null) mFinalPath = file.getAbsolutePath();
    }
    return mFinalPath;
  }

  private void onClickVideoPlayPause() {
    if (mVideoView.isPlaying()) {
      mVideoView.pause();
      mMessageHandler.removeMessages(SHOW_PROGRESS);
    } else {
      mVideoView.start();
      //mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    setPlayPauseViewIcon(mVideoView.isPlaying());
  }

  /**
   * 屏幕长度转化成视频的长度
   */
  private long PixToTime(float value) {
    if (pixelRangeMax == 0) return 0;
    return (long) ((mDuration * value) / pixelRangeMax);
  }

  /**
   * 视频长度转化成屏幕的长度
   */
  private long TimeToPix(long value) {
    return (pixelRangeMax * value) / mDuration;
  }

  private void seekTo(long msec) {
    mVideoView.seekTo((int) msec);
  }

  private boolean getRestoreState() {
    return isFromRestore;
  }

  public void setRestoreState(boolean fromRestore) {
    isFromRestore = fromRestore;
  }

  private void updateVideoProgress(int time) {
    if (mVideoView == null) {
      return;
    }
    if (isDebugMode) Log.i("Jason", "updateVideoProgress time = " + time);
    if (time >= mEndPosition) {
      mMessageHandler.removeMessages(SHOW_PROGRESS);
      mVideoView.pause();
      seekTo(mStartPosition);
      setPlayPauseViewIcon(false);
      return;
    }
  }

  private void notifyProgressUpdate() {
    if (mDuration == 0) return;
    int position = mVideoView.getCurrentPosition();
    if (isDebugMode) Log.i("Jason", "updateVideoProgress position = " + position);
    mListeners.updateProgress(position, 0, 0);
  }

  private void setPlayPauseViewIcon(boolean isPlaying) {
    mPlayView.setImageResource(isPlaying ? R.drawable.icon_video_pause_black : R.drawable.icon_video_play_black);
  }

  private final RangeSeekBarView2.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBarView2.OnRangeSeekBarChangeListener() {
    @Override public void onRangeSeekBarValuesChanged(RangeSeekBarView2 bar, long minValue, long maxValue, int action, boolean isMin,
        RangeSeekBarView2.Thumb pressedThumb) {
      Log.d(TAG, "-----minValue----->>>>>>" + minValue);
      Log.d(TAG, "-----maxValue----->>>>>>" + maxValue);
      leftProgress = minValue + scrollPos;
      rightProgress = maxValue + scrollPos;
      Log.d(TAG, "-----leftProgress----->>>>>>" + leftProgress);
      Log.d(TAG, "-----rightProgress----->>>>>>" + rightProgress);
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          Log.d(TAG, "-----ACTION_DOWN---->>>>>>");
          isSeeking = false;
          //videoPause();
          break;
        case MotionEvent.ACTION_MOVE:
          Log.d(TAG, "-----ACTION_MOVE---->>>>>>");
          isSeeking = true;
          mVideoView.seekTo((int) (pressedThumb == RangeSeekBarView2.Thumb.MIN ? leftProgress : rightProgress));
          break;
        case MotionEvent.ACTION_UP:
          Log.d(TAG, "-----ACTION_UP--leftProgress--->>>>>>" + leftProgress);
          isSeeking = false;
          //从minValue开始播
          mVideoView.seekTo((int) leftProgress);
          break;
        default:
          break;
      }
    }
  };

  private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
    @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      Log.d(TAG, "-------newState:>>>>>" + newState);
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        isSeeking = false;
        //videoStart();
      } else {
        isSeeking = true;
        if (isOverScaledTouchSlop && mVideoView != null && mVideoView.isPlaying()) {
          //videoPause();
        }
      }
    }

    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);
      isSeeking = false;
      int scrollX = getScrollXDistance();
      //达不到滑动的距离
      if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
        isOverScaledTouchSlop = false;
        return;
      }
      isOverScaledTouchSlop = true;
      Log.d(TAG, "-------scrollX:>>>>>" + scrollX);
      //初始状态,why ? 因为默认的时候有35dp的空白！
      if (scrollX == -UnitConverter.dpToPx(35)) {
        scrollPos = 0;
      } else {
        // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
        if (mVideoView != null && mVideoView.isPlaying()) {
          //videoPause();
        }
        isSeeking = true;
        scrollPos = (long) (averageMsPx * (UnitConverter.dpToPx(35) + scrollX));
        Log.d(TAG, "-------scrollPos:>>>>>" + scrollPos);
        leftProgress = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
        rightProgress = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;
        Log.d(TAG, "-------leftProgress:>>>>>" + leftProgress);
        mVideoView.seekTo((int) leftProgress);
      }
      lastScrollX = scrollX;
    }
  };

  /**
   * 水平滑动了多少px
   */
  private int getScrollXDistance() {
    LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
    int position = layoutManager.findFirstVisibleItemPosition();
    View firstVisibleChildView = layoutManager.findViewByPosition(position);
    int itemWidth = firstVisibleChildView.getWidth();
    return (position) * itemWidth - firstVisibleChildView.getLeft();
  }

  private static class MessageHandler extends Handler {
    private final WeakReference<VideoTrimmerView2> mView;

    MessageHandler(VideoTrimmerView2 view) {
      mView = new WeakReference<>(view);
    }

    @Override public void handleMessage(Message msg) {
      VideoTrimmerView2 view = mView.get();
      if (view == null || view.mVideoView == null) {
        return;
      }

      view.notifyProgressUpdate();
      if (view.mVideoView.isPlaying()) {
        sendEmptyMessageDelayed(0, 10);
      }
    }
  }

}
