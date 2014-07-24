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

	public void getBookByIsbn(String isbn, Update update) {
		Log.i("Book : getbookbyisbn()", "success");
		this.isbn = isbn;
		this.update = update;
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.douban_url);
		url += isbn;
		url += application.getString(R.string.douban_form);
		List<Update> updates = new ArrayList<Update>();
		updates.add(this);
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
			this.update.error("book get error.");
			return;
		}
		Map<String, Object> tmap = new HashMap<String, Object>();
		this.name = "";
		this.authors = "";
		this.description = "";
		this.publisher = "";
		try {
			JSONObject bookObj = new JSONObject((String) map.get("response"));
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

		tmap.put("book", this);
		this.update.after(tmap);
	}

	@Override
	public void error(String content) {
		// TODO Auto-generated method stub
		this.update.error(content);
	}
}
