package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//BookListManage负责对书本列表的界面、动作交互进行管理
public class BookListManage {
	public static final int SCANREQUEST_ADDBOOK = 1;
	public static final int SCANREQUEST_BOOKCONFIRM = 2;

	ListView mybookslistview;
	BookListAdapter bookAdapter;
	Activity activity;
	User localUser;

	public BookListManage(Activity activity) {
		this.activity = activity;
		localUser = ((LocalApp) activity.getApplication()).getUser();
	}

	public View getView() {
		return mybookslistview;
	}

	public Handler getAddBookHandler() {
		return new AddBookHandler();
	}

	@SuppressLint("HandlerLeak")
	private class AddBookHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				Bundle data = msg.getData();
				if (data.getInt("status") == NetAccess.STATUS_SUCCESS) {
					reload(data.getString("response"));
					String content = "添加成功";
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				} else {
					String content = "添加失败:" + data.getString("response");
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				}
				break;
			case NetAccess.NETMSG_ERROR:
				String content = msg.getData().getString("error");
				Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	@SuppressLint("InflateParams")
	public void initBookList() {
		bookAdapter = new BookListAdapter(activity, localUser.getBookListData());

		View view = LayoutInflater.from(activity).inflate(
				R.layout.activity_submain_book, null);
		mybookslistview = (ListView) view.findViewById(R.id.mybookslistview);

		setListener();

		mybookslistview.addHeaderView(LayoutInflater.from(this.activity)
				.inflate(R.layout.mybooks_listview_top, null));
		mybookslistview.setAdapter(bookAdapter);
	}

	private class BookListAdapter extends BaseAdapter {
		private Context context;
		private List<Map<String, Object>> datas;

		public BookListAdapter(Context context, List<Map<String, Object>> data) {
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

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView titleView;
			TextView statusView;
			ImageView coverView;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.mybooks_listview_item, null);
			}
			coverView = (ImageView) convertView
					.findViewById(R.id.mybookslistitem_bookimage);
			titleView = (TextView) convertView
					.findViewById(R.id.mybookslistitem_bookname);
			statusView = (TextView) convertView
					.findViewById(R.id.mybookslistitem_bookstate);

			Map<String, Object> item = datas.get(position);

			coverView.setImageResource(R.drawable.default_book_big);
			titleView.setText((String) item.get("bookname"));
			String text;
			if (localUser.getUserName().equals(item.get("owner")))
				text = "owner:";
			else
				text = "borrowed:";
			switch ((Integer) item.get("status")) {
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
				text += "不可卖/不可借";
				break;
			}
			statusView.setText(text);
			return convertView;
		}
	}

	private void setListener() {
		mybookslistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position) {

					Intent intent = new Intent(activity, CaptureActivity.class);
					intent.putExtra("model", "addBook");
					activity.startActivityForResult(intent, SCANREQUEST_ADDBOOK);
				} else {
					List<Map<String, Object>> bookList = localUser
							.getBookListData();
					Intent intent = new Intent(activity,
							BookInformationActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("bookName",
							(String) bookList.get(position - 1).get("bookname"));
					bundle.putString("bookIsbn",
							(String) bookList.get(position - 1).get("isbn"));
					bundle.putString(
							"bookDescription",
							(String) bookList.get(position - 1).get(
									"description"));
					bundle.putInt("bookImage", R.drawable.default_book_big);
					intent.putExtra("key", bundle);
					activity.startActivity(intent);
				}
			}
		});

		mybookslistview.setOnItemLongClickListener(new JudgeListener());
	}

	private class JudgeListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (0 == position)
				return false;

			Builder builder = new AlertDialog.Builder(activity)
					.setTitle("Confirm");
			List<Map<String, Object>> books = localUser.getBookListData();
			Map<String, Object> book = books.get(position - 1);
			String text;
			OnClickListener listener;
			if (localUser.getUserName().equals(book.get("owner"))) {
				text = "删书";
				listener = new DeleteBookListener(book);
			} else {
				text = "还书";
				listener = new ReturnBookListener(book);
			}
			builder = builder.setMessage(text).setPositiveButton("Yes",
					listener);
			builder = builder.setNegativeButton("No", null);
			builder.show();

			return true;
		}
	}

	private class ReturnBookListener implements DialogInterface.OnClickListener {
		private Map<String, Object> book;

		public ReturnBookListener(Map<String, Object> book) {
			this.book = book;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.returnBook(book, new ReturnBookHandler());
		}
	}

	private class DeleteBookListener implements DialogInterface.OnClickListener {
		private Map<String, Object> book;

		public DeleteBookListener(Map<String, Object> book) {
			this.book = book;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.deleteBook(book, new DeleteBookHandler());
		}
	}

	private class ReturnBookHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				Bundle data = msg.getData();
				if (data.getInt("status") == NetAccess.STATUS_SUCCESS) {
					String content = "发送成功";
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				} else {
					String content = "发送失败:" + data.getString("response");
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				}
				break;
			case NetAccess.NETMSG_ERROR:
				String content = msg.getData().getString("error");
				Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
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
					reload(msg.getData().getString("response"));
					bookAdapter.notifyDataSetChanged();
					String content = "删书成功";
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				} else {
					String content = "删书失败";
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				}
				break;
			case NetAccess.NETMSG_ERROR:
				String content = msg.getData().getString("error");
				Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	public void reload(String response) {
		localUser.clearBookData();
		localUser.addBookDataToList(response);
		bookAdapter.notifyDataSetChanged();
	}
}