package com.weibinhwb.customview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by weibin on 2019/3/5
 */


public class CustomView extends ViewGroup {

    private ImageView mForegroundView;
    private ImageView mBackgroundView;
    private int mOriginLeftX, mOriginLeftY;
    private static final String TAG = CustomView.class.getSimpleName();

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mForegroundView = new ImageView(getContext());
        mBackgroundView = new ImageView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mForegroundView.setLayoutParams(params);
        mBackgroundView.setLayoutParams(params);
        mForegroundView.setPadding(20, 20, 20, 20);
        mBackgroundView.setPadding(30, 30, 10, 10);
        mForegroundView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image1));
        mBackgroundView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image2));
        mForegroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mBackgroundView);
        addView(mForegroundView);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int width = childView.getMeasuredWidth();
            int height = childView.getMeasuredHeight();
            childView.layout(0, 0, width, height);
        }
        mOriginLeftX = 0;
        mOriginLeftY = 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    //拦截点击事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /*
     * return true 事件被处理了
     * */

    private int mLastX, mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        View view = isInArea(x, y);
        if (view != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int distanceX = x - mLastX;
                    final int distanceY = y - mLastY;
                    int l = view.getLeft() + distanceX;
                    int t = view.getTop() + distanceY;
                    int r = view.getRight() + distanceX;
                    int b = view.getBottom() + distanceY;
                    view.layout(l, t, r, b);
                    break;
                case MotionEvent.ACTION_UP:
                    int distanceToOriginX = view.getLeft() - mOriginLeftX;
                    int distanceToOriginY = view.getTop() - mOriginLeftY;
                    int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                    if (Math.abs(distanceToOriginX) > slop || Math.abs(distanceToOriginY) > slop) {
                        renewState(view);
                    }
                    break;

            }
            mLastX = x;
            mLastY = y;
        }
        return true;
    }

    private View isInArea(float eventX, float eventY) {
        int l1 = mForegroundView.getLeft() + mForegroundView.getPaddingLeft();
        int t1 = mForegroundView.getTop() + mForegroundView.getPaddingTop();
        int r1 = mForegroundView.getRight() - mForegroundView.getPaddingRight();
        int b1 = mForegroundView.getBottom() - mForegroundView.getPaddingBottom();
        if (eventX > l1 && eventX < r1 && eventY > t1 && eventY < b1) {
            return mForegroundView;
        }
        return null;
    }

    /*
     * 需要更新布局、更新内容数据
     * */
    private void renewState(View view) {
        removeAllViews();
        addView(view);
        addView(mBackgroundView);
        mBackgroundView.setPadding(20, 20, 20, 20);
        mForegroundView.setPadding(30, 30, 10, 10);
        mForegroundView = mBackgroundView;
        mBackgroundView = (ImageView) view;
    }
}
