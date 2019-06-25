package com.ycbjie.yctablyout;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ycbjie.tablayoutlib.CustomTabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private CustomTabLayout tab;
    private ViewPager vp;
    private CustomTabLayout tab2;
    private ViewPager vp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tab = findViewById(R.id.tab);
        vp = findViewById(R.id.vp);
        tab2 = findViewById(R.id.tab2);
        vp2 = findViewById(R.id.vp2);
        initFragmentList();
        initFragmentList2();
    }


    private void initFragmentList() {
        ArrayList<String> mTitleList = new ArrayList<>();
        ArrayList<Fragment> mFragments = new ArrayList<>();
        mTitleList.add("综合");
        mTitleList.add("文学");
        mTitleList.add("文化逗比");
        mTitleList.add("生活励志哈");
        mTitleList.add("励志");
        mTitleList.add("小杨逗比");
        mFragments.add(MyFragment.newInstance("综合"));
        mFragments.add(MyFragment.newInstance("文学"));
        mFragments.add(MyFragment.newInstance("文化逗比"));
        mFragments.add(MyFragment.newInstance("生活励志哈"));
        mFragments.add(MyFragment.newInstance("励志"));
        mFragments.add(MyFragment.newInstance("小杨逗比"));

        /*
         * 注意使用的是：getChildFragmentManager，
         * 这样setOffscreenPageLimit()就可以添加上，保留相邻2个实例，切换时不会卡
         * 但会内存溢出，在显示时加载数据
         */
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        BasePagerAdapter myAdapter = new BasePagerAdapter(supportFragmentManager,
                mFragments, mTitleList);
        vp.setAdapter(myAdapter);
        // 左右预加载页面的个数
        vp.setOffscreenPageLimit(5);
        myAdapter.notifyDataSetChanged();
        tab.setupWithViewPager(vp);
        tab.setTabMode(TabLayout.MODE_SCROLLABLE);
        //设置每个Tab的内边距
        tab.setTabPaddingLeftAndRight(20, 20);
    }


    private void initFragmentList2() {
        ArrayList<String> mTitleList = new ArrayList<>();
        ArrayList<Fragment> mFragments = new ArrayList<>();
        mTitleList.add("综合");
        mTitleList.add("文学");
        mTitleList.add("文化逗比");
        mTitleList.add("生活励志哈");
        mTitleList.add("励志");
        mFragments.add(MyFragment.newInstance("综合"));
        mFragments.add(MyFragment.newInstance("文学"));
        mFragments.add(MyFragment.newInstance("文化逗比"));
        mFragments.add(MyFragment.newInstance("生活励志哈"));
        mFragments.add(MyFragment.newInstance("励志"));
        /*
         * 注意使用的是：getChildFragmentManager，
         * 这样setOffscreenPageLimit()就可以添加上，保留相邻2个实例，切换时不会卡
         * 但会内存溢出，在显示时加载数据
         */
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        BasePagerAdapter myAdapter = new BasePagerAdapter(supportFragmentManager,
                mFragments, mTitleList);
        vp2.setAdapter(myAdapter);
        // 左右预加载页面的个数
        vp2.setOffscreenPageLimit(5);
        myAdapter.notifyDataSetChanged();
        tab2.setTabMode(TabLayout.MODE_FIXED);
        tab2.setupWithViewPager(vp2);
        //设置每个Tab的内边距
        tab2.setTabPaddingLeftAndRight(20, 20);
    }



}
