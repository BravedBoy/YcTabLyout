# YcTabLayout
#### 目录介绍
- 01.遇到的实际需求分析
- 02.原生TabLayout局限
- 03.如何使用该控件
- 04.设置自定义tabView选项卡
- 05.自定义指示器的长度
- 06.设置滑动改变选项卡颜色
- 07.使用反射的注意要点
- 08.混淆时用到反射注意项
- 09.其他内容介绍




### 01.遇到的实际需求分析
- 实际开发中UI的效果图
    - 一般要求文字内容和指示线的宽度要一样
    - ![image](https://upload-images.jianshu.io/upload_images/4432347-21e0dbb631bdbdc6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 使用TabLayout的效果图
    - 一般指示线的宽度要大于文字内容
    - ![image](https://upload-images.jianshu.io/upload_images/4432347-a7cc1b2c1c0f5447.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- 遇到问题分析
    - 设置tabPaddingStart和tabPaddingEnd，但是布局填上去后发现并没有用。
- 实现方案
    - 第一种：自定义类似TabLayout的控件，代码量巨大，且GitHub上有许多已经比较成熟的库，代码质量是层次不齐。
    - 第二种：在原有基础上通过继承TabLayout控件，重写其中几个方法，并且通过反射来修改部分属性，也能达到第一种方案效果。
    - 下面就来讲一下我自己通过第二种方案实现步骤和原理！
- 最终UI效果图展示
    - ![image](https://upload-images.jianshu.io/upload_images/4432347-661d69545bd584d3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



### 02.原生TabLayout局限
- 一张图看懂TabLayout的结构
    - ![image](https://upload-images.jianshu.io/upload_images/4432347-ac24ffc6d3f02c35.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
    - 如果要用代码进行表示的话，大概是这样的。TabLayout继承自HorizontalScrollView，而都知道ScrollView只能添加一个子 View，所以SlidingTabIndicator就是那个用来添加子View 的横向LinearLayout。
- 存在的局限性
    - 第一个无法改变指示线的宽度
    - 第二个无法做到滑动改变tab选项卡颜色渐变的效果【有的还需要放大效果】



### 03.如何使用该控件
- 在布局中
    ```
    <com.ycbjie.tablayoutlib.CustomTabLayout
        android:id="@+id/tab"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        app:tabIndicatorFullWidth="false"
        app:tabIndicatorColor="@color/red"
        app:tabMode="scrollable"
        app:tabSelectedTextColor="@color/red"
        app:tabTextColor="@color/black"/>
    ```
- 代码设置，几乎和原生TabLayout一样，只是多了几个方法
    ```
    //设置每个Tab的内边距
    tab.setIndicator(10,10);
    ```


### 04.实现滑动改变颜色
- 滑动改变指示器文字变色
    - TabLayout中可以设置文字内容，通过上面3.2源码分析，可以知道通过addTab添加自定义选项卡，那么滑动改变选项卡tabView的颜色，可以会涉及到监听滑动。因此这里需要用反射替换成自己的滑动监听，然后在TabLayoutOnPageChangeListener的监听类中的onPageScrolled方法，改变tabView的颜色。
- 通过反射找到源码中pageChangeListener成员变量，然后设置暴力访问权限。
    - 然后获取TabLayoutOnPageChangeListener的对象，删除自带的监听，同时将自己自定义的滑动监听listener添加上。


### 05.自定义指示器的长度
- 通过反射的方式修改指示器长度，如果需要指示器宽度等于文字宽度需要自己微调，或者28版本直接通过设置app:tabIndicatorFullWidth="false"属性即可让内容和指示器宽度一样。
- 大概思路就是通过反射的方式获取TabLayout的字段mTabStrip(27之前)或者slidingTabIndicator(28之后),然后再去遍历修改每一个子 View 的 Margin 值。


### 06.设置滑动改变选项卡颜色
- 滑动时如何改变选项卡的颜色呢？当然在滚动的时候去动态改变属性，具体的做法：在TabLayoutOnPageChangeListener中监听。
- 在onPageScrolled滚动监听方法中拿到positionOffset的值，然后拿到当前tabView和下一个tabView，然后依次改变Progress进度，以此达到更改文字的颜色。


### 07.使用反射的注意要点
- 比如或者mTabStrip属性，网上许多没有区分27和28名称的变化。如果因为名称的问题，会导致反射获取不到Field，那么所做的操作也就失效了，这是一个很大的风险。
    ```
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
    ```



### 07.混淆时用到反射注意项
- 还有一点就是有的人这么使用会报错，是因为混淆产生的问题，反射slidingTabIndicator或者pageChangeListener的时候可能会出问题，可以在混淆配置里面设置下TabLayout不被混淆。
    ```
    -keep class android.support.design.widget.TabLayout{*;}
    ```


### 09.其他内容介绍
#### 关于其他内容介绍
![image](https://upload-images.jianshu.io/upload_images/4432347-7100c8e5a455c3ee.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 01.关于博客汇总链接
- 1.[技术博客汇总](https://www.jianshu.com/p/614cb839182c)
- 2.[开源项目汇总](https://blog.csdn.net/m0_37700275/article/details/80863574)
- 3.[生活博客汇总](https://blog.csdn.net/m0_37700275/article/details/79832978)
- 4.[喜马拉雅音频汇总](https://www.jianshu.com/p/f665de16d1eb)
- 5.[其他汇总](https://www.jianshu.com/p/53017c3fc75d)



#### 02.关于我的博客
- 我的个人站点：www.yczbj.org，www.ycbjie.cn
- github：https://github.com/yangchong211
- 知乎：https://www.zhihu.com/people/yczbj/activities
- 简书：http://www.jianshu.com/u/b7b2c6ed9284
- csdn：http://my.csdn.net/m0_37700275
- 喜马拉雅听书：http://www.ximalaya.com/zhubo/71989305/
- 开源中国：https://my.oschina.net/zbj1618/blog
- 泡在网上的日子：http://www.jcodecraeer.com/member/content_list.php?channelid=1
- 邮箱：yangchong211@163.com
- 阿里云博客：https://yq.aliyun.com/users/article?spm=5176.100- 239.headeruserinfo.3.dT4bcV
- segmentfault头条：https://segmentfault.com/u/xiangjianyu/articles
- 掘金：https://juejin.im/user/5939433efe88c2006afa0c6e



#### 03.勘误及提问
- 如果有疑问或者发现错误，可以在相应的 issues 进行提问或勘误。如果喜欢或者有所启发，欢迎star，对作者也是一种鼓励。


#### 04.关于LICENSE
```
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
```



















