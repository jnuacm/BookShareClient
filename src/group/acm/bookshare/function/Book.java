package group.acm.bookshare.function;

import group.acm.bookshare.R;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.StreamProcess;
import group.acm.bookshare.function.http.NetProgress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;

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
	public static final String ISBN = "isbn";
	public static final String OWNER = "owner";
	public static final String HOLDER = "holder";
	public static final String STATUS = "status";
	public static final String IMG_URL_SMALL = "s_img";
	public static final String IMG_URL_MIDDLE = "m_img";
	public static final String IMG_URL_LARGE = "l_img";
	
	public static final String DEFAULT_IMG_URL = "http://www.ttoou.com/qqtouxiang/allimg/110619/1-110619113537.jpg";
	

	// protected List<Comment>comments;
	protected List<String> approval;
	protected List<String> lables;

	protected Application application;

	public Book() {

	}

	public Book(Application application) {
		this.application = application;
	}

	public void getBookByIsbn(String isbn, NetProgress progress) {
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.douban_url);
		url += isbn;
		url += application.getString(R.string.douban_form);
		network.createUrlConntectionGetThread(url, new BookProgress(progress),
				new BookInfoProcess());
	}

	private class BookInfoProcess implements StreamProcess {
		@Override
		public String getResponse(int status, InputStream responseStream) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					responseStream));
			String ret = "";
			String tmp;
			try {
				while ((tmp = br.readLine()) != null) {
					ret += tmp;
				}
			} catch (IOException e) {
				ret = e.toString();
			}
			return ret;
		}
	}

	private class BookProgress extends HttpProcessBase {
		private NetProgress progress;

		public BookProgress(NetProgress progress) {
			this.progress = progress;
		}

		public void error(String content) {
			progress.setError(content);
		}

		@Override
		public void statusError(String response) {
			progress.setError("∂π∞Í∑√Œ ¥ÌŒÛ");
		}

		@Override
		public void statusSuccess(String response) {
			progress.setAfter(NetAccess.STATUS_SUCCESS, response);
		}
	}

	public static Map<String, Object> doubanStrToBook(String response) {
		Map<String, Object> ret = new HashMap<String, Object>();
		String name = "empty";
		String authors = "";
		String description = "empty";
		String publisher = "empty";
		String url = DEFAULT_IMG_URL;

		JSONObject bookObj = new JSONObject();
		try {
			bookObj = new JSONObject(response);
			name = bookObj.getJSONObject("title").getString("$t");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			JSONArray array = bookObj.getJSONArray("author");
			for (int i = 0; i < array.length(); i++) {
				authors += (array.getJSONObject(i).getJSONObject("name")
						.getString("$t") + " ");
			}
		} catch (JSONException e) {
			authors = "empty";
		}

		try {
			description = bookObj.getJSONObject("summary").getString("$t");
		} catch (JSONException e) {
			description = "empty";
		}
		try {
			JSONArray attrsArray = bookObj.getJSONArray("db:attribute");

			for (int i = 0; i < attrsArray.length(); i++) {
				JSONObject obj = attrsArray.getJSONObject(i);
				if ("publisher".equals(obj.getString("@name"))) {
					publisher = obj.getString("$t");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			JSONArray attrsArray = bookObj.getJSONArray("link");
			for (int i = 0; i < attrsArray.length(); i++) {
				JSONObject obj = attrsArray.getJSONObject(i);
				if ("image".equals(obj.getString("@rel"))) {
					url = obj.getString("@href");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String mUrl = url;
		String lUrl = url;

		ret.put(Book.AUTHOR, authors);
		ret.put(Book.DESCRIPTION, description);
		ret.put(Book.NAME, name);
		ret.put(Book.PUBLISHER, publisher);
		ret.put(Book.IMG_URL_SMALL, url);
		ret.put(Book.IMG_URL_MIDDLE, mUrl);
		ret.put(Book.IMG_URL_LARGE, lUrl);

		return ret;
	}

	public static Map<String, Object> objToBook(JSONObject item) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map.put(Book.ISBN, item.getString(Book.ISBN));
			map.put(Book.NAME, item.getString(Book.NAME));
			map.put(Book.IMG_URL_SMALL, DEFAULT_IMG_URL);
			// map.put(Book.IMG_URL_SMALL, item.getString(Book.IMG_URL_SMALL));
			map.put(Book.AUTHOR, item.getString(Book.AUTHOR));
			map.put(Book.PUBLISHER, item.getString(Book.PUBLISHER));

			map.put(Book.ID, item.getInt(Book.ID));
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

	public static Map<String, Object> bookToDetail(Map<String, Object> book,
			Map<String, Object> doubanBook) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put(Book.ID, book.get(Book.ID));
		ret.put(Book.ISBN, book.get(Book.ISBN));
		ret.put(Book.NAME, book.get(Book.NAME));
		ret.put(Book.IMG_URL_SMALL, book.get(Book.IMG_URL_SMALL));
		ret.put(Book.IMG_URL_MIDDLE, doubanBook.get(Book.IMG_URL_MIDDLE));
		ret.put(Book.IMG_URL_LARGE, doubanBook.get(Book.IMG_URL_LARGE));
		ret.put(Book.DESCRIPTION, doubanBook.get(Book.DESCRIPTION));
		ret.put(Book.AUTHOR, doubanBook.get(Book.AUTHOR));
		return ret;
	}
}
