package com.dbf.studyandtest.myrecyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.logging.Logger;

public class CopyOppoWatcheLayoutManager extends RecyclerView.LayoutManager {
    private final String TAG = "MyLayout";
    private int itemHSize = -1;
    private int itemWSize = -1;
    private int hCound = 3;
    private int vCount = 0;
    private int screenlayoutCount = 0;
    private int mvOffsetCount = 0;

    private int mFirsItemPosition = 0;
    private int mLastItemPosition = 0;
    private int mCurrentOffset = 0;
    private int totalOffset = 0;
    private int mSetOffset = 0;
    private float minScale = 0.3f;
    private int beforOneLineStartOffset;

    public CopyOppoWatcheLayoutManager(Context context) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        detachAndScrapAttachedViews(recycler);
        mLastItemPosition = getItemCount() - 1;
        itemWSize = getHorizontalSpace() / hCound;

        if (vCount == 0) {
            vCount = getVerticalSpace() / itemWSize;
            itemHSize = itemWSize;
            mSetOffset = itemHSize;
        } else {
            if (itemHSize == 0) {
                itemHSize = getVerticalSpace() / vCount;
                mSetOffset = itemHSize;
            }
        }
        mSetOffset = 0;
        if (screenlayoutCount == 0) {
            screenlayoutCount = hCound * vCount;
        }
        if (mvOffsetCount == 0) {
            mvOffsetCount = (int) Math.ceil((getItemCount() + 0f) / hCound);
            int screenOffsetCount = (screenlayoutCount) / hCound;
            if (mvOffsetCount > screenOffsetCount) {
                mvOffsetCount = mvOffsetCount - screenOffsetCount;
            } else {
                mvOffsetCount = 0;
            }
            totalOffset = mvOffsetCount * itemHSize + itemHSize;
        }

