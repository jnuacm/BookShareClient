package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.HttpProgress;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.StreamProcess;
import group.acm.bookshare.util.Utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BookInformationActivity extends Activity {
	private User localUser;

	private int bookActionType;

	private Map<String, Object> detailBook;

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

	private Context appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		localUser = ((LocalApp) getApplication()).getUser();

		Intent intent = getIntent();
		String response = intent.getStringExtra(NetAccess.RESPONSE);
		String bookObj = intent.getStringExtra("person_book");
		bookActionType = intent.getIntExtra("action_type", Utils.DO_NOTHING);

		bookNameView = (TextView) findViewById(R.id.book_name);
		bookDescriptionView = (TextView) findViewById(R.id.book_description);
		bookAuthorView = (TextView) findViewById(R.id.book_author);
		bookPublisherView = (TextView) findViewById(R.id.book_publisher);
		bookOwnerView = (TextView) findViewById(R.id.book_owner);
		bookHolderView = (TextView) findViewById(R.id.book_holder);
		bookStatusView = (TextView) findViewById(R.id.book_status);
		backButton = (ImageView) findViewById(R.id.book_info_bar_img);
		bookImageView = (ImageView) findViewById(R.id.book_image);
		actionButton = (Button) findViewById(R.id.button_action);
		appContext = getApplicationContext();

		detailBook = getDetailBook(response, bookObj);

		bookNameView.setText((String) detailBook.get(Book.NAME));
		bookDescriptionView.setText("��飺 " + detailBook.get(Book.DESCRIPTION));
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
		localUser.getUrlBookImg((String) detailBook.get(Book.IMG_URL_MEDIUM),
				tmp, tmp);
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
				break;
			}
		}
	}

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
