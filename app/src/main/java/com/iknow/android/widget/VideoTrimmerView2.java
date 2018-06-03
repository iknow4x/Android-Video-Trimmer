package com.iknow.android.widget;

import android.animation.ValueAnimator;
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
import android.view.animation.LinearInterpolator;
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
import iknow.android.utils.UnitConverter;
import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;
import iknow.android.utils.thread.UiThreadExecutor;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.iknow.android.utils.TrimVideoUtil.VIDEO_FRAMES_WIDTH;

public class VideoTrimmerView2 extends FrameLayout {

  /**
   * 计算公式:
   * PixRangeMax = (视频总长 * SCREEN_WIDTH) / 视频最长的裁剪时间(15s)
   * 视频总长/PixRangeMax = 当前视频的时间/游标当前所在位置
   */
  private static final String TAG = VideoTrimmerView2.class.getSimpleName();

  private static final int SHOW_PROGRESS = 2;

  private int mMaxWidth = VIDEO_FRAMES_WIDTH;
  private Context mContext;
  private RelativeLayout mLinearVideo;
  private VideoView mVideoView;
  private ImageView mPlayView;
  private RecyclerView mVideoThumbRecyclerView;
  private RangeSeekBarView2 mRangeSeekBarView;
  private LinearLayout mSeekBarLayout;
  private ImageView mPositionIcon;
  private float mAverageMsPx;//每毫秒所占的px
  private float averagePxMs;//每px所占用的ms毫秒

  private Uri mSourceUri;
  private String mFinalPath;

  private ProgressVideoListener mListeners;

  private TrimVideoListener mOnTrimVideoListener;
  private int mDuration = 0;
  private int mStartPosition = 0, mEndPosition = 0;

