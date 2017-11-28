package com.yio.customview.pager1;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.yio.customview.R;


/**
 * The base cycle wheel menu layout with cursor
 *
 * @author yio
 * @attr ref wheelSelectedAngle
 * @attr ref wheelPaddingRadio
 * @attr ref wheelCenterRadio
 * @attr ref wheelItemRadio
 * @attr ref wheelFlingValue
 * @attr ref wheelCursorColor
 * @attr ref wheelCursorHeight
 */
public class CursorWheelLayout
        extends ViewGroup {
    private static final String TAG = "CircleMenuLayout";
    /**
     * size of menu item relative to parent
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;

    /**
     * ignore the origin padding ,real padding size determine by parent size
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;


    private static final int INVALID_POSITION = -1;


    private static final float DEFAULT_SELECTED_ANGLE = 0;

    /**
     * Angle a touch can wander before we think the user is flinging
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     *
     */
    private static final int NOCLICK_VALUE = 3;
    /**
     * default cursor color
     */
    public static final int ITEM_ROTATE_MODE_NONE = 0;

    public static final int ITEM_ROTATE_MODE_INWARD = 1;

    public static final int ITEM_ROTATE_MODE_OUTWARD = 2;


    /**
     * CircleMenuLayout 's size
     */
    private int mRootDiameter;

    /**
     * Angle a touch can wander before we think the user is flinging
     */
    private int mFlingableValue = FLINGABLE_VALUE;


    private float mPadding;


    private double mStartAngle = 0;
    /**
     * menu 's source data
     */
    private CycleWheelAdapter mWheelAdapter;

    /**
     *
     */
    private int mMenuItemCount;

    /**
     *
     */
    private float mTmpAngle;

    /**
     *
     */
    private long mDownTime;

    /**
     * weather is fling now
     */
    private boolean mIsFling;
    /**
     *
     */
    private boolean mIsDraging;

    /**
     */
    private float mLastX;
    private float mLastY;

    /**
     * [0,360)
     */
    private double mSelectedAngle = DEFAULT_SELECTED_ANGLE;
    /**
     * The currently selected item's child.
     */
    private View mSelectedView;
    /**
     * The position of the selected View
     */
    private int mSelectedPosition = INVALID_POSITION;

    /**
     * The temp selected item's child.
     */
    private View mTempSelectedView;

    /**
     * The position of the temp selected item's child.
     */
    private int mTempSelectedPosition = INVALID_POSITION;


    /**
     * 判断是否需要滚动到最中间，某些时候由于选中角度和布局中心的角度一样，所以不需要在进行一次移动
     */
    private boolean mNeedSlotIntoCenter;

    private FlingRunnable mFlingRunnable = new FlingRunnable();

    /**
     * callback on menu item being click
     */
    private OnMenuItemClickListener mOnMenuItemClickListener;

    /**
     * callback on menu item being selected
     */
    private OnMenuSelectedListener mOnMenuSelectedListener;

    private boolean mIsFirstLayout = true;

    private float mMenuRadioDimension;//child占parent直径的比例,默认1/4
    private float mPaddingRadio;//child距离parent的边距
    private int mItemRotateMode = ITEM_ROTATE_MODE_NONE;

    private Paint mPaint;

    public CursorWheelLayout(Context context) {
        this(context, null);
    }

    public CursorWheelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CursorWheelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWheel(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CursorWheelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWheel(context, attrs);
    }

    private void initWheel(Context context, AttributeSet attrs) {
        setWillNotDraw(false);//這樣就可以執行onDraw方法了
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        setPadding(0, 0, 0, 0);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CursorWheelLayout);
            mSelectedAngle = ta.getFloat(R.styleable.CursorWheelLayout_wheelSelectedAngle, DEFAULT_SELECTED_ANGLE);
            if (mSelectedAngle > 360) {
                mSelectedAngle %= 360;
            }
            mStartAngle = mSelectedAngle;
            mMenuRadioDimension = ta.getFloat(R.styleable.CursorWheelLayout_wheelItemRadio, RADIO_DEFAULT_CHILD_DIMENSION);
            mPaddingRadio = ta.getFloat(R.styleable.CursorWheelLayout_wheelPaddingRadio, RADIO_PADDING_LAYOUT);
            mItemRotateMode = ta.getInt(R.styleable.CursorWheelLayout_wheelItemRotateMode, ITEM_ROTATE_MODE_NONE);
            ta.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getDefaultWidth();
        int desiredHeight = desiredWidth;
        //与期望的宽高值比较，确定自己的宽高值
        int widthSpec = resolveSizeAndState(desiredWidth, widthMeasureSpec);
        int heightSpec = resolveSizeAndState(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(widthSpec, heightSpec);
        //获取圆形的直径
        mRootDiameter = Math.max(getMeasuredWidth(), getMeasuredHeight());

        final int count = getChildCount();
        // menu item 's size width and height
        int childWidth = (int) (mRootDiameter * mMenuRadioDimension);
        int childHeight = childWidth * 3;
        // menu item 's MeasureSpec
        int childMode = MeasureSpec.EXACTLY;
        //测量child的大小
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }
            int makeWidthMeasureSpec;//child测量宽度
            int makeHeightMeasureSpec;//child测量高度
            makeWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth,
                    childMode);

            makeHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight,
                    childMode);
            child.measure(makeWidthMeasureSpec, makeHeightMeasureSpec);
        }
        //内边距
        mPadding = mPaddingRadio * mRootDiameter;
    }

    private int resolveSizeAndState(int desireSize, int measureSpec) {
        int result = desireSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = desireSize;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < desireSize) {
                    result = specSize;
                } else {
                    result = desireSize;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    /**
     * @param mOnMenuItemClickListener
     */
    public void setOnMenuItemClickListener(
            OnMenuItemClickListener mOnMenuItemClickListener) {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }

    public void setOnMenuSelectedListener(OnMenuSelectedListener onMenuSelectedListener) {
        mOnMenuSelectedListener = onMenuSelectedListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDiameter = mRootDiameter;//布局直径
        int layoutRadial = (int) (layoutDiameter / 2.0);//布局半径

        final int childCount = getChildCount();

        int left, top;
        int childCenterX, childCenterY;
        // size of menu item
//        int cWidth = (int) (layoutDiameter * mMenuRadioDimension);

        float angleDelay;
        angleDelay = 360 / (getChildCount());

        //angle diff [0,360)
        double minimumAngleDiff = -1;
        double angleDiff;
        double includedAngle = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            int makeWidthMeasureSpec = child.getMeasuredWidth();
            int makeHeightMeasureSpec = child.getMeasuredHeight();
            if (child.getVisibility() == GONE) {
                continue;
            }

            mStartAngle %= 360;
            includedAngle = mStartAngle;
//            }
            //menu 's angle relative to 0°
            child.setTag(R.id.id_wheel_view_angle, mStartAngle);
            angleDiff = Math.abs(mSelectedAngle - includedAngle);
            angleDiff = angleDiff >= 180 ? 360 - angleDiff : angleDiff;
            //find the intentional selected item
            if (minimumAngleDiff == -1 || minimumAngleDiff > angleDiff) {
                minimumAngleDiff = angleDiff;
                mTempSelectedView = child;
                mTempSelectedPosition = i;
                //allowable error
                mNeedSlotIntoCenter = ((int) minimumAngleDiff) != 0;
            }
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "onLayout(),cur position:,i:" + (i - 1) + ",angleDiff：" + angleDiff + ",angle:" + mStartAngle);
//                Log.d(TAG, "onLayout(),mTempSelectedPosition:" + mTempSelectedPosition + ",mNeedSlotIntoCenter:" + mNeedSlotIntoCenter + ",minimumAngleDiff：" + minimumAngleDiff);
//            }

            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadial - makeWidthMeasureSpec / 2 - mPadding;

            // {tmp*cos(a)-1/2*width}即menu item相对中心点的横坐标
            left = layoutRadial
                    + (int) Math.round(tmp
                    * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f
                    * makeWidthMeasureSpec);

            //{tmp*sin(a)-1/2*height}即menu item相对中心点的纵坐标
            top = layoutRadial
                    + (int) Math.round(tmp
                    * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f
                    * makeHeightMeasureSpec);

            child.layout(left, top, left + makeWidthMeasureSpec, top + makeHeightMeasureSpec);
            float angel;
            switch (mItemRotateMode) {
                case ITEM_ROTATE_MODE_NONE:
                    angel = 0;
                    break;
                case ITEM_ROTATE_MODE_INWARD:
                    angel = (float) (-90 + mStartAngle);
                    break;
                case ITEM_ROTATE_MODE_OUTWARD:
                    angel = (float) (90 + mStartAngle);
                    break;
                default:
                    angel = 0;
                    break;
            }
            child.setPivotX(makeWidthMeasureSpec / 2.0f);
            child.setPivotY(makeHeightMeasureSpec / 2.0f);
            child.setRotation(angel);
            mStartAngle += angleDelay;
        }
        if (mIsFirstLayout) {
            mIsFirstLayout = false;
            scrollIntoSlots();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //画外圈圆弧
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getMeasuredWidth() / 4);
        mPaint.setColor(0XFF1d1d1d);
        float left = getMeasuredWidth() / 6, right = getMeasuredWidth() - getMeasuredWidth() / 6;
        float top = left, bottom = getMeasuredWidth() - getMeasuredWidth() / 6;
        RectF oval = new RectF(left, top, right, bottom);
        canvas.drawArc(oval, 180, 180, false, mPaint);
        //画红色矩形
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0XFFeb5E51);
        RectF rect = new RectF(getMeasuredWidth() / 2 - getMeasuredWidth() / 12, 0, getMeasuredWidth() / 2 + getMeasuredWidth() / 12, getMeasuredHeight());
        canvas.drawRoundRect(rect,
                15, //x轴的半径
                15, //y轴的半径
                mPaint);
        //画中心半圆
        mPaint.setColor(0XFF3E3837);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight(), getMeasuredWidth() / 5, mPaint);
    }

    @Override
    public void requestLayout() {
        mIsFirstLayout = true;
        super.requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;
                mIsDraging = false;
                if (mIsFling) {
                    removeCallbacks(mFlingRunnable);
                    mIsFling = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                /**
                 * 获得开始的角度
                 */
                final float start = getAngle(mLastX, mLastY);
                /**
                 * 获得当前的角度
                 */
                final float end = getAngle(x, y);

                // 如果是一、四象限，则直接(end-start)代表角度改变值（正是上移动负是下拉）
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else
                // 二、三象限，(start - end)代表角度改变值
                {
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }
                mIsDraging = true;
                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000
                        / (System.currentTimeMillis() - mDownTime);
                // 如果达到该值认为是快速移动
                if (Math.abs(anglePerSecond) > mFlingableValue && !mIsFling) {
                    mFlingRunnable.startUsingVelocity(anglePerSecond);
                    return true;
                }
                mIsFling = false;
                mIsDraging = false;
                mFlingRunnable.stop(false);
                scrollIntoSlots();
                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 如果触摸事件交由自己处理，都接受好了
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     * 根据触摸的位置，计算角度返
     *
     * @param xTouch
     * @param yTouch
     * @return 返回的是普通数字所代表的最小夹角的角度值，如45度就是45而不是1/4×PI;
     * 如果是钝角，如315度，返回结果是-45;
     * 而需要注意的是在同水平方向返回值都为0，垂直方向为90/-90
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mRootDiameter / 2d);
        double y = yTouch - (mRootDiameter / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mRootDiameter / 2);
        int tmpY = (int) (y - mRootDiameter / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }


    public void setAdapter(CycleWheelAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("Can not set a null adbapter to CursorWheelLayout!!!");
        }

        if (mWheelAdapter != null) {
            Log.w(TAG, "setAdapter() already called!");
        }
        mWheelAdapter = adapter;
        mMenuItemCount = adapter.getCount();
        addMenuItems();
    }


    /**
     * add menu item to this layout
     */
    private void addMenuItems() {
        if (mWheelAdapter == null || mWheelAdapter.getCount() == 0) {
            throw new IllegalArgumentException("Empty menu source!");
        }
        if (mWheelAdapter.getCount() != mMenuItemCount) {
            throw new IllegalArgumentException("MenuSource has been modified after setting into CursorWheelLayout!");
        }
        View view;
        for (int i = 0; i < mMenuItemCount; i++) {
            view = mWheelAdapter.getView(this, i);
            //it will be ignore the origin onClickListener
            view.setOnClickListener(new InnerClickListener(i));
            addView(view);
        }
    }

    /**
     * @param mPadding
     */
    public void setPadding(float mPadding) {
        this.mPadding = mPadding;
        invalidate();
    }

    /**
     * @return 设备宽高中的较小值
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }


    private void endFling(boolean scrollIntoSlots) {
        mIsDraging = false;
        mIsFling = false;
        if (scrollIntoSlots) {
            scrollIntoSlots();
        }
    }

    private View mLastSelectedView;

    /**
     * Scrolls the items so that the selected item is in its 'slot' (its center
     * is the gallery's center).
     */
    private void scrollIntoSlots() {
        if (mIsDraging || mIsFling) {
            return;
        }

        if (getChildCount() == 0 || mTempSelectedView == null) {
            return;
        }

        if (!mNeedSlotIntoCenter) {
            if (mSelectedView != mTempSelectedView || mSelectedPosition != mTempSelectedPosition) {
                onInnerItemUnselected(mSelectedView);
                mSelectedView = mTempSelectedView;
                onInnerItemSelected(mSelectedView);
                mSelectedPosition = mTempSelectedPosition;
                mTempSelectedView = null;
                mTempSelectedPosition = INVALID_POSITION;
                selectionChangeCallback();
                mLastSelectedView = mSelectedView;
            }

        } else {
            double angle = (double) mTempSelectedView.getTag(R.id.id_wheel_view_angle);
            if (angle > 360) {
                throw new IllegalStateException("includedAngle>180, may be something wrong with calculate angle on layout");
            }
            double diff = Math.abs(mSelectedAngle - angle);
            diff = diff >= 180 ? 360 - diff : diff;
            double diagonal = (angle + 180) % 360;
            boolean clockWise;
            if (diagonal < angle) {
                clockWise = (mSelectedAngle <= diagonal || mSelectedAngle >= angle);
            } else {
                clockWise = (mSelectedAngle >= angle && mSelectedAngle <= diagonal);
            }
            mFlingRunnable.stop(false);
            mFlingRunnable.startUsingAngle(diff * (clockWise ? 1 : -1));
        }
    }


    /**
     * do some callback when selected position change
     */
    private final void selectionChangeCallback() {
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "selectionChangeCallback() called with: mSelectedPosition" + mSelectedPosition);
//        }
        if (mOnMenuSelectedListener != null) {
            mOnMenuSelectedListener.onItemSelected(this, mSelectedView, mLastSelectedView, mSelectedPosition);
        }
    }


    /**
     * Responsible for fling behavior.
     */
    private class FlingRunnable implements Runnable {

        private static final int DEFAULT_REFRESH_TIME = 30;

        /**
         * 滑动速度
         */
        private float mAngelPerSecond;
        /**
         * 是否以旋转某个角度为目的
         */
        private boolean mStartUsingAngle;
        /**
         * 最终的角度
         */
        private double mEndAngle;
        /**
         * 需要旋转的角度
         */
        private double mSweepAngle;
        /**
         *
         */
        private boolean mBiggerBefore;
        /**
         * 记录下开始转动的时候的startAngle，因为{@link CursorWheelLayout#mStartAngle}属于[0,360),如果直接用来和{@link FlingRunnable#mEndAngle}比较大小就会比较麻烦了
         */
        private double mInitStarAngle;

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

        public FlingRunnable() {

        }

        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        /**
         * @param velocity
         */
        public void startUsingVelocity(float velocity) {
            mStartUsingAngle = false;
            startCommon();
            this.mAngelPerSecond = velocity;
            post(this);

        }

        /**
         * @param angle
         */
        public void startUsingAngle(double angle) {
            mStartUsingAngle = true;
            mSweepAngle = angle;
            mInitStarAngle = mStartAngle;
            mEndAngle = mSweepAngle + mInitStarAngle;
            mBiggerBefore = mInitStarAngle >= mEndAngle;
//            if (BuildConfig.DEBUG) {
//                Log.d(TAG, "startUsingAngle() called with: " + "angle = [" + angle + "]" + ",mStartAngle:" + mInitStarAngle + ",mEndAngle:" + mEndAngle);
//            }
            post(this);
        }

        public void run() {
            if (mMenuItemCount == 0) {
                endFling(true);
                return;
            }
            if (!mStartUsingAngle) {
                if ((int) Math.abs(mAngelPerSecond) < 20) {
                    endFling(true);
                    return;
                }
                mIsFling = true;
                mStartAngle = mStartAngle + (mAngelPerSecond / 30);
                mAngelPerSecond /= 1.0666F;
            } else {
                mStartAngle %= 360;
                if (Math.abs((int) (mEndAngle - mInitStarAngle)) == 0 || (mBiggerBefore && (mInitStarAngle < mEndAngle)) || (!mBiggerBefore && (mInitStarAngle > mEndAngle))) {
                    mNeedSlotIntoCenter = false;
                    endFling(true);
                    return;
                }
                mIsFling = true;
                double change = mSweepAngle / 5;
                mInitStarAngle += change;
                mStartAngle += change;
            }
            postDelayed(this, DEFAULT_REFRESH_TIME);
            requestLayout();
        }
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnMenuItemClickListener {

        void onItemClick(View view, int pos);

    }

    private class InnerClickListener implements OnClickListener {
        private final int mPosition;

        public InnerClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mSelectedView == v || mTempSelectedView == v) {
                return;
            }
            mFlingRunnable.stop(false);
            mIsDraging = false;
            mIsFling = false;
            mTempSelectedPosition = mPosition;
            mTempSelectedView = v;
            mNeedSlotIntoCenter = true;
            scrollIntoSlots();
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.onItemClick(v, mPosition);
            }
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * @param position
     */
    public void setSelection(final int position) {
        if (position > mMenuItemCount) {
            throw new IllegalArgumentException("Position:" + position + " is out of index!");
        }
        post(new Runnable() {
            @Override
            public void run() {
                int itemPosition = position;
                mFlingRunnable.stop(false);
                mIsDraging = false;
                mIsFling = false;
                mTempSelectedPosition = itemPosition;
                mTempSelectedView = getChildAt(itemPosition);
                mNeedSlotIntoCenter = true;
                scrollIntoSlots();
            }
        });
    }

    public void setSelectedAngle(double selectedAngle) {
        if (selectedAngle < 0) {
            return;
        }
        if (selectedAngle > 360) {
            selectedAngle %= 360;
        }
        mSelectedAngle = selectedAngle;
        requestLayout();
    }


    /**
     * to do whatever you want to perform the selected view
     *
     * @param v
     */
    protected void onInnerItemSelected(View v) {

    }

    /**
     * to do whatever you want to perform the unselected view
     *
     * @param v
     */
    protected void onInnerItemUnselected(View v) {

    }


    /**
     * callback when item selected
     */
    public interface OnMenuSelectedListener {

        void onItemSelected(CursorWheelLayout parent, View view, View lastSelectedView, int pos);
    }

    /**
     * An Adapter object acts as a bridge between an {@link CursorWheelLayout} and the
     * underlying data for that view. The Adapter provides access to the data items.
     * The Adapter is also responsible for making a {@link View} for
     * each item in the data set.
     */
    public interface CycleWheelAdapter {
        /**
         * How many menu items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        int getCount();

        /**
         * Get a View that displays the data at the specified position in the data set.
         *
         * @param parent
         * @param position
         * @return
         */
        View getView(View parent, int position);

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        Object getItem(int position);
    }

}
