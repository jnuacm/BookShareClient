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

	private List<View> viewList; // ����ҳ��
	private ViewPager viewPager;// viewpager

	private Context appContext;
	private BookSubPage bookPage; // ��ʾ�鱾��Ϣҳ��
	private CommentSubPage commentPage; // ��ʾ����ҳ��

	private Map<String, Object> detailBook; // ��¼��ǰ�Ȿ���������Ϣ

	private Intent intent;

	private BroadcastReceiver receiver; // ���۸������ͽ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		localUser = ((LocalApp) getApplication()).getUser();
		appContext = getApplicationContext();

		/* ��������ʾ��ҳ���� */
		viewList = new ArrayList<View>();
		bookPage = new BookSubPage();
		commentPage = new CommentSubPage();

		intent = getIntent();
		String response = intent.getStringExtra(NetAccess.RESPONSE);
		String bookObj = intent.getStringExtra("person_book");
		// ͨ��bookActionType�ж��ǴӺδ���ת�������жϵ�ǰ��ť��ִ�в���
		int bookActionType = intent
				.getIntExtra("action_type", Utils.DO_NOTHING);

		bookPage.initBookPage(response, bookObj, bookActionType);
		viewList.add(bookPage.getView());
		commentPage.initCommentPage();
		viewList.add(commentPage.getView());

		viewPager = (ViewPager) findViewById(R.id.book_viewpager);
		viewPager.setAdapter(new MyViewPagerAdapter(viewList));
		viewPager.setCurrentItem(0);

		// ע����յ�����ʱ�ĸ���receiver
		receiver = new MessageUpdateReceiver();
		registerUpdateReceiver();

		// ��ȡ�����б�
		localUser.getCommentList((String) detailBook.get(Book.ISBN),
				new CommentGetProgress());
	}

	/**
	 * ���ۻ�ȡ���̵Ľ������
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
		unregisterReceiver(receiver); // ��onCreateע�ᣬ�����onDestroyȡ��
		localUser.clearCommentData();
		super.onDestroy();
	}

	/**
	 * viewPager��������
	 */
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
			bookDescriptionView.setText("��飺 "
					+ detailBook.get(Book.DESCRIPTION));
			bookAuthorView.setText("����:" + detailBook.get(Book.AUTHOR));
			bookPublisherView.setText("������:" + detailBook.get(Book.PUBLISHER));
			bookOwnerView.setText("ӵ����:" + detailBook.get(Book.OWNER));
			bookHolderView.setText("������:" + detailBook.get(Book.HOLDER));
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

		// ��ȡ��ǰ״̬��ͼ����Ҫ��ʾ��״̬�ı�
		private String getStatusText() {
			String text = "";
			switch ((Integer) detailBook.get(Book.STATUS)) {
			case Book.STATUS_BUY | Book.STATUS_BORROW:
				text += "����/�ɽ�";
				break;
			case Book.STATUS_BUY | Book.STATUS_UNBORROW:
				text += "����/���ɽ�";
				break;
			case Book.STATUS_UNBUY | Book.STATUS_BORROW:
				text += "������/�ɽ�";
				break;
			case Book.STATUS_UNBUY | Book.STATUS_UNBORROW:
				text += "�ѽ��";
				break;
			}
			return text;
		}

		// ����type��ȡ��ť��ʾ
		private String getActionButtonText() {
			String text = "";
			switch (bookActionType) {
			case Utils.DO_NOTHING:
				text = "null";
				break;
			case Utils.BOOK_RETURN:
				text = "����";
				break;
			case Utils.BOOK_ASKRETURN:
				text = "����Է�����";
				break;
			case Utils.BOOK_DELETE:
				text = "ɾ��";
				break;
			case Utils.BOOK_BORROW:
				text = "����";
				break;
			}
			return text;
		}

		// ��ȡmap���󣬰��������������Ϣ
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

		// ��ȡ�鱾ͼ��Ĺ��̴���
		private class BookImgProcess extends HttpProcessBase implements
				StreamProcess {
			private Bitmap bm;

			@Override
			public void statusError(String response) {
				Toast.makeText(BookInformationActivity.this, "û���е�ͼ",
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
		 * ���ݸ�ͼ���״̬�ж���Ӧ�Ķ���
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
					text = "ɾ��";
					break;
				case Utils.BOOK_ASKRETURN:
					text = "����Է�����";
					break;
				case Utils.BOOK_RETURN:
					text = "����";
					break;
				case Utils.BOOK_BORROW:
					text = "����";
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
		// ��ť�������Ķ���ͨ��typeѡ�����
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
							BookInformationActivity.this, "���ͳɹ�", "����ʧ��"));
					break;
				case Utils.BOOK_ASKRETURN:
					localUser.askReturn(book, HttpProgress.createShowProgress(
							BookInformationActivity.this, "���ͳɹ�", "����ʧ��"));
					break;
				case Utils.BOOK_BORROW:
					if (borrowBookThread != null && !borrowBookThread.isCanceled())
						return;
					borrowBookThread = localUser.borrowBook(intent.getStringExtra("define"), book,
							HttpProgress.createShowProgress(
									BookInformationActivity.this, "���ͳɹ�",
									"����ʧ��"));
					break;
				}
			}
		}

		// �鱾�б����仯��Ĺ��̴���
		private class BookChangeProgress extends HttpProcessBase {

			public void error(String content) {
				Toast.makeText(BookInformationActivity.this, content,
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusError(String response) {
				String content = "ʧ��:" + response;
				Toast.makeText(BookInformationActivity.this, content,
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusSuccess(String response) {
				String content = "�ɹ�";
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

			// ���ؼ�
			buttonBack = (ImageView) page.findViewById(R.id.comment_bar_img);
			buttonBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

			// �����б�
			listviewComment = (ListView) page
					.findViewById(R.id.comment_listview);
			listviewComment.setAdapter(adapter);
			listviewComment.setOnItemLongClickListener(new DeleteListener());

			// ������۰�ť
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

		// �����������
		public void addComment() {
			View addCommentView = LayoutInflater.from(
					BookInformationActivity.this).inflate(
					R.layout.add_friend_alert_dialog, null);
			EditText addCommentEdit = (EditText) addCommentView
					.findViewById(R.id.add_friend_name);
			AlertDialog addFriendDialog = null;
			AlertDialog.Builder builder = null;

			builder = new AlertDialog.Builder(BookInformationActivity.this);
			builder.setTitle("����");
			builder.setMessage("��������������:");
			builder.setView(addCommentView);
			builder.setPositiveButton("Yes",
					new AddCommentConfirmDialogListener(addCommentEdit));
			builder.setNegativeButton("No", null);
			addFriendDialog = builder.create();
			addFriendDialog.show();
		}
	}

	// ȷ����Ӧ�ӿ�
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
							BookInformationActivity.this, "���ͳɹ�", "����ʧ��"));
		}
	}

	// ����itemɾ�����۽ӿ�
	private class DeleteListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			AlertDialog deleteDialog = null;
			AlertDialog.Builder builder = null;

			builder = new AlertDialog.Builder(BookInformationActivity.this);
			builder.setTitle("ɾ������");
			Map<String, Object> comment = (Map<String, Object>) parent
					.getItemAtPosition(position);
			builder.setMessage("ȷ��ɾ������?\n" + comment.get(Comment.PERSON));
			builder.setPositiveButton("Yes", new DeleteConfirmListener(comment));
			builder.setNegativeButton("No", null);
			deleteDialog = builder.create();
			deleteDialog.show();
			return false;
		}

		// ȷ��ɾ���ӿ�
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
								"���ͳɹ�", "����ʧ��"));
			}

		}
	}

	// ע�����͵�receiver
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
