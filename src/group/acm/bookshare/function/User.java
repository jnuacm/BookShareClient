package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.bool;
import android.app.Application;
import android.text.format.Time;
import android.util.Log;

public class User {
	private String username;
	private String password;
	private Time registTime;
	private String area;
	private String group;
	// private Library mylibrary;
	// private MsgManager msm;

	private List<OwnerBook> ownBooks;
	private List<OwnerBook> borrowedBooks;

	private List<String> friends;

	private Application application;
	private Update update;

	public User(Application application) {
		this.application = application;
	}

	public void setUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private class LoginUpdate implements Update {

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

		}

		@Override
		public void error(String content) {
			// TODO Auto-generated method stub

		}

	}

	public void login(Update update) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);

		url += application.getString(R.string.url_login);
		List<Update> updates = new ArrayList<Update>();
		updates.add(update);
		updates.add(new LoginUpdate());
		network.getPostThread(url, nvps, updates).start();
	}

	private class GetBookUpdate implements Update {

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
			User.this.update.process(50);
			Book book = (Book)map.get("book");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Log.i("test book trans", book.getName());
			nvps.add(new BasicNameValuePair("name", book.getName()));
			nvps.add(new BasicNameValuePair("isbn", book.getIsbn()));
			nvps.add(new BasicNameValuePair("authors", book.getAuthors()));
			nvps.add(new BasicNameValuePair("description", book.getDescription()));
			nvps.add(new BasicNameValuePair("publisher", book.getPublisher()));
			nvps.add(new BasicNameValuePair("status", "¿É½è"));

			NetAccess network = NetAccess.getInstance();
			String url = application.getString(R.string.url_host);
			url += application.getString(R.string.path_api);
			url += application.getString(R.string.action_book);
			List<Update> updates = new ArrayList<Update>();
			updates.add(new AddBookUpdate());
			network.getPostThread(url, nvps, updates).start();
		}

		@Override
		public void error(String content) {
			// TODO Auto-generated method stub
			User.this.update.error(content);
		}
	}

	private class AddBookUpdate implements Update {

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

		}

		@Override
		public void error(String content) {
			// TODO Auto-generated method stub

		}

	}

	public void addBook(String isbn, Update update) {
		Log.i("User: addBook()", "success");
		this.update = update;
		Book book = new Book(this.application);
		book.getBookByIsbn(isbn, new GetBookUpdate());
	}

	/*
	 * public bool deleteBook(MyBook obj) { return null; }
	 */
	/*
	 * public int giveBack(MyBook obj) { return 0; }
	 */
	/*
	 * public int borrow(MyBook obj, String from) { return 0; }
	 */

	public int pushInfo() {
		return 0;
	}

	public int pullInfo() {
		return 0;
	}

	public bool updateState() {
		return null;
	}

	public int addFriend(String name) {
		return 0;
	}

	public int deleteFriend(String name) {
		return 0;
	}

	public int createGroup(String name) {
		return 0;
	}

	public int deleteGroup() {
		return 0;
	}
}
