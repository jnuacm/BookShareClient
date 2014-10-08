package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.TripleDESUtil;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.util.Utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	private ImageView underlined;
	private int offset = 0;// ����ͼƬƫ����
	private int currIndex = 0;// ��ǰҳ�����
	private int bmpW;// ����ͼƬ���

	private List<TextView> textViews; // ����Textview
	private List<View> viewList; // ����ҳ��
	private ViewPager viewPager;// viewpager
	private Button mainButton; // �ײ���ť
	private BroadcastReceiver receiver;

	private User localUser;

	private BookListManage bookmanage;
	private FriendListManage friendmanage;
	private InformListManage informmanage;

	int listshowsize = 10;

	private static final int OPTIONS_ITEM_PERSONINFO = Menu.FIRST;
	private static final int OPTIONS_ITEM_SEARCH = Menu.FIRST + 1;
	private static final int OPTIONS_ITEM_EXIT = Menu.FIRST + 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		localUser = ((LocalApp) getApplication()).getUser();

		bookmanage = new BookListManage(this);
		friendmanage = new FriendListManage(this, localUser);
		informmanage = new InformListManage(this);

		viewList = new ArrayList<View>();
		// ��ʼ�����ȡview
		bookmanage.initBookList();
		viewList.add(bookmanage.getView());
		friendmanage.initFriendList();
		viewList.add(friendmanage.getView());
		informmanage.initInformList();
		viewList.add(informmanage.getView());

		/* ��������ʾ��ҳ���� */
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		InitImageView();// ��ʼ���»���
		InitTextView();
		viewPager.setAdapter(new MyViewPagerAdapter(viewList));

		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		textViews.get(currIndex).setTextColor(Color.rgb(50, 189, 189));

		// ҳ��ײ���ť���õ������
		mainButton = (Button) findViewById(R.id.main_button);
		mainButton.setOnClickListener(new BottomButtonClickListener());

		// ע����յ�����ʱ�ĸ���receiver
		receiver = new MessageUpdateReceiver();
		registerUpdateReceiver();
		informmanage.reload();

		// ����ͼƬ
		localUser.loadInitImgs(new BookImgsUpdateProcess());
		localUser.loadInitAvatar(new AvatarsUpdateProcess());
	}

	private class BookImgsUpdateProcess extends HttpProcessBase {

		@Override
		public void statusError(String response) {
		}

		@Override
		public void statusSuccess(String response) {
			bookmanage.updateDisplay();
		}

	}

	private class AvatarsUpdateProcess extends HttpProcessBase {

		@Override
		public void statusError(String response) {
		}

		@Override
		public void statusSuccess(String response) {
			friendmanage.updateDisplay();
		}

	}

	private void registerUpdateReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("group.acm.bookshare.action.UPDATEMESSAGE");
		registerReceiver(receiver, filter);
	}

	private class MessageUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					"group.acm.bookshare.action.UPDATEMESSAGE"))
				checkUpdate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		switch (currIndex) {
		case 0:
			menu.add(Menu.NONE, OPTIONS_ITEM_PERSONINFO, 1, "������Ϣ");
			menu.add(Menu.NONE, OPTIONS_ITEM_SEARCH, 2, "�����鱾");
			menu.add(Menu.NONE, OPTIONS_ITEM_EXIT, 3, "Exit");
			break;
		case 1:
			menu.add(Menu.NONE, OPTIONS_ITEM_PERSONINFO, 1, "������Ϣ");
			menu.add(Menu.NONE, OPTIONS_ITEM_SEARCH, 2, "�����鱾");
			menu.add(Menu.NONE, OPTIONS_ITEM_EXIT, 3, "Exit");
			break;
		case 2:
			menu.add(Menu.NONE, OPTIONS_ITEM_PERSONINFO, 1, "������Ϣ");
			menu.add(Menu.NONE, OPTIONS_ITEM_SEARCH, 2, "�����鱾");
			menu.add(Menu.NONE, OPTIONS_ITEM_EXIT, 3, "Exit");
			break;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTIONS_ITEM_PERSONINFO:
			localUser.getPersonInfo(new GetPersonInfoProcess());
			break;
		case OPTIONS_ITEM_SEARCH:

			break;
		case OPTIONS_ITEM_EXIT:
			localUser.logout();
			unregisterReceiver(receiver);
			PushManager.stopWork(this.getApplicationContext());
			System.exit(0);
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		checkUpdate();
		super.onResume();
	}

	private class GetPersonInfoProcess extends HttpProcessBase {
		public void before() {
			showProgressBar();
		}

		public void error(String content) {
			hideProgressBar();
			showToast(content);
		}

		@Override
		public void statusError(String response) {
			hideProgressBar();
			showToast(response);
		}

		@Override
		public void statusSuccess(String response) {
			Intent intent = new Intent(MainActivity.this,
					PersonInfoActivity.class);
			try {
				localUser.updateInfo(Friend
						.objToFriend(new JSONObject(response)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			hideProgressBar();
			startActivity(intent);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void checkUpdate() {
		if (Utils.hasUpdate(getApplicationContext())) {
			((TextView) textViews.get(2)).setText("�и���");
		} else {
			((TextView) textViews.get(2)).setText("��Ϣ");
		}
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

			// ÿ��ҳ�汻ѡ�ж�����һ�����ݸ�����ʾ
			switch (currIndex) {
			case 0:
				mainButton.setText("����鼮");
				bookmanage.updateDisplay();
				break;
			case 1:
				mainButton.setText("��Ӻ���");
				friendmanage.updateDisplay();
				break;
			case 2:
				mainButton.setText("ˢ��");
				informmanage.updateDisplay();
				break;
			}

			checkUpdate();
		}

	}

	private class BottomButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (Utils.isQuickClick())
				return;
			switch (currIndex) {
			case 0:
				Intent intent = new Intent(MainActivity.this,
						CaptureActivity.class);
				startActivityForResult(intent, Utils.ACTIVITY_REQUEST_ADDBOOK);
				break;
			case 1:
				friendmanage.addFriend();
				break;
			case 2:
				informmanage.reload();
				break;
			}
		}

	}

	public void bookListReload() {
		bookmanage.reload();
	}

	public void friendListReload() {
		friendmanage.reload();
	}

	public void InformListReload() {
		informmanage.reload();
	}

	public void showToast(String content) {
		Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
	}

	public void showProgressBar() {
		findViewById(R.id.main_progressbar).setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		findViewById(R.id.main_progressbar).setVisibility(View.INVISIBLE);
	}

	// ����ɨ�蹦�ܵķ��ؽ����Ҫ��onActivitiyResult�л�ȡ
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (RESULT_OK == resultCode) {
			Bundle bundle;
			switch (requestCode) {
			case Utils.ACTIVITY_REQUEST_ADDBOOK:
				bundle = data.getExtras();
				String isbn = bundle.getString("result");
				localUser.addBook(isbn, bookmanage.getBookChangeProgress());
				break;
			case Utils.REQUEST_SCANBOOK_UPDATESTATUS:
				bundle = data.getExtras();
				String res = bundle.getString("result");
				Log.i("result", res);
				showToast(res);
				try {
					JSONObject jsonObject = new JSONObject(res);

					String desKey1 = "ASDASDEFRGRHTTGRGEFWSP";
					String desKey2 = "IHDASHKDSJFSDKLJFKOEFJ";

					// TripleDES���ܲ���
					String contentString = (String) jsonObject
							.get("contentString");
					TripleDESUtil desUtil = new TripleDESUtil(desKey1, desKey2);
					contentString = desUtil.getDec(contentString);
					int id = Integer.parseInt(contentString);
					localUser.updateRequest(id, Inform.REQUEST_STATUS_CONFIRM,
							informmanage.getConfirmProgress(id));
					bookmanage.reload();
					Log.i("contentString", contentString);

				} catch (Exception e) {
					this.showToast(e.toString());
				}
				break;
			case Utils.ACTIVITY_REQUEST_SHOWCODE:
				informmanage.reload();
				bookmanage.reload();
				break;
			}
		}
	}
}
