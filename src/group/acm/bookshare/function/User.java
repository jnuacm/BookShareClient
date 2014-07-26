package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.bool;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	private Handler mainHandler;
	private Handler addBookHandler;

	public User(Application application) {
		this.application = application;
	}

	public void setUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void login(Handler mainHandler) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try {
			nvps.add(new BasicNameValuePair("username", URLEncoder.encode(
					username, "UTF-8")));

			nvps.add(new BasicNameValuePair("password", URLEncoder.encode(
					password, "UTF-8")));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);

		url += application.getString(R.string.url_login);
		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(mainHandler);
		network.createPostThread(url, nvps, handlers);
	}

	public void addBook(String isbn, Handler mainHandler) {
		this.mainHandler = mainHandler;
		Log.i("User: addBook()", "success");
		Book book = new Book(this.application);
		this.addBookHandler = new Handler() {
			public void handlerMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					msg.what = NetAccess.NETMSG_PROCESS;
					Bundle data = new Bundle();
					data.putInt("time", 50);
					msg.setData(data);
					User.this.mainHandler.sendMessage(msg);
					addToDB(msg.getData());
					break;
				}
			}
		};
		book.getBookByIsbn(isbn, addBookHandler);
	}

	public void addToDB(Bundle data) {
		// TODO Auto-generated method stub
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Log.i("test book trans", data.getString("name"));
		nvps.add(new BasicNameValuePair("name", data.getString("name")));
		nvps.add(new BasicNameValuePair("isbn", data.getString("isbn")));
		nvps.add(new BasicNameValuePair("authors", data.getString("authors")));
		nvps.add(new BasicNameValuePair("description", data
				.getString("description")));
		nvps.add(new BasicNameValuePair("publisher", data
				.getString("publisher")));
		nvps.add(new BasicNameValuePair("status", "¿É½è"));
		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.path_api);
		url += application.getString(R.string.action_book);
		List<Handler> handlers = new ArrayList<Handler>();
		handlers.add(new Handler(){
			public void handleMessage(Message msg){
				switch (msg.what){
				case NetAccess.NETMSG_AFTER:
					User.this.mainHandler.sendMessage(msg);
					break;
				}
			}
		});
		network.createPostThread(url, nvps, handlers);
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