        fill(recycler, state, 0);
        recycleChildren(recycler);
    }

    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        detachAndScrapAttachedViews(recycler);

        int leftOffset = getPaddingLeft();
        float yu = (mCurrentOffset + 0f) % itemHSize;
        float frac = yu / itemHSize;
        if (mCurrentOffset >= totalOffset) {
            frac = 1f;
        }
        int scrollY = (int) (itemHSize * frac);
        int viewTopOffset = getPaddingTop() - mSetOffset;
        viewTopOffset -= scrollY;//偏移量
        beforOneLineStartOffset = (int) -(itemHSize / 2 - itemHSize * minScale / 10);
        int lastEndLineStartOffset = (vCount - 1) * itemHSize - beforOneLineStartOffset;
        int lastScreenLayoutOffset = (vCount - 1) * itemHSize;
        int lastEndLineStartOffset2 = lastEndLineStartOffset;
        int oneLineStartOffset = 0;
        int fs = (int) Math.floor(Math.abs(mCurrentOffset) / itemHSize) * hCound;
        if (fs <= getItemCount() - 1) {
            mFirsItemPosition = fs;
        }

        int LastItemPosition = mFirsItemPosition + screenlayoutCount - 1 + hCound;
        if (LastItemPosition > getItemCount() - 1) {
            LastItemPosition = getItemCount() - 1;
        }
        mLastItemPosition = LastItemPosition;
        int buttomItemCount = (mLastItemPosition + 1) % hCound;
        if (buttomItemCount == 0) {
            buttomItemCount = hCound;
        }

        if (mFirsItemPosition >= hCound) {
            for (int k = mFirsItemPosition - hCound; k < mFirsItemPosition; k++) {
                View beforchild = recycler.getViewForPosition(k);
                addView(beforchild);
                measureChildWithMargins(beforchild, itemWSize * 2, itemHSize * 2);
                beforchild.setScaleX(minScale);
                beforchild.setScaleY(minScale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    beforchild.setTranslationZ(0);
                }
                layoutDecoratedWithMargins(beforchild, leftOffset, beforOneLineStartOffset, leftOffset + itemWSize, beforOneLineStartOffset + itemHSize);
                leftOffset += itemWSize;
            }
        }
        leftOffset = getPaddingLeft();
        for (int i = mFirsItemPosition; i <= mLastItemPosition; i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, itemWSize * 2, itemHSize * 2);
            if (i - mFirsItemPosition < hCound) {
//第一行
                float realScale = 1f - (1f - minScale) * frac;
                if (realScale > 1f) {
                    realScale = 1f;
                }
                child.setScaleX(realScale);
                child.setScaleY(realScale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    child.setTranslationZ(0);
                }
                oneLineStartOffset = 0;
                if (viewTopOffset < -(itemHSize + beforOneLineStartOffset)) {
                    oneLineStartOffset = viewTopOffset + (itemHSize + beforOneLineStartOffset);
                }
                if (viewTopOffset > 0) {
                    oneLineStartOffset = viewTopOffset;
                }
                if (leftOffset + itemWSize <= getHorizontalSpace() + getPaddingLeft()) { //当前行还排列的下
                    layoutDecoratedWithMargins(child, leftOffset, oneLineStartOffset, leftOffset + itemWSize, oneLineStartOffset + itemHSize);
                    leftOffset += itemWSize;
                } else {
                    oneLineStartOffset = 0;
                    if (viewTopOffset < -(itemHSize + beforOneLineStartOffset)) {
                        oneLineStartOffset = viewTopOffset + (itemHSize + beforOneLineStartOffset);
                    }
                    if (viewTopOffset > 0) {
                        oneLineStartOffset = viewTopOffset;
                    }
                    leftOffset = getPaddingLeft();
                    layoutDecoratedWithMargins(child, leftOffset, oneLineStartOffset, leftOffset + itemWSize, oneLineStartOffset + itemHSize);
                    leftOffset += itemWSize;
                }
            } else if (i > mLastItemPosition - buttomItemCount && i >= screenlayoutCount) {
                //画底部小圆
                float realScale = minScale + (1f - minScale) * frac;
                if (realScale < 0.3f) {
                    realScale = 0.3f;
                }

                child.setScaleX(realScale);
                child.setScaleY(realScale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    child.setTranslationZ(1);
                }
                lastEndLineStartOffset = viewTopOffset - beforOneLineStartOffset;
                if (viewTopOffset - beforOneLineStartOffset <= (vCount - 1) * itemHSize) {
                    lastEndLineStartOffset = (vCount - 1) * itemHSize;
                }
                if (lastEndLineStartOffset > (vCount - 1) * itemHSize - beforOneLineStartOffset) {
                    lastEndLineStartOffset = (vCount - 1) * itemHSize - beforOneLineStartOffset;
                }
                if (mCurrentOffset > totalOffset - itemHSize - 1) {
                    lastEndLineStartOffset = viewTopOffset + itemHSize;
                    child.setScaleX(1f);
                    child.setScaleY(1f);
                }
                if (leftOffset + itemWSize <= getHorizontalSpace() + getPaddingLeft()) { //当前行还排列的下
                    layoutDecoratedWithMargins(child, leftOffset, lastEndLineStartOffset, leftOffset + itemWSize, lastEndLineStartOffset + itemHSize);
                    leftOffset += itemWSize;
                } else {
                    leftOffset = getPaddingLeft();
                    lastEndLineStartOffset = viewTopOffset - beforOneLineStartOffset;
                    if (viewTopOffset - beforOneLineStartOffset <= (vCount - 1) * itemHSize) {
                        lastEndLineStartOffset = (vCount - 1) * itemHSize;
                    }
                    if (lastEndLineStartOffset > (vCount - 1) * itemHSize - beforOneLineStartOffset) {
                        lastEndLineStartOffset = (vCount - 1) * itemHSize - beforOneLineStartOffset;
                    }
                    if (mCurrentOffset > totalOffset - itemHSize - 1) {
                        lastEndLineStartOffset = viewTopOffset + itemHSize;
                        child.setScaleX(1f);
                        child.setScaleY(1f);
                    }
                    layoutDecoratedWithMargins(child, leftOffset, lastEndLineStartOffset, leftOffset + itemWSize, lastEndLineStartOffset + itemHSize);
                    leftOffset += itemWSize;
                }

            } else {
                lastScreenLayoutOffset = viewTopOffset;
                child.setScaleX(1f);
                child.setScaleY(1f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    child.setTranslationZ(1);
                }
                if (mCurrentOffset < 0) {
                    if (viewTopOffset > (vCount - 1) * itemHSize) {
                        lastScreenLayoutOffset = (vCount - 1) * itemHSize;
                        if (viewTopOffset - (itemHSize + beforOneLineStartOffset) >= (vCount - 1) * itemHSize) {
                            lastScreenLayoutOffset = viewTopOffset - (itemHSize + beforOneLineStartOffset);
                        }
                        float realScale = 1f + (1f - minScale) * frac;
                        if (realScale > 1f) {
                            realScale = 1f;
                        }
                        child.setScaleX(realScale);
                        child.setScaleY(realScale);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            child.setTranslationZ(2);
                        }
                    }

                }

                if (leftOffset + itemWSize <= getHorizontalSpace() + getPaddingLeft()) { //当前行还排列的下
                    layoutDecoratedWithMargins(child, leftOffset, lastScreenLayoutOffset, leftOffset + itemWSize, lastScreenLayoutOffset + itemHSize);
                    leftOffset += itemWSize;
                } else {
                    leftOffset = getPaddingLeft();
                    viewTopOffset += itemHSize;
                    lastScreenLayoutOffset = viewTopOffset;
                    if (mCurrentOffset < 0) {

                        if (viewTopOffset > (vCount - 1) * itemHSize) {
                            lastScreenLayoutOffset = (vCount - 1) * itemHSize;
                            if (viewTopOffset - (itemHSize + beforOneLineStartOffset) >= (vCount - 1) * itemHSize) {
                                lastScreenLayoutOffset = viewTopOffset - (itemHSize + beforOneLineStartOffset);
                            }
                            float realScale = 1f + (1f - minScale) * frac;
                            if (realScale > 1f) {
                                realScale = 1f;
                            }
                            child.setScaleX(realScale);
                            child.setScaleY(realScale);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                child.setTranslationZ(2);
                            }
                        }

                    }
                    layoutDecoratedWithMargins(child, leftOffset, lastScreenLayoutOffset, leftOffset + itemWSize, lastScreenLayoutOffset + itemHSize);
                    leftOffset += itemWSize;
                }
            }


        }
        leftOffset = getPaddingLeft();
        if (mLastItemPosition + hCound < getItemCount()) {
            for (int i = mLastItemPosition + 1; i <= mLastItemPosition + hCound; i++) {
                //画底部小圆
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, itemWSize * 2, itemHSize * 2);
                child.setScaleX(minScale);
                child.setScaleY(minScale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    child.setTranslationZ(0);
                }
                layoutDecoratedWithMargins(child, leftOffset, lastEndLineStartOffset2, leftOffset + itemWSize, lastEndLineStartOffset2 + itemHSize);
                leftOffset += itemWSize;
            }
        } else {
            for (int i = mLastItemPosition + 1; i < getItemCount(); i++) {
                //画底部小圆
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, itemWSize * 2, itemHSize * 2);
                child.setScaleX(minScale);
                child.setScaleY(minScale);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    child.setTranslationZ(0);
                }
                layoutDecoratedWithMargins(child, leftOffset, lastEndLineStartOffset2, leftOffset + itemWSize, lastEndLineStartOffset2 + itemHSize);
                leftOffset += itemWSize;
            }
        }
        return dy;
    }


    /**
     * 回收屏幕外需回收的Item
     */
    private void recycleChildren(RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeView(holder.itemView);
            recycler.recycleView(holder.itemView);
        }
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dy == 0 || getChildCount() == 0) {
            return 0;
        }
        int realOffset = dy;//实际滑动的距离
        if (realOffset < 0) {//上边界

        } else if (realOffset > 0) {//下边界

        }

        mCurrentOffset += realOffset;//累加实际滑动距离
        if (mCurrentOffset <= -itemHSize) {
            realOffset = 0;
            mCurrentOffset = (int) -itemHSize + 1;
        }
        if (mCurrentOffset >= totalOffset) {
            mCurrentOffset = totalOffset - 1;
            realOffset = 0;
        } else {
        }
        fill(recycler, state, realOffset);
