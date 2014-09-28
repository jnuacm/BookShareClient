package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.NetProgress;
import group.acm.bookshare.function.User;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
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
	ListView mybookslistview;
	BookListAdapter bookAdapter;
	MainActivity activity;
	User localUser;

	public BookListManage(MainActivity activity) {
		this.activity = activity;
		localUser = ((LocalApp) activity.getApplication()).getUser();
	}

	public View getView() {
		return mybookslistview;
	}

	public NetProgress getBookChangeProgress() {
		return new BookChangeProgress();
	}

	private class BookChangeProgress extends HttpProcessBase {

		public void error(String content) {
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "失败:" + response;
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			reload(response);
			String content = "成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("InflateParams")
	public void initBookList() {
		bookAdapter = new BookListAdapter(activity, localUser.getBookListData());

		View view = LayoutInflater.from(activity).inflate(
				R.layout.activity_submain_book, null);
		mybookslistview = (ListView) view.findViewById(R.id.mybookslistview);

		setItemListener();
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
			titleView.setText((String) item.get(Book.NAME));
			statusView.setText(getText(item)); // 状态显示
			setDivider(position, convertView, item); // 设置是否显示分界

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

		private void setDivider(int position, View convertView,
				Map<String, Object> item) {
			TextView dividerView = (TextView) convertView
					.findViewById(R.id.mybookslistviewitem_divider);
			if (0 == position) {
				dividerView.setVisibility(View.VISIBLE);
				if (localUser.getUsername().equals(item.get(Book.OWNER)))
					dividerView.setText("Personal Books");
				else
					dividerView.setText("Borrowed Books");
			} else if (position > 0
					&& !localUser.getUsername().equals(item.get(Book.OWNER))
					&& localUser.getUsername().equals(
							datas.get(position - 1).get(Book.OWNER))) {
				dividerView.setVisibility(View.VISIBLE);
				dividerView.setText("Borrowed Books");
			} else {
				dividerView.setVisibility(View.GONE);
			}
		}
	}

	private void setItemListener() {
		mybookslistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<Map<String, Object>> bookList = localUser
						.getBookListData();
				Intent intent = new Intent(activity,
						BookInformationActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("bookName", (String) bookList.get(position)
						.get(Book.NAME));
				bundle.putString("bookIsbn", (String) bookList.get(position)
						.get(Book.ISBN));
				bundle.putString("bookDescription",
						(String) bookList.get(position).get(Book.DESCRIPTION));
				bundle.putInt("bookImage", R.drawable.default_book_big);
				intent.putExtra("key", bundle);
				activity.startActivity(intent);
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
			Map<String, Object> book = books.get(position);
			String text;
			OnClickListener listener;
			if (localUser.getUsername().equals(book.get(Book.OWNER))) {
				if (((String) book.get(Book.OWNER)).equals(book
						.get(Book.HOLDER))) {
					text = "删书";
					listener = new DeleteBookListener(book);
				} else {
					text = "请求对方还书";
					listener = new AskReturnListener(book);
				}
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

	private class AskReturnListener implements DialogInterface.OnClickListener {
		private Map<String, Object> book;

		public AskReturnListener(Map<String, Object> book) {
			this.book = book;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.askReturn(book, new ReturnBookProgress());
		}
	}

	private class ReturnBookListener implements DialogInterface.OnClickListener {
		private Map<String, Object> book;

		public ReturnBookListener(Map<String, Object> book) {
			this.book = book;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.returnBook(book, new ReturnBookProgress());
		}
	}

	private class DeleteBookListener implements DialogInterface.OnClickListener {
		private Map<String, Object> book;

		public DeleteBookListener(Map<String, Object> book) {
			this.book = book;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.deleteBook(book, new BookChangeProgress());
		}
	}

	private class ReturnBookProgress extends HttpProcessBase {
		public void error(String content) {
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "发送成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			String content = "发送成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}
	}

	public void reload() {
		localUser.getBookList(getBookChangeProgress());
	}

	public void reload(String response) {
		localUser.clearBookData();
		localUser.addBookDataToList(response);
		updateDisplay();
	}

	public void updateDisplay() {
		bookAdapter.notifyDataSetChanged();
	}
}