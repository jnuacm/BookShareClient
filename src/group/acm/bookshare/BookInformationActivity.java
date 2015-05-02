package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.Comment;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.HttpProgress;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.NetThread;
import group.acm.bookshare.function.http.NetAccess.StreamProcess;
import group.acm.bookshare.util.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookInformationActivity extends Activity {
	private User localUser;

	private List<View> viewList; // 各个页面
	private ViewPager viewPager;// viewpager

	private Context appContext;
	private BookSubPage bookPage; // 显示书本信息页面
	private CommentSubPage commentPage; // 显示评论页面

	private Map<String, Object> detailBook; // 记录当前这本书的所有信息

	private Intent intent;

	private BroadcastReceiver receiver; // 评论更新推送接收

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		localUser = ((LocalApp) getApplication()).getUser();
		appContext = getApplicationContext();

		/* 以下是显示翻页部分 */
		viewList = new ArrayList<View>();
		bookPage = new BookSubPage();
		commentPage = new CommentSubPage();

		intent = getIntent();
		String response = intent.getStringExtra(NetAccess.RESPONSE);
		String bookObj = intent.getStringExtra("person_book");
		// 通过bookActionType判断是从何处跳转过来，判断当前按钮的执行操作
		int bookActionType = intent
				.getIntExtra("action_type", Utils.DO_NOTHING);

		bookPage.initBookPage(response, bookObj, bookActionType);
		viewList.add(bookPage.getView());
		commentPage.initCommentPage();
		viewList.add(commentPage.getView());

		viewPager = (ViewPager) findViewById(R.id.book_viewpager);
		viewPager.setAdapter(new MyViewPagerAdapter(viewList));
		viewPager.setCurrentItem(0);

		// 注册接收到推送时的更新receiver
		receiver = new MessageUpdateReceiver();
		registerUpdateReceiver();

		// 获取评论列表
		localUser.getCommentList((String) detailBook.get(Book.ISBN),
				new CommentGetProgress());
	}

	/**
	 * 评论获取过程的界面更新
	 */
	private class CommentGetProgress extends HttpProcessBase {

		@Override
		public void statusError(String response) {
			Toast.makeText(appContext, response, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			localUser.clearCommentData();
			localUser.addCommentDataToList(response);
			commentPage.updateDisplay();
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver); // 在onCreate注册，因此在onDestroy取消
		localUser.clearCommentData();
		super.onDestroy();
	}

	/**
	 * viewPager的适配器
	 */
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

	private class BookSubPage {
		private int bookActionType;

		private View page;

		private TextView bookNameView;
		private TextView bookDescriptionView;
		private TextView bookAuthorView;
		private TextView bookPublisherView;
		private TextView bookOwnerView;
		private TextView bookHolderView;
		private TextView bookStatusView;
		private ImageView backButton;
		private ImageView bookImageView;
		private Button actionButton;

		public void initBookPage(String response, String bookObj,
				int bookActionType) {
			this.bookActionType = bookActionType;

			page = LayoutInflater.from(BookInformationActivity.this).inflate(
					R.layout.activity_subbook_book, null);
			bookNameView = (TextView) page.findViewById(R.id.book_name);
			bookDescriptionView = (TextView) page
					.findViewById(R.id.book_description);
			bookAuthorView = (TextView) page.findViewById(R.id.book_author);
			bookPublisherView = (TextView) page
					.findViewById(R.id.book_publisher);
			bookOwnerView = (TextView) page.findViewById(R.id.book_owner);
			bookHolderView = (TextView) page.findViewById(R.id.book_holder);
			bookStatusView = (TextView) page.findViewById(R.id.book_status);
			backButton = (ImageView) page.findViewById(R.id.book_info_bar_img);
			bookImageView = (ImageView) page.findViewById(R.id.book_image);
			actionButton = (Button) page.findViewById(R.id.button_action);

			detailBook = getDetailBook(response, bookObj);

			bookNameView.setText((String) detailBook.get(Book.NAME));
			bookDescriptionView.setText("简介： "
					+ detailBook.get(Book.DESCRIPTION));
			bookAuthorView.setText("作者:" + detailBook.get(Book.AUTHOR));
			bookPublisherView.setText("出版社:" + detailBook.get(Book.PUBLISHER));
			bookOwnerView.setText("拥有人:" + detailBook.get(Book.OWNER));
			bookHolderView.setText("持有人:" + detailBook.get(Book.HOLDER));
			bookStatusView.setText(getStatusText());

			backButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			bookImageView.setOnClickListener(new BookImgClick());

			actionButton.setText(getActionButtonText());
			actionButton.setOnClickListener(new JudgeListener(detailBook));

			BookImgProcess tmp = new BookImgProcess();
			localUser.getUrlBookImg(
					(String) detailBook.get(Book.IMG_URL_MEDIUM), tmp, tmp);
		}

		public View getView() {
			return page;
		}

		// 获取当前状态的图书需要显示的状态文本
		private String getStatusText() {
			String text = "";
			switch ((Integer) detailBook.get(Book.STATUS)) {
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
			return text;
		}

		// 根据type获取按钮显示
		private String getActionButtonText() {
			String text = "";
			switch (bookActionType) {
			case Utils.DO_NOTHING:
				text = "null";
				break;
			case Utils.BOOK_RETURN:
				text = "还书";
				break;
			case Utils.BOOK_ASKRETURN:
				text = "请求对方还书";
				break;
			case Utils.BOOK_DELETE:
				text = "删书";
				break;
			case Utils.BOOK_BORROW:
				text = "借书";
				break;
			}
			return text;
		}

		// 获取map对象，包含该书的所有信息
		public Map<String, Object> getDetailBook(String response, String bookObj) {
			Map<String, Object> doubanBook = Book.doubanStrToBook(response);
			Map<String, Object> book = new HashMap<String, Object>();
			try {
				book = Book.objToBook(new JSONObject(bookObj));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return Book.bookToDetail(book, doubanBook);
		}

		// 获取书本图像的过程处理
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

		/**
		 * 根据该图书的状态判断相应的动作
		 */
		private class JudgeListener implements OnClickListener {
			private Map<String, Object> book;

			public JudgeListener(Map<String, Object> book) {
				this.book = book;
			}

			@Override
			public void onClick(View view) {
				String text = "";
				switch (bookActionType) {
				case Utils.BOOK_DELETE:
					text = "删书";
					break;
				case Utils.BOOK_ASKRETURN:
					text = "请求对方还书";
					break;
				case Utils.BOOK_RETURN:
					text = "还书";
					break;
				case Utils.BOOK_BORROW:
					text = "借书";
					break;
				default:
					return;
				}
				Builder builder = new AlertDialog.Builder(
						BookInformationActivity.this).setTitle("Confirm");
				builder = builder.setMessage(text).setPositiveButton("Yes",
						new ActionConfirmListener(book));
				builder = builder.setNegativeButton("No", null);
				builder.show();
			}
		}

		private NetThread borrowBookThread;
		// 按钮被点击后的动作通过type选择调用
		private class ActionConfirmListener implements
				DialogInterface.OnClickListener {
			private Map<String, Object> book;

			public ActionConfirmListener(Map<String, Object> book) {
				this.book = book;
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (bookActionType) {
				case Utils.BOOK_DELETE:
					localUser.deleteBook(book, new BookChangeProgress());
					break;
				case Utils.BOOK_RETURN:
					localUser.returnBook(book, HttpProgress.createShowProgress(
							BookInformationActivity.this, "发送成功", "发送失败"));
					break;
				case Utils.BOOK_ASKRETURN:
					localUser.askReturn(book, HttpProgress.createShowProgress(
							BookInformationActivity.this, "发送成功", "发送失败"));
					break;
				case Utils.BOOK_BORROW:
					if (borrowBookThread != null && !borrowBookThread.isCanceled())
						return;
					borrowBookThread = localUser.borrowBook(intent.getStringExtra("define"), book,
							HttpProgress.createShowProgress(
									BookInformationActivity.this, "发送成功",
									"发送失败"));
					break;
				}
			}
		}

		// 书本列表发生变化后的过程处理
		private class BookChangeProgress extends HttpProcessBase {

			public void error(String content) {
				Toast.makeText(BookInformationActivity.this, content,
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusError(String response) {
				String content = "失败:" + response;
				Toast.makeText(BookInformationActivity.this, content,
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusSuccess(String response) {
				String content = "成功";
				Toast.makeText(BookInformationActivity.this, content,
						Toast.LENGTH_LONG).show();

				Bundle data = new Bundle();
				data.putString(NetAccess.RESPONSE, response);
				Intent intent = new Intent();
				intent.putExtras(data);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	}

	private class CommentSubPage {
		private View page;
		private Button buttonComment;
		private ImageView buttonBack;
		private ListView listviewComment;

		private CommentListAdapter adapter;

		public void initCommentPage() {
			adapter = new CommentListAdapter(localUser.getCommentListData(),
					BookInformationActivity.this);
			page = LayoutInflater.from(BookInformationActivity.this).inflate(
					R.layout.activity_subbook_comment, null);

			// 返回键
			buttonBack = (ImageView) page.findViewById(R.id.comment_bar_img);
			buttonBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			// 评论列表
			listviewComment = (ListView) page
					.findViewById(R.id.comment_listview);
			listviewComment.setAdapter(adapter);
			listviewComment.setOnItemLongClickListener(new DeleteListener());

			// 添加评论按钮
			buttonComment = (Button) page.findViewById(R.id.comment_button);
			buttonComment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addComment();
				}
			});
		}

		public void updateDisplay() {
			adapter.reloadAdapter();
		}

		private class CommentListAdapter extends PageListAdapter {
			private List<Map<String, Object>> datas;
			private Context context;

			public CommentListAdapter(List<Map<String, Object>> datas,
					Context context) {
				this.datas = datas;
				this.context = context;
			}

			@Override
			public int getCount() {
				if (datas.size() < curViewSize)
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
				TextView person;
				TextView content;
				TextView date;
				convertView = LayoutInflater.from(context).inflate(
						R.layout.comment_listview_item, null);
				person = (TextView) convertView
						.findViewById(R.id.comment_item_person_textview);
				content = (TextView) convertView
						.findViewById(R.id.comment_item_content_textview);
				date = (TextView) convertView
						.findViewById(R.id.comment_item_date_textview);
				Map<String, Object> comment = datas.get(position);
				person.setText((String) comment.get(Comment.PERSON));
				content.setText((String) comment.get(Comment.CONTENT));
				date.setText((String) comment.get(Comment.DATE));
				return convertView;
			}

			@Override
			public void loadData() {
			}
		}

		public View getView() {
			return page;
		}

		// 弹窗添加评论
		public void addComment() {
			View addCommentView = LayoutInflater.from(
					BookInformationActivity.this).inflate(
					R.layout.add_friend_alert_dialog, null);
			EditText addCommentEdit = (EditText) addCommentView
					.findViewById(R.id.add_friend_name);
			AlertDialog addFriendDialog = null;
			AlertDialog.Builder builder = null;

			builder = new AlertDialog.Builder(BookInformationActivity.this);
			builder.setTitle("评论");
			builder.setMessage("请输入评论内容:");
			builder.setView(addCommentView);
			builder.setPositiveButton("Yes",
					new AddCommentConfirmDialogListener(addCommentEdit));
			builder.setNegativeButton("No", null);
			addFriendDialog = builder.create();
			addFriendDialog.show();
		}
	}

	// 确认响应接口
	private class AddCommentConfirmDialogListener implements
			DialogInterface.OnClickListener {
		EditText addCommentEdit;

		public AddCommentConfirmDialogListener(EditText addCommentEdit) {
			this.addCommentEdit = addCommentEdit;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			String content = addCommentEdit.getText().toString();
			if (content.length() <= 0)
				return;
			localUser.addComment((String) detailBook.get(Book.ISBN), content,
					HttpProgress.createShowProgress(
							BookInformationActivity.this, "发送成功", "发送失败"));
		}
	}

	// 长按item删除评论接口
	private class DeleteListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			AlertDialog deleteDialog = null;
			AlertDialog.Builder builder = null;

			builder = new AlertDialog.Builder(BookInformationActivity.this);
			builder.setTitle("删除评论");
			Map<String, Object> comment = (Map<String, Object>) parent
					.getItemAtPosition(position);
			builder.setMessage("确认删除评论?\n" + comment.get(Comment.PERSON));
			builder.setPositiveButton("Yes", new DeleteConfirmListener(comment));
			builder.setNegativeButton("No", null);
			deleteDialog = builder.create();
			deleteDialog.show();
			return false;
		}

		// 确认删除接口
		private class DeleteConfirmListener implements
				android.content.DialogInterface.OnClickListener {
			private Map<String, Object> comment;

			public DeleteConfirmListener(Map<String, Object> comment) {
				this.comment = comment;
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				localUser.deleteComment(comment, HttpProgress
						.createShowProgress(BookInformationActivity.this,
								"发送成功", "发送失败"));
			}

		}
	}

	// 注册推送的receiver
	private void registerUpdateReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("group.acm.bookshare.action.UPDATECOMMENT");
		registerReceiver(receiver, filter);
	}

	private class MessageUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					"group.acm.bookshare.action.UPDATECOMMENT")) {
				if (intent.getStringExtra(Book.ISBN).equals(
						detailBook.get(Book.ISBN))) {
					localUser.getCommentList(
							(String) detailBook.get(Book.ISBN),
							new CommentGetProgress());
				}
			}
		}
	}
}
