package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendBooksActivity extends Activity {
	private String friendName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_books);

		List<Map<String, Object>> books = new ArrayList<Map<String, Object>>();
		friendName = getIntent().getStringExtra("friendname");
		String response = getIntent().getStringExtra("response");

		try {
			JSONObject obj = new JSONObject(response);
			Log.i("FriendBooksActivity:", obj.getString("own_book"));
			books = Book.responseToBooks(obj.getString("own_book"));
			if (books.isEmpty())
				Log.i("FriendBooksActivity", "Ϊ��");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FriendBooksAdapter bookAdapter = new FriendBooksAdapter(this, books);
		ListView bookslistview = (ListView) findViewById(R.id.friend_book_listview);
		bookslistview.setAdapter(bookAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friend_books, menu);
		return true;
	}

	private class FriendBooksAdapter extends BaseAdapter {
		List<Map<String, Object>> datas;
		Context context;

		public FriendBooksAdapter(Context context,
				List<Map<String, Object>> data) {
			this.context = context;
			datas = data;
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
			Button borrow;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.friendbooklistview_item, null);
			}

			title = (TextView) convertView
					.findViewById(R.id.booklistviewitem_title);
			status = (TextView) convertView
					.findViewById(R.id.booklistviewitem_status);
			borrow = (Button) convertView
					.findViewById(R.id.booklistviewitem_borrow);

			Map<String, Object> item = datas.get(position);

			title.setText((String) item.get("name"));
			String[] text = { "�ɽ�", "�ڽ�", "�ѽ�δȷ��", "�ѻ�δȷ��" };
			status.setText(text[(Integer) item.get("status")]);
			borrow.setOnClickListener(new ItemButtonClick(position));

			return convertView;
		}

		private class ItemButtonClick implements OnClickListener {
			private int position;

			public ItemButtonClick(int position) {
				this.position = position;
			}

			@Override
			public void onClick(View v) {
				((LocalApp) getApplication()).getUser().bookRequest(friendName,
						(Integer) datas.get(position).get("id"), "������Ϣ",
						Inform.REQUEST_TYPE_BORROW, new Handler() {
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case NetAccess.NETMSG_AFTER:
									if (msg.getData().getInt("status") == NetAccess.STATUS_SUCCESS) {
										Toast.makeText(
												FriendBooksActivity.this,
												"���ͳɹ�", Toast.LENGTH_SHORT)
												.show();
									} else {
										Toast.makeText(
												FriendBooksActivity.this,
												"����ʧ��", Toast.LENGTH_SHORT)
												.show();
									}
									break;
								case NetAccess.NETMSG_ERROR:
									Toast.makeText(FriendBooksActivity.this,
											"�������", Toast.LENGTH_SHORT).show();
									break;
								}
							}
						});
			}

		}

	}

}