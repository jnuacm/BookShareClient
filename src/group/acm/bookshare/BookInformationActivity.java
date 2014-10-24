package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.StreamProcess;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookInformationActivity extends Activity {
	private User localUser;

	Map<String, Object> detailBook;

	TextView bookNameView;
	TextView bookDescriptionView;
	TextView bookAuthorView;
	TextView bookPublisherView;
	ImageView backButton;
	ImageView bookImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		localUser = ((LocalApp) getApplication()).getUser();

		bookNameView = (TextView) findViewById(R.id.book_name);
		bookDescriptionView = (TextView) findViewById(R.id.book_description);
		bookAuthorView = (TextView) findViewById(R.id.book_author);
		bookPublisherView = (TextView) findViewById(R.id.book_publisher);
		backButton = (ImageView) findViewById(R.id.book_info_bar_img);
		bookImageView = (ImageView) findViewById(R.id.book_image);

		detailBook = getDetailBook();

		bookNameView.setText((String) detailBook.get(Book.NAME));
		bookDescriptionView.setText("简介： " + detailBook.get(Book.DESCRIPTION));
		bookAuthorView.setText("作者:" + detailBook.get(Book.AUTHOR));
		bookPublisherView.setText("出版社:" + detailBook.get(Book.PUBLISHER));
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bookImageView.setOnClickListener(new BookImgClick());
		BookImgProcess tmp = new BookImgProcess();
		localUser.getUrlBookImg((String) detailBook.get(Book.IMG_URL_MEDIUM),
				tmp, tmp);
	}

	private class BookImgProcess extends HttpProcessBase implements
			StreamProcess {
		private Bitmap bm;

		@Override
		public void statusError(String response) {
			Toast.makeText(BookInformationActivity.this, "没有中等图",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			bookImageView.setImageBitmap(bm);
		}

		@Override
		public String getResponse(int status, InputStream responseStream) {
			bm = BitmapFactory.decodeStream(responseStream);
			return "";
		}
	}

	private class BookImgClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			BookImgProcess tmp = new BookImgProcess();
			localUser.getUrlBookImg(
					(String) detailBook.get(Book.IMG_URL_LARGE), tmp, tmp);
		}

	}

	public Map<String, Object> getDetailBook() {
		Intent intent = getIntent();// 收取 email
		String response = intent.getStringExtra(NetAccess.RESPONSE);
		String bookObj = intent.getStringExtra("person_book");

		Map<String, Object> doubanBook = Book.doubanStrToBook(response);
		Map<String, Object> book = new HashMap<String, Object>();
		try {
			book = Book.objToBook(new JSONObject(bookObj));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Book.bookToDetail(book, doubanBook);
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));

		listView.setLayoutParams(params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.book_information, menu);
		return true;
	}
}
