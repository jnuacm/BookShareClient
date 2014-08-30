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

@SuppressLint("HandlerLeak")
public class Book {
	public static final int STATUS_BORROW = 1;
	public static final int STATUS_UNBORROW = 0;
	public static final int STATUS_BUY = 2;
	public static final int STATUS_UNBUY = 0;

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
							"status")) {
						Bundle data = doubanStrToBundle((String) msg.getData()
								.get("response"));
						data.putInt("status", NetAccess.STATUS_SUCCESS);
						data.putString("isbn", isbn);
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
			data.putInt("status", NetAccess.STATUS_ERROR);
			data.putString("response", error);
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

		ret.putString("authors", authors);
		ret.putString("description", description);
		ret.putString("name", name);
		ret.putString("publisher", publisher);

		return ret;
	}

	public static List<Map<String, Object>> jsonArrayToBooks(JSONArray array) {
		List<Map<String, Object>> books = new ArrayList<Map<String, Object>>();
		Map<String, Object> item = new HashMap<String, Object>();
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				item = new HashMap<String, Object>();
				item.put("id", obj.getInt("id"));
				item.put("isbn", obj.getString("isbn"));
				item.put("name", obj.getString("name"));
				item.put("authors", obj.getString("author"));
				item.put("publisher", obj.getString("publisher"));
				item.put("description", obj.getString("description"));
				item.put("holder", obj.getString("holder"));
				item.put("owner", obj.getString("owner"));
				item.put("status", obj.getInt("status"));
				books.add(item);
			}
			return books;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
