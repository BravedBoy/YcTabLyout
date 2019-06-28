/*
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package com.ycbjie.tablayoutlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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
        //获取TabLayout的子布局slidingTabStrip
        ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);
        //获取选项卡tabView
        ViewGroup tabView = (ViewGroup) slidingTabStrip.getChildAt(position);
        //设置属性为包裹
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //获取宽高
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        Log.d("yc-----setTabWidth--","宽---"+w+"高----"+h);
        //手动测量一下
        customTabView.measure(w, h);
        int measuredWidth = customTabView.getMeasuredWidth();
        int paddingLeft = tabView.getPaddingLeft();
        int paddingRight = tabView.getPaddingRight();
        Log.d("yc-----padding--","paddingRight---"+paddingLeft+"paddingRight----"+paddingRight);
        params.width = measuredWidth + paddingLeft + paddingRight;
        Log.d("yc-----width--","宽---"+params.width);
        //设置tabView的宽度
        tabView.setLayoutParams(params);
    }

    /**
     * 设置每个Tab的左内边距和右内边距
     * 暂时有点问题，设置为过时，建议使用
     * @param left                          左边距
     * @param right                         右边距
     */
    @Deprecated
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


    /**
     * 滑动改变自定义tabView的颜色
     * @param position                      索引
     * @param positionOffset                偏移量
     */
    private void tabScrolled(int position, float positionOffset) {
        if (positionOffset == 0.0F) {
            return;
        }
        //从左往右滑动，positionOffset   从0逐渐增加到1
        //从右往左滑动，positionOffset   从1逐渐减小到0
        //当前tabView
        CustomTabView currentTrackView = getCustomTabView(position);
        //下一个tabView
        CustomTabView nextTrackView = getCustomTabView(position + 1);
        if (currentTrackView != null) {
            //DIRECTION_RIGHT
            currentTrackView.setDirection(1);
            currentTrackView.setProgress(1.0F - positionOffset);
        }
        if (nextTrackView != null) {
            //DIRECTION_LEFT
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
                Log.d("yc-----onPageScrolled--","positionOffset---"+positionOffset);
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
     * 通过反射设置TabLayout每一个的长度
     * @param left                      左边 Margin 单位 dp
     * @param right                     右边 Margin 单位 dp
     */
    public void setIndicator(int left, int right) {
        Field tabStrip = null;
        try {
            tabStrip = getTabStrip();
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        LinearLayout llTab = null;
        try {
            if (tabStrip != null) {
                llTab = (LinearLayout) tabStrip.get(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int l = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, left,
                Resources.getSystem().getDisplayMetrics());
        int r = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, right,
                Resources.getSystem().getDisplayMetrics());

        if (llTab != null) {
            for (int i = 0; i < llTab.getChildCount(); i++) {
                View child = llTab.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = l;
                params.rightMargin = r;
                child.setLayoutParams(params);
                child.invalidate();
            }
        }
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
     * 反射获取私有的mTabStrip属性，考虑support 28以后变量名修改的问题
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
