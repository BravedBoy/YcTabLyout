package com.ycbjie.tablayoutlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;


/**
 * @author yc
 */
public class CustomTabLayout extends TabLayout {

    private int mTabTextSize;
    private int mTabSelectedTextColor;
    private int mTabTextColor;
    private static final int INVALID_TAB_POS = -1;
    private int mLastSelectedTabPosition = INVALID_TAB_POS;

    public CustomTabLayout(Context context) {
        this(context, null);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("PrivateResource")
    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            @SuppressLint("CustomViewStyleable")
            TypedArray a = context.obtainStyledAttributes(attrs,
                    android.support.design.R.styleable.TabLayout,
                    defStyleAttr, android.support.design.R.style.Widget_Design_TabLayout);
            try {
                @SuppressLint("PrivateResource")
                int tabTextAppearance = a.getResourceId(
                        android.support.design.R.styleable.TabLayout_tabTextAppearance,
                        android.support.design.R.style.TextAppearance_Design_Tab);
                @SuppressLint("CustomViewStyleable")
                final TypedArray ta = context.obtainStyledAttributes(tabTextAppearance,
                        android.support.v7.appcompat.R.styleable.TextAppearance);
                try {
                    //Tab字体大小
                    mTabTextSize = ta.getDimensionPixelSize(
                            android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                            0);
                    //Tab文字颜色
                    mTabTextColor = ta.getColor(
                            android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor,
                            0);
                } finally {
                    ta.recycle();
                }
                //Tab文字选中颜色
                mTabSelectedTextColor = a.getColor(
                        android.support.design.R.styleable.TabLayout_tabSelectedTextColor, Color.BLACK);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        CustomTabView tabView = new CustomTabView(getContext());
        tabView.setProgress(setSelected ? 1 : 0);
        tabView.setText(tab.getText() + "");
        tabView.setTextSize(mTabTextSize);
        tabView.setTag(position);
        tabView.setTextChangeColor(mTabSelectedTextColor);
        tabView.setTextOriginColor(mTabTextColor);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabView.setLayoutParams(layoutParams);
        tab.setCustomView(tabView);
        //添加到tab中
        super.addTab(tab, position, setSelected);
        int selectedTabPosition = getSelectedTabPosition();

        boolean isSelect = selectedTabPosition == position;
        if ((selectedTabPosition == INVALID_TAB_POS && position == 0) || isSelect) {
            setSelectedView(position);
        }
        setTabWidth(position, tabView);
    }

    private void setTabWidth(int position, CustomTabView customTabView) {
        ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) slidingTabStrip.getChildAt(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        //手动测量一下
        customTabView.measure(w, h);
        int measuredWidth = customTabView.getMeasuredWidth();
        params.width = measuredWidth + tabView.getPaddingLeft() + tabView.getPaddingRight();
        //设置tabView的宽度
        tabView.setLayoutParams(params);
    }

    /**
     * 设置每个Tab的左内边距和右内边距
     *
     * @param left                          左边距
     * @param right                         右边距
     */
    public void setTabPaddingLeftAndRight(int left, int right) {
        try {
            Field mTabPaddingStartField = getTabPaddingStart();
            Field mTabPaddingEndField = getTabPaddingEnd();
            mTabPaddingStartField.setAccessible(true);
            mTabPaddingEndField.setAccessible(true);
            mTabPaddingStartField.set(this, left);
            mTabPaddingEndField.set(this, right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean autoRefresh) {
        super.setupWithViewPager(viewPager, autoRefresh);
        try {
            //通过反射找到mPageChangeListener
            Field field = getPageChangeListener();
            field.setAccessible(true);
            TabLayoutOnPageChangeListener listener = (TabLayoutOnPageChangeListener) field.get(this);
            if (listener!=null && viewPager!=null) {
                //删除自带监听
                viewPager.removeOnPageChangeListener(listener);
                OnPageChangeListener mPageChangeListener = new OnPageChangeListener(this);
                mPageChangeListener.reset();
                viewPager.addOnPageChangeListener(mPageChangeListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void tabScrolled(int position, float positionOffset) {
        if (positionOffset == 0.0F) {
            return;
        }
        CustomTabView currentTrackView = getCustomTabView(position);
        CustomTabView nextTrackView = getCustomTabView(position + 1);
        if (currentTrackView != null) {
            currentTrackView.setDirection(1);
            currentTrackView.setProgress(1.0F - positionOffset);
        }
        if (nextTrackView != null) {
            nextTrackView.setDirection(0);
            nextTrackView.setProgress(positionOffset);
        }
    }


    private CustomTabView getCustomTabView(int position) {
        Tab tab = getTabAt(position);
        if (tab != null) {
            CustomTabView tabAt = (CustomTabView) tab.getCustomView();
            return tabAt;
        }else {
            return null;
        }
    }

    /**
     * 滑动监听，核心逻辑
     * 建议如果是activity退到后台，或者关闭页面，将listener给remove掉
     * 采用弱引用方式防止监听listener内存泄漏，算是一个小的优化
     */
    private static class OnPageChangeListener extends TabLayoutOnPageChangeListener {

        private final WeakReference<CustomTabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        OnPageChangeListener(TabLayout tabLayout) {
            super(tabLayout);
            mTabLayoutRef = new WeakReference<>((CustomTabLayout) tabLayout);
        }

        /**
         * 这个方法是滚动状态发生变化是调用
         * @param state                     桩体
         */
        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        /**
         * 正在滚动时调用
         * @param position                  索引
         * @param positionOffset            offset偏移
         * @param positionOffsetPixels      offsetPixels
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            CustomTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout == null) {
                return;
            }
            final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                    mPreviousScrollState == SCROLL_STATE_DRAGGING;
            if (updateText) {
                tabLayout.tabScrolled(position, positionOffset);
            }
        }

        /**
         * 选中时调用
         * @param position                      索引
         */
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            CustomTabLayout tabLayout = mTabLayoutRef.get();
            mPreviousScrollState = SCROLL_STATE_SETTLING;
            tabLayout.setSelectedView(position);
        }

        /**
         * 重置状态
         */
        void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }
    }


    private void setSelectedView(int position) {
        final int tabCount = getTabCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                CustomTabView customTabView = getCustomTabView(i);
                if (customTabView!=null){
                    customTabView.setProgress(i == position ? 1 : 0);
                }
            }
        }
    }

    @Override
    public void removeAllTabs() {
        mLastSelectedTabPosition = getSelectedTabPosition();
        super.removeAllTabs();
    }

    @Override
    public int getSelectedTabPosition() {
        final int selectedTabPositionAtParent = super.getSelectedTabPosition();
        return selectedTabPositionAtParent == INVALID_TAB_POS ?
                mLastSelectedTabPosition : selectedTabPositionAtParent;
    }

    /**
     * 反射获取私有的mPageChangeListener属性，考虑support 28以后变量名修改的问题
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getPageChangeListener() throws NoSuchFieldException {
        Class clazz = TabLayout.class;
        try {
            // support design 27及一下版本
            return clazz.getDeclaredField("mPageChangeListener");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // 可能是28及以上版本
            return clazz.getDeclaredField("pageChangeListener");
        }
    }


    /**
     * 反射获取私有的mTabPaddingStart属性，考虑support 28以后变量名修改的问题
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getTabPaddingStart() throws NoSuchFieldException {
        Class clazz = TabLayout.class;
        try {
            // support design 27及一下版本
            return clazz.getDeclaredField("mTabPaddingStart");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // 可能是28及以上版本
            return clazz.getDeclaredField("tabPaddingStart");
        }
    }

    /**
     * 反射获取私有的mTabPaddingEnd属性，考虑support 28以后变量名修改的问题
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getTabPaddingEnd() throws NoSuchFieldException {
        Class clazz = TabLayout.class;
        try {
            // support design 27及一下版本
            return clazz.getDeclaredField("mTabPaddingEnd");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // 可能是28及以上版本
            return clazz.getDeclaredField("tabPaddingEnd");
        }
    }

    /**
     * 反射获取私有的mTabPaddingEnd属性，考虑support 28以后变量名修改的问题
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getTabStrip() throws NoSuchFieldException {
        Class clazz = TabLayout.class;
        try {
            // support design 27及一下版本
            return clazz.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // 可能是28及以上版本
            return clazz.getDeclaredField("slidingTabIndicator");
        }
    }

}
