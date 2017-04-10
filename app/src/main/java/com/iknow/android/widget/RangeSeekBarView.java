package com.iknow.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.iknow.android.R;
import com.iknow.android.interfaces.OnRangeSeekBarListener;

import java.util.ArrayList;
import java.util.List;

import iknow.android.utils.DateUtil;
import iknow.android.utils.DeviceUtil;
import iknow.android.utils.UnitConverter;


public class RangeSeekBarView extends View {

    private static final String TAG = RangeSeekBarView.class.getSimpleName();
    private static final int paddingTop = UnitConverter.dpToPx(15);
    private static final int TextPostionX = UnitConverter.dpToPx(10);
    private static final int deviceWidth = DeviceUtil.getDeviceWidth() - UnitConverter.dpToPx(12);
    private int mHeightTimeLine;
    private List<Thumb> mThumbs;
    private List<OnRangeSeekBarListener> mListeners;
    private float mMaxWidth;
    private float mThumbWidth;
    private float mThumbHeight;
    private int mViewWidth;
    private float mPixelRangeMin;
    private long mPixelRangeMax;
    private int mDuration;

    private long mStartPosition = 0;
    private long mEndPosition = 0;

    private final Paint mTopBottom = new Paint();
    private final Paint mShadow = new Paint();
    private final Paint mLine = new Paint();
    private final Paint mTextPaintL = new Paint();
    private final Paint mTextPaintR = new Paint();
    private int thumbMargin = UnitConverter.dpToPx(3);

    public RangeSeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mThumbs = Thumb.initThumbs(getResources());
        mThumbWidth = Thumb.getWidthBitmap(mThumbs);
        mThumbHeight = Thumb.getHeightBitmap(mThumbs);

