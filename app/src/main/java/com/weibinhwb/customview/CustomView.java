package com.weibinhwb.customview;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by weibin on 2019/3/5
 */


public class CustomView extends ViewGroup {

    private CardView mForegroundView, mBackgroundView;
    private TextView mForegroundTv, mBackgroundTv;
    private int mCardToParentLeft = 20;
    private int mCardToParentRight = 20;
    private int mCardToParentTop = 20;
    private int mCardToParentBottom = 20;
    private int mCardToCardDelta = 20;
    private boolean mIsReceiveTouchEvent = true;
    private static final String TAG = CustomView.class.getSimpleName();

    private String[] mStrings = {"Android开发艺术探索", "Android第一行代码", "Android音视频开发", "Android设计模式", "Java编程思想"};
    private int[] mColors = {Color.GREEN, Color.LTGRAY, Color.YELLOW, Color.GRAY, Color.CYAN};
    private int mIndexOfString = 0;
    private int mIndexOfColor = 0;

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
        mForegroundView = new CardView(getContext());
        mBackgroundView = new CardView(getContext());
        mForegroundTv = new TextView(getContext());
        mBackgroundTv = new TextView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mForegroundView.setLayoutParams(params);
        mBackgroundView.setLayoutParams(params);
        mForegroundTv.setLayoutParams(params);
        mBackgroundTv.setLayoutParams(params);
        //设置TextView内容
        mForegroundTv.setText(mStrings[mIndexOfString++ % mStrings.length]);
        mForegroundTv.setBackgroundColor(mColors[mIndexOfColor++ % mColors.length]);
        mBackgroundTv.setText(mStrings[mIndexOfString++ % mStrings.length]);
        mBackgroundTv.setBackgroundColor(mColors[mIndexOfColor++ % mColors.length]);
        mForegroundTv.setGravity(Gravity.CENTER);
        mBackgroundTv.setGravity(Gravity.CENTER);
        mForegroundTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mBackgroundTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mForegroundView.addView(mForegroundTv);
        mBackgroundView.addView(mBackgroundTv);
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
            int ll = mCardToParentLeft;
            int rr = childView.getMeasuredWidth() - mCardToParentRight;
            int tt = mCardToParentTop;
            int bb = childView.getMeasuredHeight() - mCardToParentBottom;
            if (i == 1) {
                childView.layout(ll, tt, rr, bb);
            } else {
                childView.layout(ll + mCardToCardDelta, tt + mCardToCardDelta, rr - mCardToCardDelta, bb + mCardToCardDelta);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsReceiveTouchEvent;
    }


    private int mLastX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        View view = isInForegroundViewArea(x, y);
        if (view != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    int distanceX = x - mLastX;
                    int l = view.getLeft() + distanceX;
                    int t = mCardToParentTop;
                    int r = view.getRight() + distanceX;
                    int b = view.getMeasuredHeight() - mCardToParentBottom;
                    view.layout(l, t, r, b);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    int distanceToOriginX = view.getLeft();
                    int slop = (getRight() - getLeft()) / 5;
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    if (Math.abs(distanceToOriginX) > slop) {
                        deleteViewAnimation(view, distanceToOriginX);
                        enlargeViewAnimation(mBackgroundView);
                    } else {
                        regressViewAnimation(view);
                    }
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                    break;
                default:
                    break;
            }
            mLastX = x;
        }
        return mIsReceiveTouchEvent;
    }

    private View isInForegroundViewArea(float eventX, float eventY) {
        int l1 = mForegroundView.getLeft() + mForegroundView.getPaddingLeft();
        int t1 = mForegroundView.getTop() + mForegroundView.getPaddingTop();
        int r1 = mForegroundView.getRight() - mForegroundView.getPaddingRight();
        int b1 = mForegroundView.getBottom() - mForegroundView.getPaddingBottom();
        if (eventX > l1 && eventX < r1 && eventY > t1 && eventY < b1) {
            return mForegroundView;
        }
        return null;
    }

    private void renewState(View view) {
        removeAllViews();
        addView(view);
        addView(mBackgroundView);
        mForegroundView = mBackgroundView;
        mBackgroundView = (CardView) view;
        TextView textView = mBackgroundTv;
        mBackgroundTv = mForegroundTv;
        mForegroundTv = textView;
        mBackgroundTv.setText(mStrings[mIndexOfString++ % mStrings.length]);
        mBackgroundTv.setBackgroundColor(mColors[mIndexOfColor++ % mColors.length]);
        mIsReceiveTouchEvent = true;
    }

    private void deleteViewAnimation(final View view, int values) {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(view);
        animator.setPropertyName("translationX");
        animator.setFloatValues(values * 5);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsReceiveTouchEvent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationX(0);
                renewState(view);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void regressViewAnimation(final View view) {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(view);
        animator.setPropertyName("translationX");
        animator.setFloatValues(-view.getLeft() + mCardToParentLeft);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsReceiveTouchEvent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLeft(mCardToParentLeft);
                view.setTop(mCardToParentTop);
                view.setRight(view.getMeasuredWidth() - mCardToParentRight);
                view.setBottom(view.getMeasuredHeight() - mCardToParentBottom);
                view.setTranslationX(0);
                mIsReceiveTouchEvent = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void enlargeViewAnimation(final View view) {
        ValueAnimator animator = ValueAnimator.ofInt(1, 100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private IntEvaluator mEvaluator = new IntEvaluator();
            int ll = mCardToParentLeft;
            int rr = view.getMeasuredWidth() - mCardToParentRight;
            int tt = mCardToParentTop;
            int bb = view.getMeasuredHeight() - mCardToParentBottom;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int l = mEvaluator.evaluate(fraction, ll + mCardToCardDelta, ll);
                int r = mEvaluator.evaluate(fraction, rr - mCardToCardDelta, rr);
                int t = mEvaluator.evaluate(fraction, tt + mCardToCardDelta, tt);
                int b = mEvaluator.evaluate(fraction, bb + mCardToCardDelta, bb);
                view.layout(l, t, r, b);
            }
        });
        animator.setStartDelay(200);
        animator.start();
    }
}
