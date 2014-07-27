package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private LayoutInflater layout_in_flater;
	private ImageView underlined;

	public static final int SCANREQUEST_ADDBOOK = 1;

	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	private List<TextView> textViews;

	private List<View> viewList;
	private ViewPager viewPager;// viewpager

	private BookListManage bookmanage = new BookListManage();
	private FriendListManage friendmanage = new FriendListManage();

	int listshowsize = 10;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		layout_in_flater = getLayoutInflater().from(this);
		viewList = new ArrayList<View>();
		viewList.add(bookmanage.getView());
		viewList.add(friendmanage.getView());
		viewList.add(layout_in_flater.inflate(R.layout.view03, null));

		/* 以下是显示翻页部分 */
		viewPager = (ViewPager) findViewById(R.id.viewpager);
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

	public void showToast(String content) {
		Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCANREQUEST_ADDBOOK && RESULT_OK == resultCode) {
			String isbn = data.getStringExtra("isbn");
			Log.i("in onactivityresult", "go inside");
			LocalApp localapp = (LocalApp) getApplication();
			localapp.getUser().addBook(isbn, new AddBookHandler());
		}
	}

	private class AddBookHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				Bundle data = msg.getData();
				if (data.getInt("status") == NetAccess.STATUS_SUCCESS)
					MainActivity.this.showToast("添加成功");
				else
					MainActivity.this.showToast("添加失败:"
							+ data.getString("response"));
				break;
			case NetAccess.NETMSG_ERROR:
				MainActivity.this
						.showToast(msg.getData().getString("response"));
				break;
			}
		}
	}

	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!以下是构造子页面的调用函数!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

	private class BookListManage implements OnScrollListener {

		ListView mybookslistview;
		SimpleAdapter bookAdapter;
		List<Map<String, Object>> bookList;

		private boolean isFirstRow = false;
		private boolean isLastRow = false;

		@Override
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

				bookmanage.loadBookData();
				bookmanage.showUpdate();
				isLastRow = false;

			} else if (isFirstRow
					&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

				bookmanage.reload();
				bookmanage.showUpdate();
				isFirstRow = false;

			}
		}

		private View getView() {
			initBookList();
			return mybookslistview;
		}

		private void initBookList() {
			bookList = getBookData();
			bookAdapter = new SimpleAdapter(MainActivity.this, bookList,
					R.layout.mybooks_listview_item, new String[] { "image",
							"bookname", "state" }, new int[] {
							R.id.mybookslistitem_bookimage,
							R.id.mybookslistitem_bookname,
							R.id.mybookslistitem_bookstate });

			View view = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.activity_submain1, null);
			mybookslistview = (ListView) view
					.findViewById(R.id.mybookslistview);

			mybookslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (0 == position) {
						Intent intent = new Intent(MainActivity.this,
								CaptureActivity.class);
						startActivityForResult(intent, SCANREQUEST_ADDBOOK);
					} else {
						Intent intent = new Intent(MainActivity.this,
								BookInformationActivity.class);
						startActivity(intent);
					}
				}
			});

			mybookslistview.setOnScrollListener(this);

			mybookslistview.addHeaderView(LayoutInflater
					.from(MainActivity.this).inflate(
							R.layout.mybooks_listview_top, null));

			mybookslistview.setAdapter(bookAdapter);

		}

		private List<Map<String, Object>> getBookData() {
			List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
			Map<String, Object> map = new HashMap<String, Object>();

			String response = getIntent().getStringExtra("response");

			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("own_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String bookname = item.getString("name");
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("image", R.drawable.book1);
					map.put("bookname", bookname);
					map.put("status", status);
					ret.add(map);
				}

				jsonarray = jsonobj.getJSONArray("borrowed_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String bookname = item.getString("name");
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("image", R.drawable.book1);
					map.put("bookname", bookname);
					map.put("status", status);
					ret.add(map);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return ret;
		}

		private void loadBookData() {

			Map<String, Object> map;
			for (int i = 0; i < 10; i++) {

				map = new HashMap<String, Object>();
				map.put("image", R.drawable.default_book_big);
				map.put("bookname", "load book name");
				if (i % 2 == 0)
					map.put("state", "mine");
				else
					map.put("state", "other");
				bookList.add(map);
			}
		}

		private void reload() {
			bookList.clear();
			Map<String, Object> map;
			for (int i = 0; i < 10; i++) {

				map = new HashMap<String, Object>();
				map.put("image", R.drawable.default_book_big);
				map.put("bookname", "get book name");
				if (i % 2 == 0)
					map.put("state", "mine");
				else
					map.put("state", "other");
				bookList.add(map);
			}
		}

		private void showUpdate() {
			bookAdapter.notifyDataSetChanged();
		}
	}

	private class FriendListManage implements OnScrollListener {
		ListView myfriendslistview;
		SimpleAdapter friendAdapter;
		List<Map<String, Object>> friendList;

		private boolean isFirstRow = false;
		private boolean isLastRow = false;

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

				friendmanage.loadFriendData();
				friendmanage.showUpdate();
				isLastRow = false;

			} else if (isFirstRow
					&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

				friendmanage.reload();
				friendmanage.showUpdate();
				isFirstRow = false;

			}

		}

		public View getView() {
			initFriendList();
			return myfriendslistview;
		}

		private void initFriendList() {
			friendList = getFriendData();
			friendAdapter = new SimpleAdapter(MainActivity.this, friendList,
					R.layout.myfriends_listview_item, new String[] { "image",
							"friendname" }, new int[] {
							R.id.myfriendslistitem_friendimage,
							R.id.myfriendslistitem_friendname });

			View view = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.activity_submain2, null);
			myfriendslistview = (ListView) view
					.findViewById(R.id.myfirendslistview);

			myfriendslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Bundle bundle = new Bundle();// 创建 email 内容
					Map<String, Object> temp = new HashMap<String, Object>();
					temp = friendList.get(position - 1);
					bundle.putString("friendName", temp.get("friendname")
							.toString());
					bundle.putString("image", temp.get("image").toString());
					Intent intent = new Intent(MainActivity.this,
							FriendsInformationActivity.class);
					intent.putExtra("key", bundle);// 封装 email
					startActivity(intent);

				}
			});

			myfriendslistview.setOnScrollListener(this);
			myfriendslistview.addHeaderView(LayoutInflater.from(
					MainActivity.this).inflate(R.layout.myfriends_listview_top,
					null));
			myfriendslistview.setAdapter(friendAdapter);

		}

		private List<Map<String, Object>> getFriendData() {
			return new ArrayList<Map<String, Object>>();
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

		private void reload() {
			friendList.clear();
			Map<String, Object> map;
			for (int i = 0; i < listshowsize; i++) {
				map = new HashMap<String, Object>();
				map.put("image", R.drawable.friend1);
				map.put("friendname", "Kitty" + i);
				friendList.add(map);
			}
		}

		private void showUpdate() {
			friendAdapter.notifyDataSetChanged();
		}
	}
}