        mHeightTimeLine = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);

        setFocusable(true);
        setFocusableInTouchMode(true);

        int topBottom = getContext().getResources().getColor(R.color.top_bottom);
        mTopBottom.setAntiAlias(true);
        mTopBottom.setColor(topBottom);

        int shadowColor = getContext().getResources().getColor(R.color.shadow_color);
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);

        int lineColor = getContext().getResources().getColor(R.color.line_color);
        mLine.setAntiAlias(true);
        mLine.setColor(lineColor);
        mLine.setAlpha(200);

        mTextPaintL.setStrokeWidth(3);
        mTextPaintL.setARGB(255, 51, 51, 51);
        mTextPaintL.setTextSize(28);
        mTextPaintL.setAntiAlias(true);
        mTextPaintL.setColor(Color.parseColor("#444444"));
        mTextPaintL.setTextAlign(Paint.Align.LEFT);

        mTextPaintR.setStrokeWidth(3);
        mTextPaintR.setARGB(255, 51, 51, 51);
        mTextPaintR.setTextSize(28);
        mTextPaintR.setAntiAlias(true);
        mTextPaintR.setColor(Color.parseColor("#444444"));
        mTextPaintR.setTextAlign(Paint.Align.RIGHT);
    }

    public void initThumbForRangeSeekBar(int duration, long pixelRangeMax) {
        mDuration = duration;
        mPixelRangeMax = pixelRangeMax;
        onCreate(this, currentThumb, getThumbValue(currentThumb));
    }

    public void initMaxWidth() {
        mMaxWidth = mThumbs.get(1).getPos() - mThumbs.get(0).getPos();
        onSeekStop(this, 0, mThumbs.get(0).getVal());
        onSeekStop(this, 1, mThumbs.get(1).getVal());
    }

    public void setStartEndTime(long start, long end) {
        this.mStartPosition = start / 1000;
        this.mEndPosition = end / 1000;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + (int) mThumbWidth;
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int minH = getPaddingBottom() + getPaddingTop() + mHeightTimeLine + UnitConverter.dpToPx(2) * 2 + paddingTop;
        int viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(mViewWidth, viewHeight);
        mPixelRangeMin = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawThumbs(canvas);
        drawTopBottom(canvas);
        drawVideoTime(canvas);
    }

    private int shadowMargin = UnitConverter.dpToPx(5);

    private void drawShadow(Canvas canvas) {
        if (!mThumbs.isEmpty()) {

            for (Thumb th : mThumbs) {
                if (th.getIndex() == 0) {
                    final float x = th.getPos();
                    if (x > mPixelRangeMin) {
                        Rect mRect = new Rect((int) mThumbWidth / 2, paddingTop, (int) (x + mThumbWidth / 2), mHeightTimeLine + paddingTop);
                        canvas.drawRect(mRect, mShadow);
                    }
                } else {
                    Rect mRect = null;
                    final float x = th.getPos() + shadowMargin;
                    if (mPixelRangeMax < deviceWidth) {
                        mRect = new Rect((int) x, paddingTop, (int) (mPixelRangeMax), mHeightTimeLine + paddingTop);
                    } else if (mPixelRangeMax >= deviceWidth)
                        mRect = new Rect((int) x, paddingTop, (int) (deviceWidth + shadowMargin), mHeightTimeLine + paddingTop);

                    if (mRect != null) canvas.drawRect(mRect, mShadow);

                }
            }
        }
    }

    private void drawThumbs(Canvas canvas) {
        if (!mThumbs.isEmpty()) {
            for (Thumb th : mThumbs) {
                if (th.getIndex() == 0) {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() + getPaddingLeft(), paddingTop, null);

                } else {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() - getPaddingRight() - thumbMargin, paddingTop, null);
                }
            }
        }
    }

    private int drawTop = UnitConverter.dpToPx(6);

    private void drawTopBottom(Canvas canvas) {

        Rect topRect = new Rect((int) getThumbs().get(0).getPos() + drawTop, paddingTop, (int) (getThumbs().get(1).getPos() - getPaddingLeft() + thumbMargin), UnitConverter.dpToPx(2) + paddingTop);
        canvas.drawRect(topRect, mTopBottom);

        final float x = getThumbs().get(0).getPos() + drawTop;
        Rect bottomRect = new Rect((int) x, mHeightTimeLine + paddingTop, (int) (getThumbs().get(1).getPos() - getPaddingLeft() + thumbMargin), mHeightTimeLine + UnitConverter.dpToPx(2) + paddingTop);
        canvas.drawRect(bottomRect, mTopBottom);
    }

    private int textPosMargin = UnitConverter.dpToPx(6);

    private void drawVideoTime(Canvas canvas) {

        String leftThumbsTime = DateUtil.convertSecondsToTime(mStartPosition);
        String rightThumbsTime = DateUtil.convertSecondsToTime(mEndPosition);

        canvas.drawText(leftThumbsTime, getThumbs().get(0).getPos() + textPosMargin, TextPostionX, mTextPaintL);
        canvas.drawText(rightThumbsTime, getThumbs().get(1).getPos() + textPosMargin, TextPostionX, mTextPaintR);
    }

    private int currentThumb = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final Thumb mThumb;
        final Thumb mThumb2;
        final float coordinateX = ev.getX();
        final float coordinateY = ev.getY();
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // Remember where we started
                currentThumb = getClosestThumb(coordinateX);

                if (currentThumb == -1) {
                    return false;
                }

                mThumb = mThumbs.get(currentThumb);
                mThumb.setLastTouchX(coordinateX);
                mThumb.setLastTouchY(coordinateY);
                onSeekStart(this, currentThumb, mThumb.getVal());
                return true;
            }
            case MotionEvent.ACTION_UP: {

                if (currentThumb == -1) {
                    return false;
                }

                mThumb = mThumbs.get(currentThumb);
                onSeekStop(this, currentThumb, mThumb.getVal());
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                mThumb = mThumbs.get(currentThumb);
                mThumb2 = mThumbs.get(currentThumb == 0 ? 1 : 0);
                // Calculate the distance moved
                final float dx = coordinateX - mThumb.getLastTouchX();
                final float newX = mThumb.getPos() + dx;
                if (currentThumb == 0) {

                    if ((newX + mThumb.getWidthBitmap()) >= mThumb2.getPos()) {
                        mThumb.setPos(mThumb2.getPos() - mThumb.getWidthBitmap());
                    } else if (newX <= mPixelRangeMin) {
                        mThumb.setPos(mPixelRangeMin);
                    } else {
                        //Check if thumb is not out of max width
                        checkPositionThumb(mThumb, mThumb2, dx, true);
                        // Move the object
                        mThumb.setPos(mThumb.getPos() + dx);

                        // Remember this touch position for the next move event
                        mThumb.setLastTouchX(coordinateX);
                        mThumb.setLastTouchY(coordinateY);
                    }

                } else {
                    if (newX <= mThumb2.getPos() + mThumb2.getWidthBitmap()) {
                        mThumb.setPos(mThumb2.getPos() + mThumb.getWidthBitmap());
                    } else if (newX >= deviceWidth) {
                        mThumb.setPos(deviceWidth);
                    } else if (newX >= mPixelRangeMax) {
                        mThumb.setPos(mPixelRangeMax);
                    } else {
                        //Check if thumb is not out of max width
                        checkPositionThumb(mThumb2, mThumb, dx, false);
                        // Move the object
                        mThumb.setPos(mThumb.getPos() + dx);
                        // Remember this touch position for the next move event
                        mThumb.setLastTouchX(coordinateX);
                        mThumb.setLastTouchY(coordinateY);
                    }
                }

                setThumbPos(currentThumb, mThumb.getPos());

                // Invalidate to request a redraw
                invalidate();
                return true;
            }
        }
        return false;
    }

    private void checkPositionThumb(Thumb mThumbLeft, Thumb mThumbRight, float dx, boolean isLeftMove) {
        if (isLeftMove && dx < 0) {
            if ((mThumbRight.getPos() - (mThumbLeft.getPos() + dx)) > mMaxWidth) {
                mThumbRight.setPos(mThumbLeft.getPos() + dx + mMaxWidth);
                setThumbPos(1, mThumbRight.getPos());
            }
        } else if (!isLeftMove && dx > 0) {
            if (((mThumbRight.getPos() + dx) - mThumbLeft.getPos()) > mMaxWidth) {
                mThumbLeft.setPos(mThumbRight.getPos() + dx - mMaxWidth);
                setThumbPos(0, mThumbLeft.getPos());
            }
        }
    }

    /**
     * 计算游标的值
     *
     * @param index
     */
    private void calculateThumbValue(int index) {
        if (index < mThumbs.size() && !mThumbs.isEmpty()) {
            Thumb th = mThumbs.get(index);
            th.setVal(pixelToScale(index, th.getPos()));
            onSeek(this, index, th.getVal());
        }
    }

    private float pixelToScale(int index, float pixelValue) {
        if (index == 0) {
            return pixelValue;
        } else {
            return pixelValue;
        }
    }

    private void calculateThumbPos(int index) {
        if (index < mThumbs.size() && !mThumbs.isEmpty()) {
            Thumb th = mThumbs.get(index);
            th.setPos(scaleToPixel(index, th.getVal()));
        }
    }

    private float scaleToPixel(int index, float scaleValue) {
        float px = scaleValue;
        if (index == 0) {
            float pxThumb = 0;
            return px - pxThumb;
        } else {
            float pxThumb = 0;
            return px + pxThumb;
        }
    }

    private float getThumbValue(int index) {
        return mThumbs.get(index).getVal();
    }

    public void setThumbValue(int index, float value) {
        mThumbs.get(index).setVal(value);
        calculateThumbPos(index);
        // Tell the view we want a complete redraw
        invalidate();
    }

    /**
     * 设置游标的位置
     *
     * @param index
     * @param pos
     */
    private void setThumbPos(int index, float pos) {
        mThumbs.get(index).setPos(pos);
        calculateThumbValue(index);
        invalidate();
    }

    private int getClosestThumb(float coordinate) {
        int closest = -1;
        if (!mThumbs.isEmpty()) {
            for (int i = 0; i < mThumbs.size(); i++) {
                // Find thumb closest to x coordinate
                final float tcoordinate = mThumbs.get(i).getPos() + mThumbWidth;
                if (coordinate >= mThumbs.get(i).getPos() && coordinate <= tcoordinate) {
                    closest = mThumbs.get(i).getIndex();
                }
            }
        }
        return closest;
    }

    public void addOnRangeSeekBarListener(OnRangeSeekBarListener listener) {

        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }

        mListeners.add(listener);
    }

    private void onCreate(RangeSeekBarView rangeSeekBarView, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarListener item : mListeners) {
            item.onCreate(rangeSeekBarView, index, value);
        }
    }

    private void onSeek(RangeSeekBarView rangeSeekBarView, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarListener item : mListeners) {
            item.onSeek(rangeSeekBarView, index, value);
        }
    }

    private void onSeekStart(RangeSeekBarView rangeSeekBarView, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarListener item : mListeners) {
            item.onSeekStart(rangeSeekBarView, index, value);
        }
    }

    private void onSeekStop(RangeSeekBarView rangeSeekBarView, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarListener item : mListeners) {
            item.onSeekStop(rangeSeekBarView, index, value);
        }
    }

    private List<Thumb> getThumbs() {
        return mThumbs;
    }
}
