package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Book {
	protected String isbn;
	protected String name;
	protected String authors;
	protected String description;
	protected String publisher;
	protected String coverurl;
	// protected List<Comment>comments;
	protected List<String> approval;
	protected List<String> lables;

	protected Application application;
	protected Handler handler;

	public Book(Application application) {
		this.application = application;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public String getName() {
		return this.name;
	}

	public String getAuthors() {
		return this.authors;
	}

	public String getDescription() {
		return this.description;
	}

	public String getPublisher() {
		return this.publisher;
	}

	public String getCoverurl() {
		return this.coverurl;
	}

	public void getBookByIsbn(String isbn, Handler handler) {
		this.handler = handler;
		Log.i("Book : getbookbyisbn()", "success");
		this.isbn = isbn;
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.douban_url);
		url += isbn;
		url += application.getString(R.string.douban_form);
		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					strToBook(msg.getData());
					Bundle data = new Bundle();
					data.putString("isbn", Book.this.isbn);
					data.putString("authors", Book.this.authors);
					data.putString("description", Book.this.description);
					data.putString("name", Book.this.name);
					data.putString("publisher", Book.this.publisher);
					msg.what = NetAccess.NETMSG_AFTER;
					msg.setData(data);
					Book.this.handler.sendMessage(msg);
					break;
				}
			}
		});
		network.createDoubanThread(url, handlers);
	}

	public int addComment(String username) {
		return 0;
	}

	public int addApproval(String username) {
		return 0;
	}

	public int deleteComment(String username) {
		return 0;
	}

	public void strToBook(Bundle data) {
		// TODO Auto-generated method stub
		if (NetAccess.STATUS_SUCCESS != data.getInt("status")) {
			return;
		}
		this.name = "";
		this.authors = "";
		this.description = "";
		this.publisher = "";
		try {
			JSONObject bookObj = new JSONObject((String) data.get("response"));
			this.name = bookObj.getJSONObject("title").getString("$t");
			JSONArray array = bookObj.getJSONArray("author");
			for (int i = 0; i < array.length(); i++) {
				this.authors += (array.getJSONObject(i).getJSONObject("name")
						.getString("$t") + ",");
			}
			this.description = bookObj.getJSONObject("summary").getString("$t");
			this.publisher = bookObj.getJSONArray("db:attribute")
					.getJSONObject(5).getString("$t");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