  private VideoTrimmerAdapter mVideoThumbAdapter;
  private boolean isFromRestore = false;
  //new
  private long mLeftProgressPos, mRightProgressPos;
  private long mRedProgressBarPos = 0;
  private long scrollPos = 0;
  private int mScaledTouchSlop;
  private int lastScrollX;
  private boolean isSeeking;
  private boolean isOverScaledTouchSlop;
  private int mThumbsTotalCount;

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
    mSeekBarLayout = findViewById(R.id.seekBarLayout);
    mPositionIcon = findViewById(R.id.positionIcon);
    mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
    mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
    mVideoThumbAdapter = new VideoTrimmerAdapter(mContext);
    mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
    mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener);
    setUpListeners();
  }

  private void initRangeSeekBarView() {
    int rangeWidth;
    mLeftProgressPos = 0;
    if (mDuration <= TrimVideoUtil.MAX_SHOOT_DURATION) {
      mThumbsTotalCount = TrimVideoUtil.MAX_COUNT_RANGE;
      rangeWidth = mMaxWidth;
      mRightProgressPos = mDuration;
    } else {
      mThumbsTotalCount = (int) (mDuration * 1.0f / (TrimVideoUtil.MAX_SHOOT_DURATION * 1.0f) * TrimVideoUtil.MAX_COUNT_RANGE);
      rangeWidth = mMaxWidth / TrimVideoUtil.MAX_COUNT_RANGE * mThumbsTotalCount;
      mRightProgressPos = TrimVideoUtil.MAX_SHOOT_DURATION;
    }
    mVideoThumbRecyclerView.addItemDecoration(new SpacesItemDecoration2(UnitConverter.dpToPx(35), mThumbsTotalCount));

    mRangeSeekBarView = new RangeSeekBarView2(mContext, mLeftProgressPos, mRightProgressPos);
    mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos);
    mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos);

    mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
    mRangeSeekBarView.setMinShootTime(TrimVideoUtil.MIN_SHOOT_DURATION);
    mRangeSeekBarView.setNotifyWhileDragging(true);
    mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
    mSeekBarLayout.addView(mRangeSeekBarView);

    mAverageMsPx = mDuration * 1.0f / rangeWidth * 1.0f;
    averagePxMs = (mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos));
  }

  public void initVideoByURI(final Uri videoURI) {
    mSourceUri = videoURI;
    mVideoView.setVideoURI(mSourceUri);
    mVideoView.requestFocus();
  }

  private void startShootVideoThumbs(final Context context, final Uri videoUri, int totalThumbsCount, long startPosition, long endPosition) {
    TrimVideoUtil.backgroundShootVideoThumb(context, videoUri, totalThumbsCount, startPosition, endPosition,
        new SingleCallback<ArrayList<Bitmap>, Integer>() {
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

  private void videoPrepared(MediaPlayer mp) {
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
      seekTo((int) mRedProgressBarPos);
    } else {
      setRestoreState(false);
      seekTo((int) mRedProgressBarPos);
    }
    initRangeSeekBarView();
    startShootVideoThumbs(mContext, mSourceUri, mThumbsTotalCount, 0, mDuration);
  }

  private void videoCompleted() {
    seekTo(mLeftProgressPos);
    setPlayPauseViewIcon(false);
  }

  private void onVideoReset() {
    mVideoView.pause();
    setPlayPauseViewIcon(false);
  }

  private void playVideoOrPause() {
    mRedProgressBarPos = mVideoView.getCurrentPosition();
    if (mVideoView.isPlaying()) {
      mVideoView.pause();
      pauseAnim();
    } else {
      mVideoView.start();
      playingAnim();
    }
    setPlayPauseViewIcon(mVideoView.isPlaying());
  }

  public void onPause() {
    if (mVideoView.isPlaying()) {
      seekTo(mLeftProgressPos);//复位
      mVideoView.pause();
      setPlayPauseViewIcon(false);
      mPositionIcon.setVisibility(GONE);
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
        videoPrepared(mp);
      }
    });
    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override public void onCompletion(MediaPlayer mp) {
        videoCompleted();
      }
    });
    mPlayView.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        playVideoOrPause();
      }
    });
  }

  private void onSaveClicked() {
    if (mEndPosition / 1000 - mStartPosition / 1000 < TrimVideoUtil.MIN_TIME_FRAME) {
      Toast.makeText(mContext, "视频长不足5秒,无法上传", Toast.LENGTH_SHORT).show();
    } else {
      mVideoView.pause();
      TrimVideoUtil.trim(mContext, mSourceUri.getPath(), getTrimmedVideoPath(), mStartPosition, mEndPosition, mOnTrimVideoListener);
    }
  }

  private String getTrimmedVideoPath() {
    if (mFinalPath == null) {
      File file = mContext.getExternalCacheDir();
      if (file != null) mFinalPath = file.getAbsolutePath();
    }
    return mFinalPath;
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
    if (TrimVideoUtil.isDebugMode) Log.i("Jason", "updateVideoProgress time = " + time);
    if (time >= mEndPosition) {
      mVideoView.pause();
      seekTo(mStartPosition);
      setPlayPauseViewIcon(false);
      return;
    }
  }

  private void notifyProgressUpdate() {
    if (mDuration == 0) return;
    int position = mVideoView.getCurrentPosition();
    if (TrimVideoUtil.isDebugMode) Log.i("Jason", "updateVideoProgress position = " + position);
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
      mLeftProgressPos = minValue + scrollPos;
      mRedProgressBarPos = mLeftProgressPos;
      mRightProgressPos = maxValue + scrollPos;
      Log.d(TAG, "-----mLeftProgressPos----->>>>>>" + mLeftProgressPos);
      Log.d(TAG, "-----mRightProgressPos----->>>>>>" + mRightProgressPos);
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          isSeeking = false;
          break;
        case MotionEvent.ACTION_MOVE:
          isSeeking = true;
          seekTo((int) (pressedThumb == RangeSeekBarView2.Thumb.MIN ? mLeftProgressPos : mRightProgressPos));
          break;
        case MotionEvent.ACTION_UP:
          isSeeking = false;
          seekTo((int) mLeftProgressPos);
          break;
        default:
          break;
      }

      mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
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
      int scrollX = calcScrollXDistance();
      //达不到滑动的距离
      if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
        isOverScaledTouchSlop = false;
        return;
      }
      isOverScaledTouchSlop = true;
      //初始状态,why ? 因为默认的时候有35dp的空白！
      if (scrollX == -TrimVideoUtil.RECYCLER_VIEW_PADDING) {
        scrollPos = 0;
      } else {
        // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
        if (mVideoView != null && mVideoView.isPlaying()) {
          //videoPause();
        }
        isSeeking = true;
        scrollPos = (long) (mAverageMsPx * (TrimVideoUtil.RECYCLER_VIEW_PADDING + scrollX));
        mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
        mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;
        Log.d(TAG, "onScrolled >>>> mLeftProgressPos = " + mLeftProgressPos);
        Log.d(TAG, "onScrolled >>>> mRightProgressPos = " + mRightProgressPos);
        mRedProgressBarPos = mLeftProgressPos;
        if (mVideoView.isPlaying()) {
          mVideoView.pause();
          setPlayPauseViewIcon(false);
        }
        mPositionIcon.setVisibility(GONE);
        seekTo(mLeftProgressPos);
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
        mRangeSeekBarView.invalidate();
      }
      lastScrollX = scrollX;
    }
  };

  /**
   * 水平滑动了多少px
   */
  private int calcScrollXDistance() {
    LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
    int position = layoutManager.findFirstVisibleItemPosition();
    View firstVisibleChildView = layoutManager.findViewByPosition(position);
    int itemWidth = firstVisibleChildView.getWidth();
    return (position) * itemWidth - firstVisibleChildView.getLeft();
  }

  private ValueAnimator animator;

  private void playingAnim() {
    mPositionIcon.clearAnimation();
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    anim();
    handler.removeCallbacks(run);
    handler.post(run);
  }

  private void pauseAnim() {
    mPositionIcon.clearAnimation();
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
  }

  private void anim() {
    if (mPositionIcon.getVisibility() == View.GONE) {
      mPositionIcon.setVisibility(View.VISIBLE);
    }
    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPositionIcon.getLayoutParams();
    int start = (int) (TrimVideoUtil.RECYCLER_VIEW_PADDING + (mRedProgressBarPos/*mVideoView.getCurrentPosition()*/ - scrollPos) * averagePxMs);
    int end = (int) (TrimVideoUtil.RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs);
    animator = ValueAnimator.ofInt(start, end).setDuration((mRightProgressPos - scrollPos) - (mRedProgressBarPos/*mVideoView.getCurrentPosition()*/ - scrollPos));
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        mRedProgressBarPos = (int) animation.getAnimatedValue();
        params.leftMargin = (int) animation.getAnimatedValue();
        mPositionIcon.setLayoutParams(params);
        Log.d(TAG, "----onAnimationUpdate--->>>>>>>" + mRedProgressBarPos);
      }
    });
    animator.start();
  }

  private Handler handler = new Handler();
  private Runnable run = new Runnable() {

    @Override public void run() {
      updateVideoProgress();
      handler.post(run);
    }
  };

  private void updateVideoProgress() {
    long currentPosition = mVideoView.getCurrentPosition();
    Log.d(TAG, "updateVideoProgress currentPosition = " + currentPosition);
    if (currentPosition >= (mRightProgressPos)) {
      mRedProgressBarPos = mLeftProgressPos;
      mPositionIcon.clearAnimation();
      if (animator != null && animator.isRunning()) {
        animator.cancel();
      }
      onPause();
    }
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
