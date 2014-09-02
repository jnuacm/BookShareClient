package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class Book {
	public static final int STATUS_BORROW = 1;
	public static final int STATUS_UNBORROW = 0;
	public static final int STATUS_BUY = 2;
	public static final int STATUS_UNBUY = 0;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PUBLISHER = "publisher";
	public static final String DESCRIPTION = "description";
	public static final String AUTHOR = "author";
	public static final String COVERURL = "coverurl";
	public static final String ISBN = "isbn";
	public static final String OWNER = "owner";
	public static final String HOLDER = "holder";
	public static final String STATUS = "status";

	public static String DEFAULT_BOOK_IMAGE_URL = "http://pica.nipic.com/2008-05-03/200853124434763_2.jpg";

	// protected List<Comment>comments;
	protected List<String> approval;
	protected List<String> lables;

	protected Application application;

	public Book() {

	}

	public Book(Application application) {
		this.application = application;
	}

	public void getBookByIsbn(String isbn, Handler handler) {
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.douban_url);
		url += isbn;
		url += application.getString(R.string.douban_form);
		network.createDoubanThread(url, new BookHandler(isbn, handler));
	}

	private class BookHandler extends Handler {
		private Handler handler;
		private String isbn;

		public BookHandler(String isbn, Handler handler) {
			this.handler = handler;
			this.isbn = isbn;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				try {
					if (NetAccess.STATUS_SUCCESS == msg.getData().getInt(
							NetAccess.STATUS)) {
						Bundle data = doubanStrToBundle((String) msg.getData()
								.get(NetAccess.RESPONSE));
						data.putInt(NetAccess.STATUS, NetAccess.STATUS_SUCCESS);
						data.putString(Book.ISBN, isbn);
						msg = Message.obtain();
						msg.what = NetAccess.NETMSG_AFTER;
						msg.setData(data);
						handler.sendMessage(msg);
					} else {
						sendFailMessage("∂π∞Í∑√Œ ¥ÌŒÛ");
					}
				} catch (JSONException e) {
					sendFailMessage(e.toString());
					break;
				}
				break;
			}
		}

		private void sendFailMessage(String error) {
			Bundle data = new Bundle();
			data.putInt(NetAccess.STATUS, NetAccess.STATUS_ERROR);
			data.putString(NetAccess.RESPONSE, error);
			Message msg = Message.obtain();
			msg.what = NetAccess.NETMSG_AFTER;
			msg.setData(data);
			handler.sendMessage(msg);
		}
	}

	public Bundle doubanStrToBundle(String response) throws JSONException {
		Bundle ret = new Bundle();
		String name = "";
		String authors = "";
		String description = "";
		String publisher = "";

		JSONObject bookObj = new JSONObject(response);
		name = bookObj.getJSONObject("title").getString("$t");
		JSONArray array = bookObj.getJSONArray("author");
		for (int i = 0; i < array.length(); i++) {
			authors += (array.getJSONObject(i).getJSONObject("name")
					.getString("$t") + " ");
		}
		description = bookObj.getJSONObject("summary").getString("$t");
		publisher = bookObj.getJSONArray("db:attribute").getJSONObject(5)
				.getString("$t");

		ret.putString(Book.AUTHOR, authors);
		ret.putString(Book.DESCRIPTION, description);
		ret.putString(Book.NAME, name);
		ret.putString(Book.PUBLISHER, publisher);

		return ret;
	}

	public static Map<String, Object> objToBook(JSONObject item) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map.put(Book.ID, item.getInt(Book.ID));
			map.put(Book.ISBN, item.getString(Book.ISBN));
			map.put(Book.NAME, item.getString(Book.NAME));
			map.put(Book.COVERURL, R.drawable.default_book_big);
			map.put(Book.DESCRIPTION, item.getString(Book.DESCRIPTION));
			map.put(Book.AUTHOR, item.getString(Book.AUTHOR));
			map.put(Book.PUBLISHER, item.getString(Book.PUBLISHER));

			map.put(Book.OWNER, item.getString(Book.OWNER));
			map.put(Book.HOLDER, item.getString(Book.HOLDER));
			map.put(Book.STATUS, item.getInt(Book.STATUS));

			return map;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Map<String, Object>> jsonArrayToBooks(JSONArray array) {
		List<Map<String, Object>> books = new ArrayList<Map<String, Object>>();
		try {
			for (int i = 0; i < array.length(); i++) {
				books.add(objToBook(array.getJSONObject(i)));
			}
			return books;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
