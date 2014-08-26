package group.acm.bookshare;

import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.RSAUtils;
import group.acm.bookshare.function.TripleDESUtil;
import group.acm.bookshare.function.User;

import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	private ImageView underlined;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	private List<TextView> textViews;

	private List<View> viewList;
	private ViewPager viewPager;// viewpager

	private User localUser;

	private BookListManage bookmanage = new BookListManage(this);
	private FriendListManage friendmanage = new FriendListManage(this);
	private InformListManage informmanage = new InformListManage(this);

	int listshowsize = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		localUser = ((LocalApp) getApplication()).getUser();

		viewList = new ArrayList<View>();
		bookmanage.initBookList();
		viewList.add(bookmanage.getView());
		friendmanage.initFriendList();
		viewList.add(friendmanage.getView());
		informmanage.initInformList();
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

	// 调用扫描功能的返回结果需要在onActivitiyResult中获取
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (RESULT_OK == resultCode) {
			Bundle bundle = data.getExtras();
			String scanModel = bundle.getString("model");
			if (0 == scanModel.compareTo("addBook")) {
				String isbn = bundle.getString("result");
				localUser.addBook(isbn, bookmanage.getAddBookHandler());
			} else if (0 == scanModel.compareTo("borrowBook")) {

				String res = bundle.getString("result");
				Log.i("result", res);
				showToast(res);
				try {
					JSONObject jsonObject = new JSONObject(res);

					// 模
					String modulus = "1095908257922794133899641353345223659509198870727619"
							+ "84662925904428324513840234320762060769240802226180024972009"
							+ "23198993652791393424108233803797411622424439308380949251312"
							+ "11865875997007206462274689115480894523234426618616006872199"
							+ "90868747713338468835352980211896324717589079982458697178916"
							+ "072092088274807099109";
					// 私钥指数
					String private_exponent = "22924159341842905201077533779406550724341375"
							+ "587538246299709343177257133531223161013276333758367910015746"
							+ "255417162234310997774877161160836964807469968386805892084950"
							+ "686634327143636933869340087048479665692267235347320120424665"
							+ "407579323193200884292522562661901712837097758688799098085230"
							+ "883536351178306308687201";
					RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus,
							private_exponent);

					// 还原desKey1和desKey2
					String desKey1 = (String) jsonObject.get("desKey1");
					String desKey2 = (String) jsonObject.get("desKey2");
					desKey1 = RSAUtils.decryptByPrivateKey(desKey1, priKey);
					desKey2 = RSAUtils.decryptByPrivateKey(desKey2, priKey);
					Log.i("desKey1", desKey1);
					Log.i("desKey2", desKey2);

					// TripleDES解密部分

					String contentString = (String) jsonObject
							.get("contentString");
					TripleDESUtil desUtil = new TripleDESUtil(desKey1, desKey2);
					contentString = desUtil.getDec(contentString);
					int id = Integer.parseInt(contentString);
					localUser.updateRequest(id, Inform.REQUEST_STATUS_CONFIRM,
							informmanage.getConfirmHandler(id));
					Log.i("contentString", contentString);

				} catch (Exception e) {
					this.showToast(e.toString());
				}
			}
		}
	}
}
