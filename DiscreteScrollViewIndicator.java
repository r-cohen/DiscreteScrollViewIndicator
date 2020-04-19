package com.phearme.libs;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.yarolegovich.discretescrollview.DiscreteScrollView;

public class DiscreteScrollViewIndicator extends RecyclerView.ItemDecoration {
    private DiscreteScrollView mDiscreteScrollView;
    private boolean mAppendToBottom;
    private int colorActive = 0xFFFFFFFF;
    private int colorInactive = 0x66FFFFFF;
    private Alignment mAlignment = Alignment.PARENT_BOTTOM;
    private boolean mMatchParentWidth;

    public enum Alignment {
        PARENT_TOP,
        PARENT_BOTTOM
    }

    private int itemsCount = -1;

    private static final float DP = Resources.getSystem().getDisplayMetrics().density;

    /**
     * Height of the space the indicator takes up at the bottom of the view.
     */
    private final int mIndicatorHeight = (int) (DP * 16);

    /**
     * Indicator stroke width.
     */
    private float mIndicatorStrokeWidth = DP * 2;

    /**
     * Indicator width.
     */
    private float mIndicatorItemLength = DP * 16;
    /**
     * Padding between indicators.
     */
    private float mIndicatorItemPadding = DP * 4;

    /**
     * Some more natural animation interpolation
     */
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private final Paint mPaint = new Paint();

    public static DiscreteScrollViewIndicator Builder(DiscreteScrollView discreteScrollView) {
        return new DiscreteScrollViewIndicator(discreteScrollView);
    }

    private DiscreteScrollViewIndicator(DiscreteScrollView discreteScrollView) {
        mDiscreteScrollView = discreteScrollView;
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mIndicatorStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        if (mDiscreteScrollView == null || parent.getAdapter() == null) {
            return;
        }

        // find active page (which should be highlighted)
        int activePosition = mDiscreteScrollView.getCurrentItem();
        if (activePosition == RecyclerView.NO_POSITION) {
            return;
        }

        int itemCount = itemsCount;
        if (itemCount == -1) {
            itemCount = parent.getAdapter().getItemCount();
        }
        if (itemCount == 0) {
            return;
        }

        // position horizontally, calculate width and subtract half from center
        float paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding;
        if (mMatchParentWidth) {
            int parentWidth = mDiscreteScrollView.getWidth();
            mIndicatorItemLength = (parentWidth - paddingBetweenItems - (2 * mIndicatorItemPadding)) / itemCount;
        }
        float totalLength = mIndicatorItemLength * itemCount;
        float indicatorTotalWidth = totalLength + paddingBetweenItems;
        float indicatorStartX = (parent.getWidth() - indicatorTotalWidth) / 2F;

        // position vertically
        float indicatorPosY = mAlignment == Alignment.PARENT_BOTTOM ?
                parent.getHeight() - mIndicatorHeight / 2F:
                mIndicatorHeight / 2F;

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount);

        // find offset of active page (if the user is scrolling)
        RecyclerView.ViewHolder viewHolder = mDiscreteScrollView.getViewHolder(activePosition);
        if (viewHolder == null) {
            return;
        }
        final View activeChild = viewHolder.itemView;
        int left = activeChild.getLeft();
        int width = activeChild.getWidth();

        boolean reverseScrolling = left > 0;

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        float progress = mInterpolator.getInterpolation(left * -1  / (float) width);

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount, reverseScrolling);
    }

    private void drawInactiveIndicators(Canvas c, float indicatorStartX, float indicatorPosY, int itemCount) {
        mPaint.setColor(colorInactive);

        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        float start = indicatorStartX;
        for (int i = 0; i < itemCount; i++) {
            // draw the line for every item
            c.drawLine(start, indicatorPosY, start + mIndicatorItemLength, indicatorPosY, mPaint);
            start += itemWidth;
        }
    }

    private void drawHighlights(Canvas c, float indicatorStartX, float indicatorPosY,
                                int highlightPosition, float progress, int itemCount, boolean reverseScrolling) {
        mPaint.setColor(colorActive);

        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;
        float highlightStart = indicatorStartX + itemWidth * highlightPosition;
        if (progress == 0F) {
            // no swipe, draw a normal indicator
            c.drawLine(highlightStart, indicatorPosY,
                    highlightStart + mIndicatorItemLength, indicatorPosY, mPaint);
        } else {
            // calculate partial highlight
            float partialLength = mIndicatorItemLength * progress;
            if (reverseScrolling) {
                // draw the cut off highlight
                c.drawLine(highlightStart - mIndicatorItemPadding - partialLength, indicatorPosY,
                        highlightStart - mIndicatorItemPadding, indicatorPosY, mPaint);

                // draw the highlight overlapping to the next item as well
                c.drawLine(highlightStart, indicatorPosY,
                        highlightStart + mIndicatorItemLength - partialLength, indicatorPosY, mPaint);
            } else {
                // draw the cut off highlight
                c.drawLine(highlightStart + partialLength, indicatorPosY,
                        highlightStart + mIndicatorItemLength, indicatorPosY, mPaint);

                // draw the highlight overlapping to the next item as well
                if (highlightPosition < itemCount - 1) {
                    highlightStart += itemWidth;
                    c.drawLine(highlightStart, indicatorPosY,
                            highlightStart + partialLength, indicatorPosY, mPaint);
                }
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mAppendToBottom) {
            outRect.bottom = mIndicatorHeight;
        }
    }

    public DiscreteScrollViewIndicator appendToBottom() {
        this.mAppendToBottom = true;
        return this;
    }

    public DiscreteScrollViewIndicator setColorActive(int colorActive) {
        this.colorActive = colorActive;
        return this;
    }

    public DiscreteScrollViewIndicator setColorInactive(int colorInactive) {
        this.colorInactive = colorInactive;
        return this;
    }

    public DiscreteScrollViewIndicator setIndicatorStrokeWidth(float indicatorStrokeWidth) {
        this.mIndicatorStrokeWidth = indicatorStrokeWidth;
        mPaint.setStrokeWidth(mIndicatorStrokeWidth);
        return this;
    }

    public DiscreteScrollViewIndicator setIndicatorItemPadding(float indicatorItemPadding) {
        this.mIndicatorItemPadding = indicatorItemPadding;
        return this;
    }

    public DiscreteScrollViewIndicator align(Alignment alignment) {
        this.mAlignment = alignment;
        return this;
    }

    public DiscreteScrollViewIndicator matchParentWidth() {
        mMatchParentWidth = true;
        return this;
    }

    public DiscreteScrollViewIndicator setItemsCount(int count) {
        itemsCount = count;
    }
}

