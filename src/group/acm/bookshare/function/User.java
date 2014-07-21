package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.bool;
import android.app.Application;
import android.text.format.Time;

public class User {
	private String username;
	private String password;
	private String nickname;
	private Time regist_time;

	// private Library mylibrary;
	// private MsgManager msm;

	private String area;
	private List<String> friends;
	private String group;

	private Application application;

	public User(Application application) {
		this.application = application;
	}

	public void setUser(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	private class LoginUpdate implements Update{

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
		network.getPostThread(url, nvps, updates).start();
	}

	/*
	 * public bool addBook(MyBook obj) { return null; }
	 */
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
