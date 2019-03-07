package com.weibinhwb.customview;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by weibin on 2019/3/5
 */


public class CustomView extends ViewGroup {

    private CardView mForegroundView, mBackgroundView;
    private int mCardToParentLeft = 20;
    private int mCardToParentRight = 20;
    private int mCardToParentTop = 20;
    private int mCardToParentBottom = 20;
    private int mCardToCardDelta = 20;
    private boolean mIsReceiveTouchEvent = true;
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
        mForegroundView = new CardView(getContext());
        mBackgroundView = new CardView(getContext());
        LayoutParams foregroundParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LayoutParams backgroundParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mForegroundView.setLayoutParams(foregroundParams);
        mBackgroundView.setLayoutParams(backgroundParams);

        LayoutParams ivParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ImageView iv1 = new ImageView(getContext());
        iv1.setLayoutParams(ivParams);
        ImageView iv2 = new ImageView(getContext());
        iv2.setLayoutParams(ivParams);

        iv1.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image1));
        iv2.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image2));
        iv1.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv2.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mForegroundView.addView(iv1);
        mBackgroundView.addView(iv2);

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

    //拦截点击事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsReceiveTouchEvent;
    }

    /*
     * return true 事件被处理了
     * */

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
                    int distanceToOriginX = view.getLeft();
                    int slop = (getRight() - getLeft()) / 4;
                    if (Math.abs(distanceToOriginX) > slop) {
                        deleteViewAnimation(view, distanceToOriginX);
                        enlargeViewAnimation(mBackgroundView);
                    } else {
                        regressViewAnimation(view);
                    }
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

    /*
     * 需要更新布局、更新内容数据
     * */
    private void renewState(View view) {
        removeAllViews();
        addView(view);
        addView(mBackgroundView);
        mForegroundView = mBackgroundView;
        mBackgroundView = (CardView) view;
        mIsReceiveTouchEvent = true;
    }

    private void deleteViewAnimation(final View view, int values) {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(view);
        animator.setPropertyName("translationX");
        animator.setFloatValues(values * 5);
        animator.setDuration(800);
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

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLeft(mCardToParentLeft);
                view.setTop(mCardToParentTop);
                view.setRight(view.getMeasuredWidth() - mCardToParentRight);
                view.setBottom(view.getMeasuredHeight() - mCardToParentBottom);
                view.setTranslationX(0);
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
