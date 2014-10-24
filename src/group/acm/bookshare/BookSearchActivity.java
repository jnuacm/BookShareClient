package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BookSearchActivity extends Activity {
	private User localUser;

	private RelativeLayout activityLayout;
	private Spinner typeSpinner; // 下拉列表
	private EditText searchText; // 搜索输入框
	private Button searchButton; // 搜索按钮
	private ListView searchListview; // 搜索显示列表，暂定显示搜索记录

	private StrListAdapter historyAdapter; // 历史记录的适配器

	private int curChoosed = 0; // 记录当前选中搜索类型

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_search);

		localUser = ((LocalApp) getApplication()).getUser();

		activityLayout = (RelativeLayout) findViewById(R.id.book_search_layout);
		typeSpinner = (Spinner) findViewById(R.id.search_spinner);
		searchText = (EditText) findViewById(R.id.search_edittext);
		searchButton = (Button) findViewById(R.id.search_button);
		searchListview = new ListView(this);

		historyAdapter = new StrListAdapter(this, getHistoryList());

		setSpinner();
		searchListview.setAdapter(historyAdapter);
		searchListview.setOnItemClickListener(new HistoryClickListener());
		searchButton.setOnClickListener(new SearchClickListener(
				new SearchProcess()));

		/**
		 * 添加ListView显示参数
		 */
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(
				getResources().getDimensionPixelSize(
						R.dimen.activity_horizontal_margin),
				0,
				getResources().getDimensionPixelSize(
						R.dimen.activity_horizontal_margin), 0);
		params.addRule(RelativeLayout.BELOW, R.id.history_text);
		activityLayout.addView(searchListview, params);
	}

	/**
	 * 添加记录item，且不重复，按输入顺序排列
	 * 
	 * @param content
	 */
	private void addHistory(String content) {
		SharedPreferences sp = getSharedPreferences(localUser.getUsername()
				+ "_history", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		Set<String> data = new LinkedHashSet<String>();
		List<String> list = historyAdapter.getDatas();
		for (String item : list) {
			data.add(item);
		}
		data.add(content);
		switch (curChoosed) {
		case 0:
			editor.putStringSet(Book.NAME, data);
			break;
		case 1:
			editor.putStringSet(Book.AUTHOR, data);
			break;
		case 2:
			editor.putStringSet(Book.ISBN, data);
			break;
		case 3:
			editor.putStringSet(Book.PUBLISHER, data);
			break;
		}
		editor.commit();
	}

	/**
	 * 从文件读取当前用户历史记录
	 * 
	 * @return
	 */
	private List<String> getHistoryList() {
		List<String> list = new ArrayList<String>();
		SharedPreferences sp = getSharedPreferences(localUser.getUsername()
				+ "_history", Activity.MODE_PRIVATE);

		Set<String> data = new LinkedHashSet<String>();

		switch (curChoosed) {
		case 0:
			data = sp.getStringSet(Book.NAME, new LinkedHashSet<String>());
			break;
		case 1:
			data = sp.getStringSet(Book.AUTHOR, new LinkedHashSet<String>());
			break;
		case 2:
			data = sp.getStringSet(Book.ISBN, new LinkedHashSet<String>());
			break;
		case 3:
			data = sp.getStringSet(Book.PUBLISHER, new LinkedHashSet<String>());
			break;
		}

		for (String item : data) {
			list.add(item);
		}

		return list;
	}

	/**
	 * 构建spinner
	 */
	private void setSpinner() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("书名");
		list.add("作者");
		list.add("isbn");
		list.add("出版社");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(adapter);
		typeSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long id) {
						curChoosed = position;
						historyAdapter.setDatas(getHistoryList());
						historyAdapter.updateAdapter();
					}

					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
	}

	/**
	 * 当历史记录item点击相应
	 */
	private class HistoryClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String content = (String) parent.getItemAtPosition(position);
			searchText.setText(content);
		}
	}

	/**
	 * 搜索按钮点击响应
	 */
	private class SearchClickListener implements OnClickListener {
		NetProgress progress;

		public SearchClickListener(NetProgress progress) {
			this.progress = progress;
		}

		@Override
		public void onClick(View v) {
			String content = searchText.getText().toString();
			if (content.length() <= 0)
				return;
			String isbn = "";
			String name = "";
			String author = "";
			String publisher = "";
			switch (curChoosed) {
			case 0:
				name = content;
				break;
			case 1:
				author = content;
				break;
			case 2:
				isbn = content;
				break;
			case 3:
				publisher = content;
				break;
			}
			Log.i(Utils.getLineInfo(), "isbn:" + isbn + " name:" + name
					+ " author:" + author + " publisher:" + publisher);
			addHistory(content);
			historyAdapter.updateAdapter();
			localUser.bookSearch(isbn, name, author, publisher, progress);
		}
	}

	/**
	 * 搜索处理
	 */
	private class SearchProcess extends HttpProcessBase {
		@Override
		public void statusError(String response) {
			Toast.makeText(BookSearchActivity.this, response, Toast.LENGTH_LONG)
					.show();
			// showDialog("[{\"id\":\"1\",\"name\":\"design\",\"publisher\":\"jp\",\"description\":\"empty\",\"author\":\"fans\",\"isbn\":\"9787302251200\",\"owner\":\"amy\",\"holder\":\"cc\",\"status\":\"0\",\"small_img\":\"url\",\"medium_img\":\"url\",\"large_img\":\"url\"}]");
		}

		@Override
		public void statusSuccess(String response) {
			showDialog(response);
		}

	}

	private void showDialog(String response) {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		builder = new AlertDialog.Builder(this);
		builder.setView(getListView(response));
		alertDialog = builder.create();
		alertDialog.show();
	}

	/**
	 * 获取弹窗显示的图书列表
	 * 
	 * @param response
	 * @return
	 */
	private ListView getListView(String response) {
		JSONArray array;
		try {
			array = new JSONArray(response);
		} catch (JSONException e) {
			array = new JSONArray();
			e.printStackTrace();
		}
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		datas = Book.jsonArrayToBooks(array);

		// 预先加载部分图片
		for (int i = 0; i < datas.size() && i < User.PERTIME_LOAD_NUMBER; i++) {
			localUser.loadBookImg(datas.get(i));
		}

		View view = LayoutInflater.from(this).inflate(
				R.layout.activity_submain_inform, null);
		ListView myListView = (ListView) view.findViewById(R.id.informlistview);
		ListAdapter adapter = new BookListAdapter(this, datas,
				localUser.getBookImgs());
		myListView.setAdapter(adapter);
		myListView.setOnItemClickListener(new SearchItemClick());
		return myListView;
	}

	/**
	 * 搜索结果列表item点击响应
	 * 
	 */
	private class SearchItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Map<String, Object> item = (Map<String, Object>) parent
					.getItemAtPosition(position);
			Book book = new Book(getApplication());
			book.getBookByIsbn((String) item.get(Book.ISBN),
					new BookInfoProcess(item));
		}
	}

	/**
	 * 搜书过程处理
	 * 
	 */
	private class BookInfoProcess extends HttpProcessBase {
		private Map<String, Object> book;

		public BookInfoProcess(Map<String, Object> book) {
			this.book = book;
		}

		public void before() {
			showProgressBar();
		}

		public void error(String content) {
			hideProgressBar();
		}

		@Override
		public void statusError(String response) {
			hideProgressBar();
			showToast(response);
		}

		@Override
		public void statusSuccess(String response) {
			hideProgressBar();

			Intent intent = new Intent();
			Bundle data = new Bundle();
			JSONObject obj = Book.bookToObj(book);
			data.putString("person_book", obj.toString());
			data.putString(NetAccess.RESPONSE, response);
			intent.putExtras(data);
			intent.setClass(BookSearchActivity.this,
					BookInformationActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * 历史记录Adapter
	 * 
	 */
	public class StrListAdapter extends PageListAdapter {
		List<String> datas;
		Context context;

		public StrListAdapter(Context context, List<String> data) {
			this.context = context;
			datas = data;
			initViewItemSize();
		}

		public void setDatas(List<String> datas) {
			this.datas = datas;
		}

		public List<String> getDatas() {
			return this.datas;
		}

		@Override
		public void loadData() {
		}

		@Override
		public int getCount() {
			if (curViewSize > datas.size())
				curViewSize = datas.size();
			return curViewSize;
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
			convertView = LayoutInflater.from(context).inflate(
					R.layout.simple_listview_item, null);

			TextView view = (TextView) convertView
					.findViewById(R.id.simple_listviewitem_content);
			view.setText(datas.get(position));

			return convertView;
		}
	}

	public void hideProgressBar() {
		findViewById(R.id.book_search_progressbar)
				.setVisibility(View.INVISIBLE);
	}

	public void showProgressBar() {
		findViewById(R.id.book_search_progressbar).setVisibility(View.VISIBLE);
	}

	public void showToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_LONG).show();
	}

	/**
	 * 搜索列表Adapter
	 * 
	 */
	private class BookListAdapter extends PageListAdapter {
		private Context context;
		private List<Map<String, Object>> datas;
		private Map<String, Bitmap> bookImgMap;

		public BookListAdapter(Context context, List<Map<String, Object>> data,
				Map<String, Bitmap> bookImgMap) {
			this.datas = data;
			this.context = context;
			this.bookImgMap = bookImgMap;
			initViewItemSize();
		}

		@Override
		public int getCount() {
			int allViewSize = datas.size();
			if (curViewSize > allViewSize)
				curViewSize = allViewSize;
			return curViewSize;
		}

		@Override
		public void loadData() {
			for (int i = curViewSize + 1; i < datas.size()
					&& i < curViewSize + User.PERTIME_LOAD_NUMBER; i++) {
				localUser.loadBookImg(datas.get(i));
			}
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int viewPosition, View convertView, ViewGroup parent) {
			convertView = getDataView(viewPosition);
			return convertView;
		}

		private View getDataView(int dataPosition) {
			TextView titleView;
			TextView statusView;
			ImageView coverView;
			View convertView = LayoutInflater.from(context).inflate(
					R.layout.mybooks_listview_item, null);
			coverView = (ImageView) convertView
					.findViewById(R.id.mybookslistitem_bookimage);
			titleView = (TextView) convertView
					.findViewById(R.id.mybookslistitem_bookname);
			statusView = (TextView) convertView
					.findViewById(R.id.mybookslistitem_bookstate);

			Map<String, Object> item = datas.get(dataPosition);

			if (bookImgMap.containsKey(item.get(Book.ISBN)))
				coverView.setImageBitmap(bookImgMap.get(item.get(Book.ISBN)));
			else
				coverView.setImageResource(R.drawable.default_book_big);
			titleView.setText((String) item.get(Book.NAME));
			statusView.setText(getText(item)); // 状态显示

			return convertView;
		}

		private String getText(Map<String, Object> item) {
			String text = "";
			if (localUser.getUsername().equals(item.get(Book.OWNER))) {
				switch ((Integer) item.get(Book.STATUS)) {
				case Book.STATUS_BUY | Book.STATUS_BORROW:
					text += "可卖/可借";
					break;
				case Book.STATUS_BUY | Book.STATUS_UNBORROW:
					text += "可卖/不可借";
					break;
				case Book.STATUS_UNBUY | Book.STATUS_BORROW:
					text += "不可卖/可借";
					break;
				case Book.STATUS_UNBUY | Book.STATUS_UNBORROW:
					text += "已借出";
					break;
				}
			} else {
				text = "非本人";
			}

			return text;
		}
	}
}
