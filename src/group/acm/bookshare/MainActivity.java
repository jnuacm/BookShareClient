package group.acm.bookshare;

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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	public static final int SCANREQUEST_ADDBOOK = 1;

	private ImageView underlined;
	private int offset = 0;// ����ͼƬƫ����
	private int currIndex = 0;// ��ǰҳ�����
	private int bmpW;// ����ͼƬ���

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

		/* ��������ʾ��ҳ���� */
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		InitImageView();// ��ʼ���»���
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
		private boolean isCreated[] = { false, false, false };

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;// ���췽�������������ǵ�ҳ���������ȽϷ��㡣
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// container.removeView(mListViews.get(position));// ɾ��ҳ��
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // �����������ʵ����ҳ��
			if (isCreated[position]) {
				return mListViews.get(position);
			} else {
				container.addView(mListViews.get(position), position);// ���ҳ��
				isCreated[position] = true;
				return mListViews.get(position);
			}
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
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!�����ǹ�����ҳ��ĵ��ú���!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

	// BookListManage������鱾�б�Ľ��桢�����������й���
	private class BookListManage {

		ListView mybookslistview;
		SimpleAdapter bookAdapter;
		List<Map<String, Object>> bookList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> ownList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> borrowedList = new ArrayList<Map<String, Object>>();
	

		/*
		 * ˢ�´��� private boolean isFirstRow = false; private boolean isLastRow =
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
						MainActivity.this.showToast("��ӳɹ�");
					} else
						MainActivity.this.showToast("���ʧ��:"
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
						MainActivity.this.showToast("ɾ��ɹ�");
					} else
						MainActivity.this.showToast("ɾ��ʧ��");
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
			addBookDataToList(response);
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

		public View getView() {
			initFriendList();
			return myfriendslistview;
		}
		
		private void addFriendDataToList(String response) {
			this.friendList.clear();
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("friend");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String name = item.getString("name");
					String email = item.getString("email");
					String area = item.getString("area");

					map = new HashMap<String, Object>();
					map.put("name", name);
					map.put("email", email);
					map.put("area", area);
					
					this.friendList.add(map);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void initFriendList() {
			addFriendDataToList(getIntent().getStringExtra("response"));
			
			for (int i = 0; i < 2; i++) {//���ز���
				
				String name = "Kitty"+String.valueOf(i);
				String email = "999@qq.com";
				String area = "ZH";

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", name);
				map.put("email", email);
				map.put("area", area);
				map.put("image", R.drawable.friend1);
				this.friendList.add(map);
			}
			
			User user = ((LocalApp) getApplication()).getUser();
			user.setFriend(friendList);
			
			friendAdapter = new SimpleAdapter(MainActivity.this, friendList,
					R.layout.myfriends_listview_item, new String[] { "image",
							"name" }, new int[] {
							R.id.myfriendslistitem_friendimage,
							R.id.myfriendslistitem_friendname });

			View view = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.activity_submain_friend, null);
			myfriendslistview = (ListView) view
					.findViewById(R.id.myfirendslistview);

			setListener();
			
			myfriendslistview.addHeaderView(LayoutInflater.from(
					MainActivity.this).inflate(R.layout.myfriends_listview_top,
					null));
			myfriendslistview.setAdapter(friendAdapter);
		}

		private void setListener() {
			myfriendslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (0 == position) {
						//��ʵ��
					} else{
						Intent intent = new Intent(MainActivity.this,
								FriendsInformationActivity.class);
						
						Bundle bundle = new Bundle();
						bundle.putString("name",friendList.get(position-1).get("name").toString());  
						bundle.putString("area", friendList.get(position-1).get("area").toString());
						bundle.putString("email", friendList.get(position-1).get("email").toString());
						bundle.putString("image", friendList.get(position-1).get("image").toString());
						
						intent.putExtra("key",bundle);
						startActivity(intent);
					}
				}
			});

			//myfriendslistview.setOnItemLongClickListener(new JudgeListener());
		}
		
		
		
		private List<Map<String, Object>> getFriendData() {
			return new ArrayList<Map<String, Object>>();
		}

		private void loadFriendData() {
			/*Map<String, Object> map;
			for (int i = 0; i < listshowsize; i++) {
				map = new HashMap<String, Object>();
				map.put("image", R.drawable.friend1);
				map.put("friendname", "Kitty" + i);
				friendList.add(map);
			}*/
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

	private class InformListManage {
		ListView informlistview;
		SimpleAdapter informAdapter;
		List<Map<String, Object>> informList = new ArrayList<Map<String, Object>>();

		private void initInformList() {
			informList = new ArrayList<Map<String, Object>>();
			informAdapter = new SimpleAdapter(MainActivity.this, informList,
					R.layout.inform_listview_item, new String[] { "title",
							"content", "confirm", "cancel" }, new int[] {
							R.id.informlistviewitem_title,
							R.id.informlistviewitem_content,
							R.id.informlistviewitem_confirm,
							R.id.informlistviewitem_cancel });

			informlistview = (ListView) LayoutInflater.from(MainActivity.this)
					.inflate(R.layout.activity_submain_inform, null);

			View head = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.inform_listview_top, null);
			Button refresh = (Button) head.findViewById(R.id.button_refresh);
			refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					localUser.getSendInformList(new Handler() {
						public void handleMessage(Message msg) {
							switch (msg.what) {
							case NetAccess.NETMSG_AFTER:
								if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
									addSendDataToList(msg.getData().getString(
											"response"));
									localUser
											.getReceiveInformList(new Handler() {
												public void handleMessage(
														Message msg) {
													switch (msg.what) {
													case NetAccess.NETMSG_AFTER:
														if (msg.getData()
																.getInt("status") == NetAccess.STATUS_SUCCESS) {
															addReceiveDataToList(msg
																	.getData()
																	.getString(
																			"response"));
															informmanage.informAdapter
																	.notifyDataSetChanged();
														}
														break;
													}
												}
											});
								}
								break;
							}
						}
					});
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

		private void addSendDataToList(String response) {
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("own_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("status", status);
					this.informList.add(map);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void addReceiveDataToList(String response) {
			Map<String, Object> map = new HashMap<String, Object>();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("own_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String status = item.getString("status");

					map = new HashMap<String, Object>();
					map.put("status", status);
					this.informList.add(map);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private View getView() {
			initInformList();
			return informlistview;
		}
	}
}
