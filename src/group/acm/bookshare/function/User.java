package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class User {
	private String username;
	private String password;
	// private Time registTime;
	// private String area;
	// private String group;
	// private Library mylibrary;
	// private MsgManager msm;

	private List<OwnerBook> ownBooks;
	private List<OwnerBook> borrowedBooks;

	// private List<String> friends;

	private Application application;
	private Handler mainHandler;

	public User(Application application) {
		this.application = application;
	}

	public void setUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUserName() {
		return username;
	}

	public void setOwnBooks(List<Map<String, Object>> ownBooks) {

		this.ownBooks = new ArrayList<OwnerBook>();
		for (Map<String, Object> item : ownBooks)
			this.ownBooks.add(new OwnerBook(item));
	}

	public void setBorrowedBooks(List<Map<String, Object>> borrowedBooks) {
		this.borrowedBooks = new ArrayList<OwnerBook>();
		for (Map<String, Object> item : borrowedBooks)
			this.borrowedBooks.add(new OwnerBook(item));
	}

	public void login(Handler mainHandler) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_login);

		NetAccess network = NetAccess.getInstance();
		network.createPostThread(url, nvps, mainHandler);
	}

	public void addBook(String isbn, Handler mainHandler) {
		this.mainHandler = mainHandler;
		Log.i("User: addBook()", "success");
		Book book = new Book(this.application);
		book.getBookByIsbn(isbn, new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					Bundle repData = msg.getData();
					if (repData.getInt("status") == NetAccess.STATUS_ERROR) {
						Message tmsg = Message.obtain();
						tmsg.what = NetAccess.NETMSG_AFTER;
						Bundle data = new Bundle();
						data.putInt("status", NetAccess.STATUS_ERROR);
						data.putString("response",
								repData.getString("response"));
						tmsg.setData(data);
						User.this.mainHandler.sendMessage(tmsg);
					} else {
						Message tmsg = Message.obtain();
						tmsg.what = NetAccess.NETMSG_PROCESS;
						Bundle data = new Bundle();
						data.putInt("time", 50);
						tmsg.setData(data);
						User.this.mainHandler.sendMessage(tmsg);
						addToDB(repData);
					}
					break;
				}
			}
		});
	}

	public void addToDB(Bundle data) {
		// TODO Auto-generated method stub
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("name", data.getString("name")));
		nvps.add(new BasicNameValuePair("isbn", data.getString("isbn")));
		nvps.add(new BasicNameValuePair("author", data.getString("authors")));
		nvps.add(new BasicNameValuePair("description", data
				.getString("description")));
		nvps.add(new BasicNameValuePair("publisher", data
				.getString("publisher")));
		nvps.add(new BasicNameValuePair("status", "¿É½è"));

		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.path_api);
		url += application.getString(R.string.action_book);
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					msg = Message.obtain(msg);
					User.this.mainHandler.sendMessage(msg);
					break;
				}
			}
		};
		network.createPostThread(url, nvps, handler);
	}

	public boolean deleteBook(Map<String, Object> book, Handler handler) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_book);
		url += (String) book.get("id");
		net.createDeleteThread(url, handler);
		//net.createPostThread(url,new ArrayList<NameValuePair>(), handler);
		return true;
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
