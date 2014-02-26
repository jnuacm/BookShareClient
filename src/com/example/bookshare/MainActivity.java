package com.example.bookshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnScrollListener {

	private LayoutInflater layout_in_flater;
	private ImageView underlined;

	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private int textNum;// 页卡个数
	private List<TextView> textViews;

	private List<View> viewList;
	private ViewPager viewPager;// viewpager

	ListView mybookslistview;
	SimpleAdapter adapter;
	List<Map<String, Object>> list;
	boolean isLastRow = false;
	boolean isFirstRow = false;
	int listshowsize = 10;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* 以下是子示图1 */
		initData();
		mybookslistview.setAdapter(adapter);
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		layout_in_flater = getLayoutInflater().from(this);
		viewList = new ArrayList<View>();
		viewList.add(mybookslistview);
		viewList.add(layout_in_flater.inflate(R.layout.view02, null));
		viewList.add(layout_in_flater.inflate(R.layout.view03, null));

		textNum = 3;
		InitImageView();// 初始化下划线
		InitTextView();
		viewPager.setAdapter(new MyViewPagerAdapter(viewList));

		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		textViews.get(currIndex).setTextColor(Color.rgb(50, 189, 189));

	}

	@SuppressWarnings("unused")
	private BitmapDrawable BitmapDrawable(Bitmap btm1) {
		// TODO Auto-generated method stub
		return null;
	}

	private void InitImageView() {

		underlined = (ImageView) findViewById(R.id.underlined);
		bmpW = BitmapFactory.decodeResource(getResources(),
				R.drawable.underlined).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度

		// double temp_offset = (screenW - textNum * bmpW) / 6000.0;
		// offset = (int)(temp_offset*1000);// 计算偏移量
		offset = 0;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		matrix.setScale((float) (screenW / (3.0 * bmpW)), (float) 1.0);
		bmpW = dm.widthPixels / 3;
		underlined.setImageMatrix(matrix);// 设置动画初始位置

	}

	private void InitTextView() {
		textViews = new ArrayList<TextView>();
		textViews.add((TextView) findViewById(R.id.text1));
		textViews.add((TextView) findViewById(R.id.text2));
		textViews.add((TextView) findViewById(R.id.text3));

		int j = 0;
		for (TextView i : textViews)
			i.setOnClickListener(new MyOnClickListener(j++));

	}

	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewPager.setCurrentItem(index);

		}

	}

	public class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));// 删除页卡
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			container.addView(mListViews.get(position), 0);// 添加页卡
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();// 返回页卡的数量
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int gap = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			textViews.get(currIndex).setTextColor(Color.rgb(0, 0, 0));
			Animation animation = new TranslateAnimation(gap * currIndex, gap
					* arg0, 0, 0);// 显然这个比较简洁，只有一行代码。
			currIndex = arg0;
			textViews.get(currIndex).setTextColor(Color.rgb(50, 189, 189));
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			underlined.startAnimation(animation);
		}

	}

	// @Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if (firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount > 0) {
			isLastRow = true;
		}
		if (0 == firstVisibleItem && totalItemCount > 0) {
			isFirstRow = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

		if (isLastRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			// 加载元素
			loadData();
			adapter.notifyDataSetChanged();
			isLastRow = false;
		} else if (isFirstRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			list.clear();
			loadData();
			adapter.notifyDataSetChanged();
			isFirstRow = false;
		}
	}

	private void initData() {
		list = getData();
		adapter = new SimpleAdapter(this, list, R.layout.mybooks_listview_item,
				new String[] { "image", "bookname", "state" }, new int[] {
						R.id.mybookslistitem_bookimage,
						R.id.mybookslistitem_bookname,
						R.id.mybookslistitem_bookstate });

		View view = LayoutInflater.from(this).inflate(
				R.layout.activity_submain1, null);
		mybookslistview = (ListView) view.findViewById(R.id.mybookslistview);

		mybookslistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position)
					Toast.makeText(getApplicationContext(),
							"Jump into add books", Toast.LENGTH_SHORT).show();
				else {
					// Intent intent = new
					// Intent(MainActivity.this,MyBookContentActivity.class);
					// startActivity(intent);
				}
			}
		});

		mybookslistview.setOnScrollListener(this);

		mybookslistview.addHeaderView(LayoutInflater.from(this).inflate(
				R.layout.mybooks_listview_top, null));

	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.tmp);
			map.put("bookname", "Book Name !!! " + i);
			map.put("state", "状态！！！");
			ret.add(map);
		}

		return ret;
	}

	private void loadData() {
		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.tmp);
			map.put("bookname", "REN YAO " + list.size());
			map.put("state", "在借");
			list.add(map);
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */

}
