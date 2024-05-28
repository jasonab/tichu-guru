package kankan.wheel.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import java.util.LinkedList;
import java.util.List;
import kankan.wheel.R;
import kankan.wheel.widget.WheelScroller;
import kankan.wheel.widget.adapters.WheelViewAdapter;

/* loaded from: classes.dex */
public class WheelView extends View {
    private static final int DEF_VISIBLE_ITEMS = 5;
    private static final int ITEM_OFFSET_PERCENT = 10;
    private static final int PADDING = 10;
    private static final int[] SHADOWS_COLORS = {-15658735, 11184810, 11184810};
    private GradientDrawable bottomShadow;
    private Drawable centerDrawable;
    private List<OnWheelChangedListener> changingListeners;
    private List<OnWheelClickedListener> clickingListeners;
    private int currentItem;
    private DataSetObserver dataObserver;
    private int firstItem;
    boolean isCyclic;
    private boolean isScrollingPerformed;
    private int itemHeight;
    private LinearLayout itemsLayout;
    private WheelRecycle recycle;
    private WheelScroller scroller;
    WheelScroller.ScrollingListener scrollingListener;
    private List<OnWheelScrollListener> scrollingListeners;
    private int scrollingOffset;
    private GradientDrawable topShadow;
    private WheelViewAdapter viewAdapter;
    private int visibleItems;

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.currentItem = 0;
        this.visibleItems = DEF_VISIBLE_ITEMS;
        this.itemHeight = 0;
        this.isCyclic = false;
        this.recycle = new WheelRecycle(this);
        this.changingListeners = new LinkedList();
        this.scrollingListeners = new LinkedList();
        this.clickingListeners = new LinkedList();
        this.scrollingListener = new WheelScroller.ScrollingListener() { // from class: kankan.wheel.widget.WheelView.1
            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onStarted() {
                WheelView.this.isScrollingPerformed = true;
                WheelView.this.notifyScrollingListenersAboutStart();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onScroll(int distance) {
                WheelView.this.doScroll(distance);
                int height = WheelView.this.getHeight();
                if (WheelView.this.scrollingOffset > height) {
                    WheelView.this.scrollingOffset = height;
                    WheelView.this.scroller.stopScrolling();
                } else if (WheelView.this.scrollingOffset < (-height)) {
                    WheelView.this.scrollingOffset = -height;
                    WheelView.this.scroller.stopScrolling();
                }
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onFinished() {
                if (WheelView.this.isScrollingPerformed) {
                    WheelView.this.notifyScrollingListenersAboutEnd();
                    WheelView.this.isScrollingPerformed = false;
                }
                WheelView.this.scrollingOffset = 0;
                WheelView.this.invalidate();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onJustify() {
                if (Math.abs(WheelView.this.scrollingOffset) > 1) {
                    WheelView.this.scroller.scroll(WheelView.this.scrollingOffset, 0);
                }
            }
        };
        this.dataObserver = new DataSetObserver() { // from class: kankan.wheel.widget.WheelView.2
            @Override // android.database.DataSetObserver
            public void onChanged() {
                WheelView.this.invalidateWheel(false);
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                WheelView.this.invalidateWheel(true);
            }
        };
        initData(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.currentItem = 0;
        this.visibleItems = DEF_VISIBLE_ITEMS;
        this.itemHeight = 0;
        this.isCyclic = false;
        this.recycle = new WheelRecycle(this);
        this.changingListeners = new LinkedList();
        this.scrollingListeners = new LinkedList();
        this.clickingListeners = new LinkedList();
        this.scrollingListener = new WheelScroller.ScrollingListener() { // from class: kankan.wheel.widget.WheelView.1
            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onStarted() {
                WheelView.this.isScrollingPerformed = true;
                WheelView.this.notifyScrollingListenersAboutStart();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onScroll(int distance) {
                WheelView.this.doScroll(distance);
                int height = WheelView.this.getHeight();
                if (WheelView.this.scrollingOffset > height) {
                    WheelView.this.scrollingOffset = height;
                    WheelView.this.scroller.stopScrolling();
                } else if (WheelView.this.scrollingOffset < (-height)) {
                    WheelView.this.scrollingOffset = -height;
                    WheelView.this.scroller.stopScrolling();
                }
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onFinished() {
                if (WheelView.this.isScrollingPerformed) {
                    WheelView.this.notifyScrollingListenersAboutEnd();
                    WheelView.this.isScrollingPerformed = false;
                }
                WheelView.this.scrollingOffset = 0;
                WheelView.this.invalidate();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onJustify() {
                if (Math.abs(WheelView.this.scrollingOffset) > 1) {
                    WheelView.this.scroller.scroll(WheelView.this.scrollingOffset, 0);
                }
            }
        };
        this.dataObserver = new DataSetObserver() { // from class: kankan.wheel.widget.WheelView.2
            @Override // android.database.DataSetObserver
            public void onChanged() {
                WheelView.this.invalidateWheel(false);
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                WheelView.this.invalidateWheel(true);
            }
        };
        initData(context);
    }

    public WheelView(Context context) {
        super(context);
        this.currentItem = 0;
        this.visibleItems = DEF_VISIBLE_ITEMS;
        this.itemHeight = 0;
        this.isCyclic = false;
        this.recycle = new WheelRecycle(this);
        this.changingListeners = new LinkedList();
        this.scrollingListeners = new LinkedList();
        this.clickingListeners = new LinkedList();
        this.scrollingListener = new WheelScroller.ScrollingListener() { // from class: kankan.wheel.widget.WheelView.1
            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onStarted() {
                WheelView.this.isScrollingPerformed = true;
                WheelView.this.notifyScrollingListenersAboutStart();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onScroll(int distance) {
                WheelView.this.doScroll(distance);
                int height = WheelView.this.getHeight();
                if (WheelView.this.scrollingOffset > height) {
                    WheelView.this.scrollingOffset = height;
                    WheelView.this.scroller.stopScrolling();
                } else if (WheelView.this.scrollingOffset < (-height)) {
                    WheelView.this.scrollingOffset = -height;
                    WheelView.this.scroller.stopScrolling();
                }
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onFinished() {
                if (WheelView.this.isScrollingPerformed) {
                    WheelView.this.notifyScrollingListenersAboutEnd();
                    WheelView.this.isScrollingPerformed = false;
                }
                WheelView.this.scrollingOffset = 0;
                WheelView.this.invalidate();
            }

            @Override // kankan.wheel.widget.WheelScroller.ScrollingListener
            public void onJustify() {
                if (Math.abs(WheelView.this.scrollingOffset) > 1) {
                    WheelView.this.scroller.scroll(WheelView.this.scrollingOffset, 0);
                }
            }
        };
        this.dataObserver = new DataSetObserver() { // from class: kankan.wheel.widget.WheelView.2
            @Override // android.database.DataSetObserver
            public void onChanged() {
                WheelView.this.invalidateWheel(false);
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                WheelView.this.invalidateWheel(true);
            }
        };
        initData(context);
    }

    private void initData(Context context) {
        this.scroller = new WheelScroller(getContext(), this.scrollingListener);
    }

    public void setInterpolator(Interpolator interpolator) {
        this.scroller.setInterpolator(interpolator);
    }

    public int getVisibleItems() {
        return this.visibleItems;
    }

    public void setVisibleItems(int count) {
        this.visibleItems = count;
    }

    public WheelViewAdapter getViewAdapter() {
        return this.viewAdapter;
    }

    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.viewAdapter != null) {
            this.viewAdapter.unregisterDataSetObserver(this.dataObserver);
        }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(this.dataObserver);
        }
        invalidateWheel(true);
    }

    public void addChangingListener(OnWheelChangedListener listener) {
        this.changingListeners.add(listener);
    }

    public void removeChangingListener(OnWheelChangedListener listener) {
        this.changingListeners.remove(listener);
    }

    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : this.changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    public void addScrollingListener(OnWheelScrollListener listener) {
        this.scrollingListeners.add(listener);
    }

    public void removeScrollingListener(OnWheelScrollListener listener) {
        this.scrollingListeners.remove(listener);
    }

    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : this.scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : this.scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    public void addClickingListener(OnWheelClickedListener listener) {
        this.clickingListeners.add(listener);
    }

    public void removeClickingListener(OnWheelClickedListener listener) {
        this.clickingListeners.remove(listener);
    }

    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : this.clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

    public int getCurrentItem() {
        return this.currentItem;
    }

    public void setCurrentItem(int index, boolean animated) {
        int scroll;
        if (this.viewAdapter != null && this.viewAdapter.getItemsCount() != 0) {
            int itemCount = this.viewAdapter.getItemsCount();
            if (index < 0 || index >= itemCount) {
                if (this.isCyclic) {
                    while (index < 0) {
                        index += itemCount;
                    }
                    index %= itemCount;
                } else {
                    return;
                }
            }
            if (index != this.currentItem) {
                if (animated) {
                    int itemsToScroll = index - this.currentItem;
                    if (this.isCyclic && (scroll = (Math.min(index, this.currentItem) + itemCount) - Math.max(index, this.currentItem)) < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                    scroll(itemsToScroll, 0);
                    return;
                }
                this.scrollingOffset = 0;
                int old = this.currentItem;
                this.currentItem = index;
                notifyChangingListeners(old, this.currentItem);
                invalidate();
            }
        }
    }

    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    public boolean isCyclic() {
        return this.isCyclic;
    }

    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        invalidateWheel(false);
    }

    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            this.recycle.clearAll();
            if (this.itemsLayout != null) {
                this.itemsLayout.removeAllViews();
            }
            this.scrollingOffset = 0;
        } else if (this.itemsLayout != null) {
            this.recycle.recycleItems(this.itemsLayout, this.firstItem, new ItemsRange());
        }
        invalidate();
    }

    private void initResourcesIfNecessary() {
        if (this.centerDrawable == null) {
            this.centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        }
        if (this.topShadow == null) {
            this.topShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }
        if (this.bottomShadow == null) {
            this.bottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }
        setBackgroundResource(R.drawable.wheel_bg);
    }

    private int getDesiredHeight(LinearLayout layout) {
        if (layout != null && layout.getChildAt(0) != null) {
            this.itemHeight = layout.getChildAt(0).getMeasuredHeight();
        }
        int desired = (this.itemHeight * this.visibleItems) - ((this.itemHeight * 10) / 50);
        return Math.max(desired, getSuggestedMinimumHeight());
    }

    private int getItemHeight() {
        if (this.itemHeight != 0) {
            return this.itemHeight;
        }
        if (this.itemsLayout != null && this.itemsLayout.getChildAt(0) != null) {
            this.itemHeight = this.itemsLayout.getChildAt(0).getHeight();
            return this.itemHeight;
        }
        return getHeight() / this.visibleItems;
    }

    private int calculateLayoutWidth(int widthSize, int mode) {
        int width;
        initResourcesIfNecessary();
        this.itemsLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        this.itemsLayout.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        int width2 = this.itemsLayout.getMeasuredWidth();
        if (mode == 1073741824) {
            width = widthSize;
        } else {
            width = Math.max(width2 + 20, getSuggestedMinimumWidth());
            if (mode == Integer.MIN_VALUE && widthSize < width) {
                width = widthSize;
            }
        }
        this.itemsLayout.measure(View.MeasureSpec.makeMeasureSpec(width - 20, 1073741824), View.MeasureSpec.makeMeasureSpec(0, 0));
        return width;
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        buildViewForMeasuring();
        int width = calculateLayoutWidth(widthSize, widthMode);
        if (heightMode == 1073741824) {
            height = heightSize;
        } else {
            height = getDesiredHeight(this.itemsLayout);
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(height, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override // android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layout(r - l, b - t);
    }

    private void layout(int width, int height) {
        int itemsWidth = width - 20;
        this.itemsLayout.layout(0, 0, itemsWidth, height);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.viewAdapter != null && this.viewAdapter.getItemsCount() > 0) {
            updateView();
            drawItems(canvas);
            drawCenterRect(canvas);
        }
        drawShadows(canvas);
    }

    private void drawShadows(Canvas canvas) {
        int height = (int) (1.5d * getItemHeight());
        this.topShadow.setBounds(0, 0, getWidth(), height);
        this.topShadow.draw(canvas);
        this.bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
        this.bottomShadow.draw(canvas);
    }

    private void drawItems(Canvas canvas) {
        canvas.save();
        int top = ((this.currentItem - this.firstItem) * getItemHeight()) + ((getItemHeight() - getHeight()) / 2);
        canvas.translate(10.0f, (-top) + this.scrollingOffset);
        this.itemsLayout.draw(canvas);
        canvas.restore();
    }

    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = (int) ((getItemHeight() / 2) * 1.2d);
        this.centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        this.centerDrawable.draw(canvas);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int distance;
        if (!isEnabled() || getViewAdapter() == null) {
            return true;
        }
        switch (event.getAction()) {
            case WheelScroller.MIN_DELTA_FOR_SCROLLING /* 1 */:
                if (!this.isScrollingPerformed) {
                    int distance2 = ((int) event.getY()) - (getHeight() / 2);
                    if (distance2 > 0) {
                        distance = distance2 + (getItemHeight() / 2);
                    } else {
                        distance = distance2 - (getItemHeight() / 2);
                    }
                    int items = distance / getItemHeight();
                    if (items != 0 && isValidItemIndex(this.currentItem + items)) {
                        notifyClickListenersAboutClick(this.currentItem + items);
                        break;
                    }
                }
                break;
            case 2:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                }
                break;
        }
        return this.scroller.onTouchEvent(event);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doScroll(int delta) {
        this.scrollingOffset += delta;
        int iHeight = getItemHeight();
        int count = this.scrollingOffset / iHeight;
        int pos = this.currentItem - count;
        int itemCount = this.viewAdapter.getItemsCount();
        int fixPos = this.scrollingOffset % iHeight;
        if (Math.abs(fixPos) <= iHeight / 2) {
            fixPos = 0;
        }
        if (this.isCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else if (pos < 0) {
            count = this.currentItem;
            pos = 0;
        } else if (pos >= itemCount) {
            count = (this.currentItem - itemCount) + 1;
            pos = itemCount - 1;
        } else if (pos > 0 && fixPos > 0) {
            pos--;
            count++;
        } else if (pos < itemCount - 1 && fixPos < 0) {
            pos++;
            count--;
        }
        int offset = this.scrollingOffset;
        if (pos != this.currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }
        this.scrollingOffset = offset - (count * iHeight);
        if (this.scrollingOffset > getHeight()) {
            this.scrollingOffset = (this.scrollingOffset % getHeight()) + getHeight();
        }
    }

    public void scroll(int itemsToScroll, int time) {
        int distance = (getItemHeight() * itemsToScroll) - this.scrollingOffset;
        this.scroller.scroll(distance, time);
    }

    private ItemsRange getItemsRange() {
        if (getItemHeight() == 0) {
            return null;
        }
        int first = this.currentItem;
        int count = 1;
        while (getItemHeight() * count < getHeight()) {
            first--;
            count += 2;
        }
        if (this.scrollingOffset != 0) {
            if (this.scrollingOffset > 0) {
                first--;
            }
            int emptyItems = this.scrollingOffset / getItemHeight();
            first -= emptyItems;
            count = (int) (count + 1 + Math.asin(emptyItems));
        }
        return new ItemsRange(first, count);
    }

    private boolean rebuildItems() {
        boolean updated;
        ItemsRange range = getItemsRange();
        if (this.itemsLayout != null) {
            int first = this.recycle.recycleItems(this.itemsLayout, this.firstItem, range);
            updated = this.firstItem != first;
            this.firstItem = first;
        } else {
            createItemsLayout();
            updated = true;
        }
        if (!updated) {
            updated = (this.firstItem == range.getFirst() && this.itemsLayout.getChildCount() == range.getCount()) ? false : true;
        }
        if (this.firstItem > range.getFirst() && this.firstItem <= range.getLast()) {
            for (int i = this.firstItem - 1; i >= range.getFirst() && addViewItem(i, true); i--) {
                this.firstItem = i;
            }
        } else {
            this.firstItem = range.getFirst();
        }
        int first2 = this.firstItem;
        for (int i2 = this.itemsLayout.getChildCount(); i2 < range.getCount(); i2++) {
            if (!addViewItem(this.firstItem + i2, false) && this.itemsLayout.getChildCount() == 0) {
                first2++;
            }
        }
        this.firstItem = first2;
        return updated;
    }

    private void updateView() {
        if (rebuildItems()) {
            calculateLayoutWidth(getWidth(), 1073741824);
            layout(getWidth(), getHeight());
        }
    }

    private void createItemsLayout() {
        if (this.itemsLayout == null) {
            this.itemsLayout = new LinearLayout(getContext());
            this.itemsLayout.setOrientation(1);
        }
    }

    private void buildViewForMeasuring() {
        if (this.itemsLayout != null) {
            this.recycle.recycleItems(this.itemsLayout, this.firstItem, new ItemsRange());
        } else {
            createItemsLayout();
        }
        int addItems = this.visibleItems / 2;
        for (int i = this.currentItem + addItems; i >= this.currentItem - addItems; i--) {
            if (addViewItem(i, true)) {
                this.firstItem = i;
            }
        }
    }

    private boolean addViewItem(int index, boolean first) {
        View view = getItemView(index);
        if (view == null) {
            return false;
        }
        if (first) {
            this.itemsLayout.addView(view, 0);
        } else {
            this.itemsLayout.addView(view);
        }
        return true;
    }

    private boolean isValidItemIndex(int index) {
        return this.viewAdapter != null && this.viewAdapter.getItemsCount() > 0 && (this.isCyclic || (index >= 0 && index < this.viewAdapter.getItemsCount()));
    }

    private View getItemView(int index) {
        if (this.viewAdapter == null || this.viewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = this.viewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            return this.viewAdapter.getEmptyItem(this.recycle.getEmptyItem(), this.itemsLayout);
        }
        while (index < 0) {
            index += count;
        }
        return this.viewAdapter.getItem(index % count, this.recycle.getItem(), this.itemsLayout);
    }

    public void stopScrolling() {
        this.scroller.stopScrolling();
    }
}
