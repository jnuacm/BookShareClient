package group.acm.bookshare;

import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	public static final int SCANREQUEST_ADDBOOK = 1;

	private ImageView underlined;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	private List<TextView> textViews;

	private List<View> viewList;
	private ViewPager viewPager;// viewpager

	private User localUser;

	private BookListManage bookmanage = new BookListManage();
	private FriendListManage friendmanage = new FriendListManage();
	private InformListManage informmanage = new InformListManage();

	int listshowsize = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		localUser = ((LocalApp) getApplication()).getUser();

		viewList = new ArrayList<View>();
		viewList.add(bookmanage.getView());
		viewList.add(friendmanage.getView());
		viewList.add(informmanage.getView());

		/* 以下是显示翻页部分 */
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		InitImageView();// 初始化下划线
		InitTextView();
		viewPager.setAdapter(new MyViewPagerAdapter(viewList));

		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		textViews.get(currIndex).setTextColor(Color.rgb(50, 189, 189));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sort:
			break;
		case R.id.action_check_own:
			break;
		case R.id.action_check_borrow:
			break;
		}
		return false;
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
		private boolean isCreated[] = { false, false, false };

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// container.removeView(mListViews.get(position));// 删除页卡
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			if (isCreated[position]) {
				return mListViews.get(position);
			} else {
				container.addView(mListViews.get(position), position);// 添加页卡
				isCreated[position] = true;
				return mListViews.get(position);
			}
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
			localUser.addBook(isbn, bookmanage.getAddBookHandler());
		}
	}

	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!以下是构造子页面的调用函数!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

	// BookListManage负责对书本列表的界面、动作交互进行管理
	private class BookListManage {

		ListView mybookslistview;
		SimpleAdapter bookAdapter;
		List<Map<String, Object>> bookList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> ownList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> borrowedList = new ArrayList<Map<String, Object>>();

		/*
		 * 刷新代码 private boolean isFirstRow = false; private boolean isLastRow =
		 * false;
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) { if
		 * (visibleItemCount >= totalItemCount) { isLastRow = false; return; }
		 * if (firstVisibleItem + visibleItemCount == totalItemCount &&
		 * totalItemCount > 0) { isLastRow = true; } if (0 == firstVisibleItem
		 * && totalItemCount > 0) { isFirstRow = true; } }
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { if (isFirstRow && scrollState ==
		 * AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
		 * Log.i("onscrollstatechanged", "firstrow"); // bookmanage.reload();
		 * isFirstRow = false; } else if (isLastRow && scrollState ==
		 * AbsListView.OnScrollListener.SCROLL_STATE_IDLE) { isLastRow = false;
		 * Log.i("onscrollstatechanged", "lastrow");
		 * 
		 * } }
		 */

		private View getView() {
			initBookList();
			return mybookslistview;
		}

		private Handler getAddBookHandler() {
			return new AddBookHandler();
		}

		private class AddBookHandler extends Handler {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					Bundle data = msg.getData();
					if (data.getInt("status") == NetAccess.STATUS_SUCCESS) {
						bookmanage.reload(data.getString("response"));
						MainActivity.this.showToast("添加成功");
					} else
						MainActivity.this.showToast("添加失败:"
								+ data.getString("response"));
					break;
				case NetAccess.NETMSG_ERROR:
					MainActivity.this.showToast(msg.getData().getString(
							"response"));
					break;
				}
			}
		}

		public class BookListAdapter extends BaseAdapter {
			private Context context;
			private LayoutInflater inflater;
			private List<? extends Map<String, ?>> data;
			private int resource;
			private String[] from;
			private int[] to;

			public BookListAdapter(Context context,
					List<? extends Map<String, ?>> data, int resource,
					String[] from, int[] to) {
				this.context = context;
				this.data = data;
				this.resource = resource;
				this.from = from;
				this.to = to;
				inflater = LayoutInflater.from(this.context);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return data.size();
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				ViewHolder holder;
				if (convertView == null) {
					convertView = inflater.inflate(
							R.layout.mybooks_listview_item, null);
					holder = new ViewHolder();
					int url = (Integer) data.get(position).get("coverurl");
					String name = (String) data.get(position).get("bookname");
					String state = (String) data.get(position).get("state");
					holder.cover = (ImageView) convertView
							.findViewById(R.id.mybookslistitem_bookimage);

					holder.cover.setImageResource(url);
					holder.name = (TextView) convertView
							.findViewById(R.id.mybookslistitem_bookname);
					holder.name.setText(name);

					holder.state = (TextView) convertView
							.findViewById(R.id.mybookslistitem_bookstate);
					holder.state.setText(state);

					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				return convertView;
			}

			public final class ViewHolder {
				public ImageView cover;
				public TextView name;
				public TextView state;
			}

		}

		private void initBookList() {
			addBookDataToList(getIntent().getStringExtra("response"));
			localUser.setOwnBooks(ownList);
			localUser.setBorrowedBooks(borrowedList);
			bookAdapter = new SimpleAdapter(MainActivity.this, bookList,
					R.layout.mybooks_listview_item, new String[] { "coverurl",
							"bookname", "state" }, new int[] {
							R.id.mybookslistitem_bookimage,
							R.id.mybookslistitem_bookname,
							R.id.mybookslistitem_bookstate });

			View view = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.activity_submain_book, null);
			mybookslistview = (ListView) view
					.findViewById(R.id.mybookslistview);

			setListener();

			mybookslistview.addHeaderView(LayoutInflater
					.from(MainActivity.this).inflate(
							R.layout.mybooks_listview_top, null));
			mybookslistview.setAdapter(bookAdapter);

		}

		private void setListener() {
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

			mybookslistview.setOnItemLongClickListener(new JudgeListener());
		}

		private class DeleteBookHandler extends Handler {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					break;
				case NetAccess.NETMSG_AFTER:
					if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
						bookmanage.reload(msg.getData().getString("response"));
						bookmanage.bookAdapter.notifyDataSetChanged();
						MainActivity.this.showToast("删书成功");
					} else
						MainActivity.this.showToast("删书失败");
					break;
				case NetAccess.NETMSG_ERROR:
					MainActivity.this.showToast(msg.getData()
							.getString("error"));
					break;
				}
			}
		}

		private class JudgeListener implements OnItemLongClickListener {
			private int position;

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				this.position = position;
				if (0 == position)
					return false;

				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Confirm!")
						.setMessage("Are you sure to delete this book?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										localUser.deleteBook(
												bookmanage.bookList
														.get(JudgeListener.this.position - 1),
												new DeleteBookHandler());
									}

								}).setNegativeButton("No", null).show();

				return true;
			}
		}

		private void addBookDataToList(String response) {
			this.bookList.clear();
			this.ownList.clear();
			this.borrowedList.clear();
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("own_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String id = item.getString("id");
					String isbn = item.getString("isbn");
					String bookname = item.getString("name");
					String coverurl = "";
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("id", id);
					map.put("isbn", isbn);
					map.put("bookname", bookname);
					map.put("coverurl", R.drawable.default_book_big);
					map.put("status", status);
					this.bookList.add(map);

					Map<String, Object> omap = new HashMap<String, Object>();
					omap.put("id", item.getString("id"));
					omap.put("isbn", item.getString("isbn"));
					omap.put("name", item.getString("name"));
					omap.put("coverurl", coverurl);
					omap.put("authors", item.getString("author"));
					omap.put("description", item.getString("description"));
					omap.put("owner", localUser.getUserName());
					omap.put("holder", localUser.getUserName());
					omap.put("status", item.getString("status"));
					this.ownList.add(omap);
				}

				jsonarray = jsonobj.getJSONArray("borrowed_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String bookname = item.getString("name");
					String coverurl = "";
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("id", item.getString("id"));
					map.put("isbn", item.getString("isbn"));
					map.put("bookname", bookname);
					map.put("coverurl", R.drawable.default_book_big);
					map.put("status", status);
					this.bookList.add(map);

					Map<String, Object> omap = new HashMap<String, Object>();
					omap.put("id", item.getString("id"));
					omap.put("isbn", item.getString("isbn"));
					omap.put("name", item.getString("name"));
					omap.put("coverurl", coverurl);
					omap.put("authors", item.getString("author"));
					omap.put("description", item.getString("description"));
					omap.put("owner", "");
					omap.put("holder", localUser.getUserName());
					omap.put("status", item.getString("status"));
					this.borrowedList.add(omap);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void reload() {
			localUser.getBookList(new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case NetAccess.NETMSG_AFTER:
						if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
							addBookDataToList(msg.getData().getString(
									"response"));
							localUser.setOwnBooks(ownList);
							localUser.setBorrowedBooks(borrowedList);
							bookmanage.showUpdate();
						}
						break;
					case NetAccess.NETMSG_ERROR:
						break;
					}
				}
			});
		}

		public void reload(String response) {
			localUser.setOwnBooks(ownList);
			localUser.setBorrowedBooks(borrowedList);
			bookAdapter.notifyDataSetChanged();
		}

		private void showUpdate() {
			bookAdapter.notifyDataSetChanged();
		}
	}

	private class FriendListManage {
		ListView myfriendslistview;
		SimpleAdapter friendAdapter;
		List<Map<String, Object>> friendList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
		View addFrienView;
		EditText addFriendEdit;
		AlertDialog addFriendDialog;  
        AlertDialog.Builder builder;

		public View getView() {
			initFriendList();
			return myfriendslistview;
		}

		private void addFriendDataToList(String response) {
			this.friendList.clear();
			this.groupList.clear();
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("friend");

				for (int i = 0; i < jsonarray.length(); i++) {

					JSONObject item = jsonarray.getJSONObject(i);
					String name = item.getString("username");
					String email = item.getString("email");
					String area = item.getString("area");
					int is_group = item.getInt("is_group");
					
					map = new HashMap<String, Object>();
					map.put("username", name);
					map.put("email", email);
					map.put("area", area);
					map.put("image", R.drawable.friend1);
					map.put("is_group", is_group);

					if (0 == is_group)// 朋友关系
						this.friendList.add(map);
					else			  // 组属关系
						this.groupList.add(map);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		private void initFriendList() {
			View head = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.myfriends_listview_top, null);
			
			Button refresh = (Button)head.findViewById(R.id.button_friend_refresh);
			refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					localUser.updateFriendship(new UpdateFriendshipHandler());
					
				}
			});
			
			Button group = (Button)head.findViewById(R.id.button_group);
			group.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, GroupActivity.class);
					startActivity(intent);
				
				}
			});
			
			addFrienView =  LayoutInflater.from(MainActivity.this).inflate(R.layout.add_friend_alert_dialog, null);
			addFriendEdit =(EditText)addFrienView.findViewById(R.id.add_friend_name);
			addFriendDialog = null;  
	        builder = null;   
	        builder = new AlertDialog.Builder(MainActivity.this);  
	        builder.setTitle("Friend");
	        builder.setMessage("Please input your firend'account.");
	        builder.setView(addFrienView);
	        builder.setPositiveButton("Yes",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					
					String addFriendName = addFriendEdit.getText().toString();
					Log.i("will add the friend'name is",addFriendName);
					addFriendEdit.setText("");
				}

			});
	        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					addFriendEdit.setText("");
				}

			});
	        addFriendDialog = builder.create();
			Button add_friend = (Button)head.findViewById(R.id.button_add_friend);
			add_friend.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addFriendDialog.show();  
				}
			});
			
			addFriendDataToList(getIntent().getStringExtra("response"));

			User user = ((LocalApp) getApplication()).getUser();
			user.setFriend(friendList);
			user.setGroup(groupList);

			friendAdapter = new SimpleAdapter(MainActivity.this, friendList,
					R.layout.myfriends_listview_item, new String[] { "image",
							"username" }, new int[] {
							R.id.myfriendslistitem_friendimage,
							R.id.myfriendslistitem_friendname });

			View view = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.activity_submain_friend, null);
			myfriendslistview = (ListView) view
					.findViewById(R.id.myfirendslistview);

			setListener();

			myfriendslistview.addHeaderView(head);
			myfriendslistview.setAdapter(friendAdapter);
			myfriendslistview.setOnItemLongClickListener(new JudgeListener());
			
		}
		
		public void reload(String response) {
			this.friendList.clear();
			this.groupList.clear();
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				JSONArray jsonarray = new JSONArray(response);
				for (int i = 0; i < jsonarray.length(); i++) {

					JSONObject item = jsonarray.getJSONObject(i);
					String name = item.getString("username");
					String email = item.getString("email");
					String area = item.getString("area");
					int is_group = item.getInt("is_group");
					
					map = new HashMap<String, Object>();
					map.put("username", name);
					map.put("email", email);
					map.put("area", area);
					map.put("image", R.drawable.friend1);
					map.put("is_group", is_group);

					if (0 == is_group)// 朋友关系
						this.friendList.add(map);
					else			  // 组属关系
						this.groupList.add(map);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			localUser.setFriend(friendList);
			localUser.setGroup(groupList);
			friendAdapter.notifyDataSetChanged();
		}
		
		private class DeleteFriendHandler extends Handler {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					break;
				case NetAccess.NETMSG_AFTER:
					if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
						friendmanage.reload(msg.getData().getString("response"));
						//friendmanage.friendAdapter.notifyDataSetChanged();
						MainActivity.this.showToast("删好友成功");
					} else
						MainActivity.this.showToast("删好友失败");
					break;
				case NetAccess.NETMSG_ERROR:
					MainActivity.this.showToast(msg.getData()
							.getString("error"));
					break;
				}
			}
		}
		
		private class UpdateFriendshipHandler extends Handler {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					break;
				case NetAccess.NETMSG_AFTER:
					if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
						Log.i("update_resposnse:",msg.getData().getString("response"));
						friendmanage.reload(msg.getData().getString("response"));
						
						MainActivity.this.showToast("更新好友成功");
					} else
						MainActivity.this.showToast("更新好友失败");
					break;
				case NetAccess.NETMSG_ERROR:
					MainActivity.this.showToast(msg.getData()
							.getString("error"));
					break;
				}
			}
		}
		
		private class JudgeListener implements OnItemLongClickListener {
			private int position;

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				this.position = position;
				if (0 == position)
					return false;

				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Confirm!")
						.setMessage("Are you sure to delete this friend?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										localUser.deleteFriend(
												friendmanage.friendList
														.get(JudgeListener.this.position - 1),
												new DeleteFriendHandler());
									}

								}).setNegativeButton("No", null).show();

				return true;
			}
		}

		private void setListener() {
			myfriendslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (0 == position) {
					} else {
						Intent intent = new Intent(MainActivity.this,
								FriendsInformationActivity.class);

						Bundle bundle = new Bundle();
						bundle.putString("name", friendList.get(position - 1)
								.get("username").toString());
						bundle.putString("area", friendList.get(position - 1)
								.get("area").toString());
						bundle.putString("email", friendList.get(position - 1)
								.get("email").toString());
						bundle.putString("image", friendList.get(position - 1)
								.get("image").toString());

						intent.putExtra("key", bundle);
						startActivity(intent);
					}
				}
			});

			
		}
		private void showUpdate() {
			friendAdapter.notifyDataSetChanged();
		}
		
	}

	private class InformListManage {
		ListView informlistview;
		InformListAdapter informAdapter;

		private class InformListAdapter extends BaseAdapter {
			List<Map<String, Object>> informs;
			Context context;

			public InformListAdapter(Context context,
					List<Map<String, Object>> data) {
				this.context = context;
				informs = data;
			}

			@Override
			public int getCount() {
				return informs.size();
			}

			@Override
			public Object getItem(int position) {
				return informs.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			public class ViewHolder {
				TextView title;
				TextView content;
				Button confirm;
				Button cancel;
			}

			public class InformClickListener implements OnClickListener {
				int id;
				int status;
				int position;

				public InformClickListener(int id, int status, int position) {
					this.id = id;
					this.status = status;
					this.position = position;
				}

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					localUser.updateRequest(id, status, new RequestHandler(
							position));
				}
			}

			public class RequestHandler extends Handler {
				private int position;

				public RequestHandler(int position) {
					this.position = position;
				}

				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case NetAccess.NETMSG_ERROR:
						MainActivity.this.showToast(msg.getData().getString(
								"error"));
					case NetAccess.NETMSG_AFTER:
						if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS)
							informs.remove(position);
						break;
					}
				}
			}

			private InformState getCurState(Map<String, Object> item,
					String username) {
				switch ((Integer) item.get("status")) {
				case Inform.REQUEST_STATUS_UNPROCESSED:
					if (username.equals(item.get("from"))) {
						return new UnprocessFromState();
					} else {
						return new UnprocessToState();
					}
				case Inform.REQUEST_STATUS_PERMITTED:
					return new PermittedFromState();
				case Inform.REQUEST_STATUS_REFUSED:
					return new RefusedFromState();
				}
				return null;
			}

			public abstract class InformState {
				public abstract void setView(ViewHolder holder,
						Map<String, Object> item, int position);
			}

			public class UnprocessFromState extends InformState {

				@Override
				public void setView(ViewHolder holder,
						Map<String, Object> item, int position) {
					holder.title.setText("未处理消息:");
					holder.content.setText((String) item.get("description"));
					holder.confirm.setVisibility(View.INVISIBLE);
					holder.cancel.setVisibility(View.INVISIBLE);
				}

			}

			public class UnprocessToState extends InformState {

				@Override
				public void setView(ViewHolder holder,
						Map<String, Object> item, int position) {
					// TODO Auto-generated method stub
					int id = (Integer) item.get("id");
					holder.title.setText((String) item.get("未处理消息:"));
					holder.content.setText((String) item.get("description"));
					holder.confirm.setText("确认");
					holder.cancel.setText("取消");

					holder.confirm.setOnClickListener(new InformClickListener(
							id, Inform.REQUEST_STATUS_PERMITTED, position));
					holder.cancel.setOnClickListener(new InformClickListener(
							id, Inform.REQUEST_STATUS_REFUSED, position));
				}
			}

			public class PermittedFromState extends InformState {

				@Override
				public void setView(ViewHolder holder,
						Map<String, Object> item, int position) {
					// TODO Auto-generated method stub
					int id = (Integer) item.get("id");

					holder.title.setText("已确认");
					holder.content.setText((String) item.get("description"));
					holder.confirm.setText("确认");
					holder.cancel.setText("取消");

					holder.confirm.setOnClickListener(new InformClickListener(
							id, Inform.REQUEST_STATUS_CONFIRM, position));
					holder.cancel.setVisibility(View.INVISIBLE);
				}

			}

			public class RefusedFromState extends InformState {

				@Override
				public void setView(ViewHolder holder,
						Map<String, Object> item, int position) {
					// TODO Auto-generated method stub
					int id = (Integer) item.get("id");

					holder.title.setText("已取消");
					holder.content.setText((String) item.get("description"));
					holder.confirm.setText("确认");
					holder.cancel.setText("取消");

					holder.confirm.setOnClickListener(new InformClickListener(
							id, Inform.REQUEST_STATUS_CONFIRM, position));
					holder.cancel.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder views = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(context).inflate(
							R.layout.inform_listview_item, null);
					views = new ViewHolder();
					views.title = (TextView) convertView
							.findViewById(R.id.informlistviewitem_title);
					views.content = (TextView) convertView
							.findViewById(R.id.informlistviewitem_content);
					views.confirm = (Button) convertView
							.findViewById(R.id.informlistviewitem_confirm);
					views.cancel = (Button) convertView
							.findViewById(R.id.informlistviewitem_cancel);
				}
				Map<String, Object> item = informs.get(position);

				InformState curState = getCurState(item,
						localUser.getUserName());
				curState.setView(views, item, position);

				return convertView;
			}
		}

		private void initInformList() {
			List<Map<String, Object>> informList = localUser
					.getInitInformData();

			informAdapter = new InformListAdapter(MainActivity.this, informList);

			informlistview = (ListView) LayoutInflater.from(MainActivity.this)
					.inflate(R.layout.activity_submain_inform, null);

			View head = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.inform_listview_top, null);
			Button refresh = (Button) head.findViewById(R.id.button_refresh);
			refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					localUser.getCurInformData().clear();
					localUser.getSendInformList(new SendInformHandler());
				}
			});

			Button button2 = (Button) head.findViewById(R.id.button2);
			button2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(MainActivity.this, "button2",
							Toast.LENGTH_SHORT).show();
				}
			});

			informlistview.addHeaderView(head);
			informlistview.setAdapter(informAdapter);
		}

		private class SendInformHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
						localUser.addSendDataToList(msg.getData().getString(
								"response"));
						localUser
								.getReceiveInformList(new ReceiveInformHandler());
					}
					break;
				}
			}
		}

		private class ReceiveInformHandler extends Handler {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
						localUser.addReceiveDataToList(msg.getData().getString(
								"response"));
						informmanage.informAdapter.notifyDataSetChanged();
					}
					break;
				}
			}
		}

		private View getView() {
			initInformList();
			return informlistview;
		}
	}
}
