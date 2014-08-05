package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.RSAUtils;
import group.acm.bookshare.function.TripleDESUtil;
import group.acm.bookshare.function.User;

import java.security.interfaces.RSAPrivateKey;
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
	public static final int SCANREQUEST_BOOKCONFIRM = 2;

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

	// ����ɨ�蹦�ܵķ��ؽ����Ҫ��onActivitiyResult�л�ȡ
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (RESULT_OK == resultCode) {

			Bundle bundle = data.getExtras();
			String scanModel = bundle.getString("model");
			if(0 == scanModel.compareTo("addBook")){
				String isbn = bundle.getString("result");
				localUser.addBook(isbn, bookmanage.getAddBookHandler());
			}
			else if(0 == scanModel.compareTo("borrowBook")){
				
				String res = bundle.getString("result");
				Log.i("result",res);
				MainActivity.this.showToast(res);
				try {
					JSONObject jsonObject = new JSONObject(res);
				
					//ģ  
			        String modulus = "1095908257922794133899641353345223659509198870727619"
			        		+ "84662925904428324513840234320762060769240802226180024972009"
			        		+ "23198993652791393424108233803797411622424439308380949251312"
			        		+ "11865875997007206462274689115480894523234426618616006872199"
			        		+ "90868747713338468835352980211896324717589079982458697178916"
			        		+ "072092088274807099109";
					//˽Կָ��  
			        String private_exponent = "22924159341842905201077533779406550724341375"
			        		+ "587538246299709343177257133531223161013276333758367910015746"
			        		+ "255417162234310997774877161160836964807469968386805892084950"
			        		+ "686634327143636933869340087048479665692267235347320120424665"
			        		+ "407579323193200884292522562661901712837097758688799098085230"
			        		+ "883536351178306308687201";
			        RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent); 
					
			        //��ԭdesKey1��desKey2
			        String desKey1 = (String)jsonObject.get("desKey1");
			        String desKey2 = (String)jsonObject.get("desKey2");
			        desKey1 = RSAUtils.decryptByPrivateKey(desKey1, priKey);
			        desKey2 = RSAUtils.decryptByPrivateKey(desKey2, priKey);
			        Log.i("desKey1",desKey1);
			        Log.i("desKey2",desKey2);
			        
			        // TripleDES���ܲ���
			        
			        String contentString = (String)jsonObject.get("contentString");
			        TripleDESUtil desUtil = new TripleDESUtil(desKey1,desKey2);
			        contentString = desUtil.getDec(contentString);
			        
					Log.i("contentString",contentString);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!�����ǹ�����ҳ��ĵ��ú���!!!!!!!!!!!!!!!!!!!!!! */
	/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

	// BookListManage������鱾�б�Ľ��桢�����������й���
	private class BookListManage {

		ListView mybookslistview;
		BookListAdapter bookAdapter;
		List<Map<String, Object>> bookList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> ownList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> borrowedList = new ArrayList<Map<String, Object>>();

		private View getView() {
			initBookList();
			return mybookslistview;
		}

		private Handler getAddBookHandler() {
			return new AddBookHandler();
		}

		private void initBookList() {
			addBookDataToList(getIntent().getStringExtra("response"));
			localUser.setOwnBooks(ownList);
			localUser.setBorrowedBooks(borrowedList);
			bookAdapter = new BookListAdapter(MainActivity.this, bookList);

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

		private class BookListAdapter extends BaseAdapter {
			private Context context;
			private List<Map<String, Object>> datas;

			public BookListAdapter(Context context,
					List<Map<String, Object>> data) {
				this.datas = data;
				this.context = context;
			}

			@Override
			public int getCount() {
				return datas.size();
			}

			@Override
			public Object getItem(int position) {
				return datas.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView title;
				TextView status;
				ImageView cover;
				if (convertView == null) {
					convertView = LayoutInflater.from(context).inflate(
							R.layout.mybooks_listview_item, null);
				}

				cover = (ImageView) convertView
						.findViewById(R.id.mybookslistitem_bookimage);
				title = (TextView) convertView
						.findViewById(R.id.mybookslistitem_bookname);
				status = (TextView) convertView
						.findViewById(R.id.mybookslistitem_bookstate);

				Map<String, Object> item = datas.get(position);

				cover.setImageResource(R.drawable.default_book_big);
				title.setText((String) item.get("bookname"));
				String text = "";
				switch ((Integer) item.get("status")) {
				case Book.STATUS_BUY | Book.STATUS_BORROW:
					text = "����/�ɽ�";
					break;
				case Book.STATUS_BUY | Book.STATUS_UNBORROW:
					text = "����/���ɽ�";
					break;
				case Book.STATUS_UNBUY | Book.STATUS_BORROW:
					text = "������/�ɽ�";
					break;
				case Book.STATUS_UNBUY | Book.STATUS_UNBORROW:
					text = "������/���ɽ�";
					break;
				}

				status.setText(text);

				return convertView;
			}
		}

		private void setListener() {
			mybookslistview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (0 == position) {

						Intent intent = new Intent(MainActivity.this,CaptureActivity.class);
						intent.putExtra("model", "addBook");
						startActivityForResult(intent, SCANREQUEST_ADDBOOK);
					} else {
						Intent intent = new Intent(MainActivity.this,
								BookInformationActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString(
								"bookName",
								(String) bookList.get(position - 1).get(
										"bookname"));
						bundle.putString("bookIsbn",
								(String) bookList.get(position - 1).get("isbn"));
						bundle.putString("bookDescription", (String) bookList
								.get(position - 1).get("description"));
						bundle.putInt("bookImage", R.drawable.default_book_big);
						intent.putExtra("key", bundle);
						startActivity(intent);
					}
				}
			});

			mybookslistview.setOnItemLongClickListener(new JudgeListener());
		}

		private class ReturnBookHandler extends Handler {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					Bundle data = msg.getData();
					if (data.getInt("status") == NetAccess.STATUS_SUCCESS) {
						MainActivity.this.showToast("���ͳɹ�");
					} else {
						MainActivity.this.showToast("����ʧ��:"
								+ data.getString("response"));
					}
					break;
				case NetAccess.NETMSG_ERROR:
					MainActivity.this.showToast(msg.getData()
							.getString("error"));
					break;
				}
			}
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
					MainActivity.this.showToast(msg.getData()
							.getString("error"));
					break;
				}
			}
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
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position)
					return false;

				String[] choices = { "����", "ɾ��" };
				new AlertDialog.Builder(MainActivity.this).setTitle("Choose:")
						.setItems(choices, new ChooseListener(position - 1))
						.show();

				return true;
			}
		}

		private class ChooseListener implements DialogInterface.OnClickListener {
			private int position;

			public ChooseListener(int position) {
				this.position = position;
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i("in dialog", "touch" + Integer.toString(which));
				Map<String, Object> book = bookList.get(position);
				String owner = (String) book.get("owner");
				String holder = (String) book.get("holder");
				Log.i("long click in booklist", "owner:" + owner + "  holder:"
						+ holder);
				switch (which) {
				case 0:
					if (owner != holder) {
						Log.i("in dialog", "not equal");
						localUser.bookRequest(owner, (Integer) book.get("id"),
								"������", Inform.REQUEST_TYPE_RETURN,
								new ReturnBookHandler());
					}
					break;
				case 1:
					if (owner.equals(holder)) {
						localUser.deleteBook(bookList.get(position),
								new DeleteBookHandler());
					}
					break;
				}
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
					int id = item.getInt("id");
					String isbn = item.getString("isbn");
					String bookname = item.getString("name");
					String owner = item.getString("owner");
					String holder = item.getString("holder");
					String coverurl = "";
					int status = item.getInt("status");

					map = new HashMap<String, Object>();
					map.put("id", id);
					map.put("isbn", isbn);
					map.put("bookname", bookname);
					map.put("owner", owner);
					map.put("holder", holder);
					map.put("coverurl", R.drawable.default_book_big);
					map.put("description", item.getString("description"));
					map.put("status", status);
					this.bookList.add(map);

					Map<String, Object> omap = new HashMap<String, Object>();
					omap.put("id", item.getInt("id"));
					omap.put("isbn", item.getString("isbn"));
					omap.put("name", item.getString("name"));
					omap.put("coverurl", coverurl);
					omap.put("authors", item.getString("author"));
					omap.put("description", item.getString("description"));
					omap.put("owner", owner);
					omap.put("holder", holder);
					omap.put("status", item.getInt("status"));
					this.ownList.add(omap);
				}

				jsonarray = jsonobj.getJSONArray("borrowed_book");

				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject item = jsonarray.getJSONObject(i);
					String bookname = item.getString("name");
					String coverurl = "";
					int status = item.getInt("status");

					map = new HashMap<String, Object>();
					map.put("id", item.getInt("id"));
					map.put("isbn", item.getString("isbn"));
					map.put("owner", item.getString("owner"));
					map.put("holder", item.getString("holder"));
					map.put("bookname", bookname);
					map.put("coverurl", R.drawable.default_book_big);
					map.put("description", item.getString("description"));
					map.put("status", status);
					this.bookList.add(map);

					Map<String, Object> omap = new HashMap<String, Object>();
					omap.put("id", item.getInt("id"));
					omap.put("isbn", item.getString("isbn"));
					omap.put("name", item.getString("name"));
					omap.put("owner", item.getString("owner"));
					omap.put("holder", item.getString("holder"));
					omap.put("coverurl", coverurl);
					omap.put("authors", item.getString("author"));
					omap.put("description", item.getString("description"));
					omap.put("owner", "");
					omap.put("holder", localUser.getUserName());
					omap.put("status", item.getInt("status"));
					this.borrowedList.add(omap);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void reload(String response) {
			localUser.setOwnBooks(ownList);
			localUser.setBorrowedBooks(borrowedList);
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
					// Log.i("is_group",name+" is "+is_group+"!!!");

					if (0 == is_group)// ���ѹ�ϵ
						this.friendList.add(map);
					else
						// ������ϵ
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

			Button refresh = (Button) head
					.findViewById(R.id.button_friend_refresh);
			refresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					localUser.updateFriendship(new UpdateFriendshipHandler());
				}
			});

			Button group = (Button) head.findViewById(R.id.button_group);
			group.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, GroupActivity.class);
					startActivity(intent);

				}
			});

			addFrienView = LayoutInflater.from(MainActivity.this).inflate(
					R.layout.add_friend_alert_dialog, null);
			addFriendEdit = (EditText) addFrienView
					.findViewById(R.id.add_friend_name);
			addFriendDialog = null;
			builder = null;
			builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Friend");
			builder.setMessage("Please input your firend'account.");
			builder.setView(addFrienView);
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							String addFriendName = addFriendEdit.getText()
									.toString();
							Log.i("will add the friend'name is", addFriendName);
							addFriendEdit.setText("");
						}

					});
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							addFriendEdit.setText("");
						}
					});
			addFriendDialog = builder.create();
			Button add_friend = (Button) head
					.findViewById(R.id.button_add_friend);
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

					if (0 == is_group)// ���ѹ�ϵ
						this.friendList.add(map);
					else
						// ������ϵ
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
						friendmanage
								.reload(msg.getData().getString("response"));
						// friendmanage.friendAdapter.notifyDataSetChanged();
						MainActivity.this.showToast("ɾ���ѳɹ�");
					} else
						MainActivity.this.showToast("ɾ����ʧ��");
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
						Log.i("update_resposnse:",
								msg.getData().getString("response"));
						friendmanage
								.reload(msg.getData().getString("response"));

						MainActivity.this.showToast("���º��ѳɹ�");
					} else
						MainActivity.this.showToast("���º���ʧ��");
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


	}

	public class InformListManage {
		private ListView informlistview;
		private InformListAdapter informAdapter;

		private View getView() {
			initInformList();
			return informlistview;
		}

		private void initInformList() {
			List<Map<String, Object>> informList = localUser
					.getInitInformData();

			informAdapter = new InformListAdapter(MainActivity.this, informList);

			informlistview = (ListView) LayoutInflater.from(MainActivity.this)
					.inflate(R.layout.activity_submain_inform, null);

			informlistview.addHeaderView(getHeadView());

			setListener();

			informlistview.setAdapter(informAdapter);
		}

		private void setListener() {
			informlistview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position != 0) {
						new AlertDialog.Builder(MainActivity.this)
								.setTitle("Message")
								.setMessage(
										localUser.getInformString(localUser
												.getCurInformData().get(
														position - 1)))
								.setPositiveButton("ȷ��", null).show();
					}
				}
			});
		}

		private View getHeadView() {
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

			Button button_scan = (Button) head.findViewById(R.id.button_scan);
			button_scan.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//��ɨ�����ɨ����������ά��
					Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
					openCameraIntent.putExtra("model", "borrowBook");
					startActivityForResult(openCameraIntent, 0);
					
				}
			});
			
			Button button3 = (Button) head.findViewById(R.id.button3);
			button3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,GenerateQRCodeActivity.class);
					intent.putExtra("key", "key");
					startActivity(intent);
				}
			});

			return head;
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

		public class InformListAdapter extends BaseAdapter {
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
						if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
							MainActivity.this.showToast("���");
							informs.remove(position);
							informAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}

			public class ViewHolder {
				public TextView title;
				public TextView content;
				public Button confirm;
				public Button cancel;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder views;
				if (convertView == null) {
					views = new ViewHolder();
					convertView = LayoutInflater.from(context).inflate(
							R.layout.inform_listview_item, null);
					views.title = (TextView) convertView
							.findViewById(R.id.informlistviewitem_title);
					views.content = (TextView) convertView
							.findViewById(R.id.informlistviewitem_content);
					views.confirm = (Button) convertView
							.findViewById(R.id.informlistviewitem_confirm);
					views.cancel = (Button) convertView
							.findViewById(R.id.informlistviewitem_cancel);
					convertView.setTag(views);
				} else {
					views = (ViewHolder) convertView.getTag();
				}

				Map<String, Object> item = informs.get(position);

				int id = (Integer) item.get("id");

				Inform inform = new Inform();
				inform.setState(item, localUser.getUserName());
				int nextConfirmStatus = inform.getNextConfirmStatus();
				int nextCancelStatus = inform.getNextCancelStatus();

				if (nextConfirmStatus != Inform.EMPTY_STATUS) {
					views.confirm.setOnClickListener(new InformClickListener(
							true, item, id, nextConfirmStatus, position));
				}
				if (nextCancelStatus != Inform.EMPTY_STATUS) {
					views.cancel.setOnClickListener(new InformClickListener(
							false, item, id, nextCancelStatus, position));
				}
				Map<String, Object> showingData = inform.getContent();
				views.title.setText((String) showingData.get(Inform.TITLE));
				views.content.setText((String) showingData.get(Inform.CONTENT));
				views.confirm.setText((String) showingData.get(Inform.CONFIRM));
				views.cancel.setText((String) showingData.get(Inform.CANCEL));
				views.confirm.setVisibility((Integer) showingData
						.get(Inform.CONFIRM_VISIBILITY));
				views.cancel.setVisibility((Integer) showingData
						.get(Inform.CANCEL_VISIBILITY));

				return convertView;
			}

			public class InformClickListener implements OnClickListener {
				int id;
				int status;
				int position;
				Map<String, Object> item;
				boolean flag;

				public InformClickListener(boolean flag,
						Map<String, Object> item, int id, int status,
						int position) {
					this.flag = flag;
					this.id = id;
					this.status = status;
					this.position = position;
					this.item = item;
				}

				@Override
				public void onClick(View v) {
					if (flag
							&& status == Inform.REQUEST_STATUS_CONFIRM
							&& (Integer) item.get("status") == Inform.REQUEST_STATUS_PERMITTED) {
						if ((Integer) item.get("type") == Inform.REQUEST_TYPE_BORROW
								|| (Integer) item.get("type") == Inform.REQUEST_TYPE_RETURN) {
							if (localUser.getUserName()
									.equals(item.get("from"))) {
								// ɨ��
								//��ɨ�����ɨ����������ά��
								Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
								openCameraIntent.putExtra("model", "borrowBook");
								startActivityForResult(openCameraIntent, 0);
								Log.i("click", "ɨ��");
							} else {
								// ��ʾ��
								Log.i("click", (Integer)item.get("id")+"");
								Intent intent = new Intent(MainActivity.this,GenerateQRCodeActivity.class);
								intent.putExtra("ContentString",(Integer)item.get("id")+"");
								startActivity(intent);
								Log.i("click", "��ʾ��");
							}
							return;
						}
					}
					localUser.updateRequest(id, status, new RequestHandler(
							position));
				}
			}
		}
	}
}