//        offsetChildrenVertical(-realOffset);//滑动
        return realOffset;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_DRAGGING://手指触摸屏幕
                cancelAnmationtor();
                break;
            case RecyclerView.SCROLL_STATE_IDLE://列表停止滚动后
                smoothScrollToPosition(findShouldSelectPosition());
                break;
            default:
                break;
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        smoothScrollToPosition(position);
    }

    @Override
    public void scrollToPosition(int position) {
        mCurrentOffset += getScrollToPositionOffset(position);
        if (mCurrentOffset >= totalOffset) {
            mCurrentOffset = totalOffset - 1;
        }
        requestLayout();
    }

    private int getScrollToPositionOffset(int position) {
        position = (int) Math.ceil((position + 0f) / hCound);
        return position * itemHSize - Math.abs(mCurrentOffset);
    }

    private int findShouldSelectPosition() {
        if (itemHSize == 0) {
            return -1;
        }
        int remainder = -1;
        remainder = Math.abs(mCurrentOffset) % itemHSize;
        if (remainder > itemHSize / 2.0f) {
//下一项
            if (mFirsItemPosition + hCound <= getItemCount() - 1) {
                return mFirsItemPosition + hCound;
            }
        }

        return mFirsItemPosition;
    }

    private void smoothScrollToPosition(int position) {
        if (position >= 0 && position < getItemCount()) {
            startValueAnimation(position);
        }
    }

    /**
     * 自动选中动画
     */
    private ValueAnimator selectAnimator;
    private long autoSelectMinDuration = 100;
    private long autoSelectMaxDuration = 300;
    private float distance;

    private void startValueAnimation(int position) {
        cancelAnmationtor();
        Log.i("startValueAnimation", "position=" + position);
        distance = getScrollToPositionOffset(position);
        if (mCurrentOffset < 0) {
            distance = mCurrentOffset;
        } else if (mCurrentOffset > totalOffset - itemHSize) {
            distance = -(mCurrentOffset - (totalOffset - itemHSize));
        }
        long minDuration = autoSelectMinDuration;
        long maxDuration = autoSelectMaxDuration;
        long duration;
        float distanceFraction = Math.abs(distance) / itemHSize;
        if (distance <= itemHSize) {
            duration = (long) (minDuration + (maxDuration - minDuration) * distanceFraction);
        } else {
            duration = (long) (maxDuration * distanceFraction);
        }
        selectAnimator = ValueAnimator.ofFloat(0.0f, distance).setDuration(duration);
        selectAnimator.setInterpolator(new LinearInterpolator());
        final float anstartOffset = mCurrentOffset;
        selectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mCurrentOffset < 0) {
                    mCurrentOffset = (int) Math.floor(anstartOffset - (float) animation.getAnimatedValue());
                    if (mCurrentOffset >= totalOffset) {
                        mCurrentOffset = totalOffset - 1;
                    }
                } else {
                    mCurrentOffset = (int) Math.ceil(anstartOffset + (float) animation.getAnimatedValue());
                    if (mCurrentOffset >= totalOffset) {
                        mCurrentOffset = totalOffset - 1;
                    }
                }
                requestLayout();
            }
        });
        selectAnimator.start();
    }

    public void cancelAnmationtor() {
        if (selectAnimator != null && (selectAnimator.isStarted() || selectAnimator.isRunning())) {
            selectAnimator.cancel();
        }
    }

    /*
     * 获取某个childView在水平方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}