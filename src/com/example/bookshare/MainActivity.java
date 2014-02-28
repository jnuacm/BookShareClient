package com.example.bookshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
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

	private int offset = 0;// ����ͼƬƫ����
	private int currIndex = 0;// ��ǰҳ�����
	private int bmpW;// ����ͼƬ���
	
	private List<TextView> textViews;

	private List<View> viewList;
	private ViewPager viewPager;// viewpager

	ListView mybookslistview,myfriendslistview;
	SimpleAdapter bookAdapter,friendAdapter;
	List<Map<String, Object>> bookList,friendList;
	boolean isBookLastRow = false, isFriendLastRow = false;
	boolean isBookFirstRow = false, isFriendFirstRow = false;
	int listshowsize = 10;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* ��������ʾͼ*/
		initBookData();
		initFriendData();
		mybookslistview.setAdapter(bookAdapter);
		myfriendslistview.setAdapter(friendAdapter);
		

		layout_in_flater = getLayoutInflater().from(this);
		viewList = new ArrayList<View>();
		viewList.add(mybookslistview);
		viewList.add(myfriendslistview);
		viewList.add(layout_in_flater.inflate(R.layout.view03, null));
		
		
		/*��������ʾ��ҳ����*/
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		InitImageView();// ��ʼ���»���
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
				R.drawable.underlined).getWidth();// ��ȡͼƬ���
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// ��ȡ�ֱ��ʿ��

		// double temp_offset = (screenW - textNum * bmpW) / 6000.0;
		// offset = (int)(temp_offset*1000);// ����ƫ����
		offset = 0;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		matrix.setScale((float) (screenW / (3.0 * bmpW)), (float) 1.0);
		bmpW = dm.widthPixels / 3;
		underlined.setImageMatrix(matrix);// ���ö�����ʼλ��

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
			this.mListViews = mListViews;// ���췽�������������ǵ�ҳ���������ȽϷ��㡣
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));// ɾ��ҳ��
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // �����������ʵ����ҳ��
			container.addView(mListViews.get(position), 0);// ���ҳ��
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();// ����ҳ��������
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// �ٷ���ʾ����д
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int gap = offset * 2 + bmpW;// ҳ��1 -> ҳ��2 ƫ����

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			textViews.get(currIndex).setTextColor(Color.rgb(0, 0, 0));
			Animation animation = new TranslateAnimation(gap * currIndex, gap
					* arg0, 0, 0);// ��Ȼ����Ƚϼ�ֻ࣬��һ�д��롣
			currIndex = arg0;
			textViews.get(currIndex).setTextColor(Color.rgb(50, 189, 189));
			animation.setFillAfter(true);// True:ͼƬͣ�ڶ�������λ��
			animation.setDuration(300);
			underlined.startAnimation(animation);
		}

	}
	
	
	/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
	/*!!!!!!!!!!!!!!!!!!!!!!!!!!�����ǹ�����ҳ��ĵ��ú���!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
	/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if (firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount > 0) {
			if(0 == currIndex){
				isBookLastRow = true;
			}
			else if(1 == currIndex){
				isFriendLastRow = true;
			}
		}
		if (0 == firstVisibleItem && totalItemCount > 0) {
			if(0 == currIndex){
				isBookFirstRow = true;
			}
			else if(1 == currIndex){
				isFriendFirstRow = true;
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

		if( (0 == currIndex) && isBookLastRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			// ����Ԫ��
			loadBookData();
			bookAdapter.notifyDataSetChanged();
			isBookLastRow = false;
			
		} else if ((0 == currIndex) &&isBookFirstRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			
			bookList.clear();
			loadBookData();
			bookAdapter.notifyDataSetChanged();
			isBookFirstRow = false;
			
		} else if((1 == currIndex) &&isFriendLastRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
			
			loadFriendData();
			friendAdapter.notifyDataSetChanged();
			isFriendLastRow = false;
			
		} else if((1 == currIndex) &&isFriendFirstRow
				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
			
			friendList.clear();
			loadFriendData();
			friendAdapter.notifyDataSetChanged();
			isFriendFirstRow = false;
			
		}
		
	}

	private void initBookData() {
		bookList = getBookData();
		bookAdapter = new SimpleAdapter(this, bookList, R.layout.mybooks_listview_item,
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
	
	

	private List<Map<String, Object>> getBookData() {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.book1);
			map.put("bookname", "Book Name !!! " + i);
			map.put("state", "״̬������");
			ret.add(map);
		}

		return ret;
	}

	private void loadBookData() {
		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.book1);
			map.put("bookname", "REN YAO " + bookList.size());
			map.put("state", "�ڽ�");
			bookList.add(map);
		}
	}
	
	private void initFriendData() {
		friendList = getFriendData();
		friendAdapter = new SimpleAdapter(this, friendList, R.layout.myfriends_listview_item,
				new String[] { "image", "friendname" }, new int[] {
						R.id.myfriendslistitem_friendimage,
						R.id.myfriendslistitem_friendname});

		View view = LayoutInflater.from(this).inflate(
				R.layout.activity_submain2, null);
		myfriendslistview = (ListView) view.findViewById(R.id.myfirendslistview);

		myfriendslistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position)
					Toast.makeText(getApplicationContext(),
							"Jump into add friends", Toast.LENGTH_SHORT).show();
				else {
					 Intent intent = new
					 Intent(MainActivity.this,FriendsInformationActivity.class);
					 startActivity(intent);
				}
			}
		});

		myfriendslistview.setOnScrollListener(this);
		myfriendslistview.addHeaderView(LayoutInflater.from(this).inflate(
				R.layout.myfriends_listview_top, null));

	}
	private List<Map<String, Object>> getFriendData() {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.friend1);
			map.put("friendname", "������" + i);
			ret.add(map);
		}

		return ret;
	}
	
	private void loadFriendData() {
		Map<String, Object> map;
		for (int i = 0; i < listshowsize; i++) {
			map = new HashMap<String, Object>();
			map.put("image", R.drawable.friend1);
			map.put("friendname", "Kitty" + i);
			friendList.add(map);
		}
	}

}
