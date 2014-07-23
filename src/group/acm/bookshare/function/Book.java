package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;

public class Book implements Update {
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
	protected Update update;

	public Book(Application application) {
		this.application = application;
	}

	public void getBookByIsbn(String isbn, Update update) {
		Log.i("Book : getbookbyisbn()", "success");
		this.isbn = isbn;
		this.update = update;
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.douban_url);
		Log.i("h", "before app");
		url += isbn;
		url += application.getString(R.string.douban_form);
		List<Update> updates = new ArrayList<Update>();
		updates.add(this);
		Log.i("url String:", url);
		// url = "http://api.douban.com/book/subject/isbn/" + this.isbn +
		// "?alt=json";
		network.getDoubanThread(url, updates).start();
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

	@Override
	public void before() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(Map<String, Object> map) {
		// TODO Auto-generated method stub
		if (NetAccess.STATUS_SUCCESS != (Integer) map.get("status")) {
			update.error("book get error.");
			return;
		}
		Map<String, Object> tmap = new HashMap<String, Object>();
		name = "";
		authors = "";
		description = "";
		publisher = "";
		try {
			JSONObject bookObj = new JSONObject((String) map.get("response"));
			name = bookObj.getJSONObject("title").getString("$t");
			JSONArray array = bookObj.getJSONArray("author");
			for (int i = 0; i < array.length(); i++) {
				authors += (array.getJSONObject(i).getJSONObject("name")
						.getString("$t") + ",");
			}
			description = bookObj.getJSONObject("summary").getString("$t");
			publisher = bookObj.getJSONArray("db:attribute").getJSONObject(5)
					.getString("$t");
			/*
			 * map.get("name"))); (String)map.get("isbn")));
			 * (String)map.get("authors"))); n (String)map.get("description")));
			 * nvps.add(new BasicNameValuePair("publisher",
			 * (String)map.get("publisher"))); nvps.add(new
			 * BasicNameValuePair("status", (String)map.get("status"))
			 */
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tmap.put("isbn", this.isbn);
		tmap.put("name", name);
		tmap.put("authors", authors);
		tmap.put("description", description);
		tmap.put("publisher", publisher);
		update.after(tmap);
	}

	@Override
	public void error(String content) {
		// TODO Auto-generated method stub
		update.error(content);
	}
}
