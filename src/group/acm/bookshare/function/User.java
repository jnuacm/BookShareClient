package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
public class User {
	private String username;
	private String password;

	private List<Map<String, Object>> books;
	private List<OwnerBook> ownBooks;
	private List<OwnerBook> borrowedBooks;

	private List<Friend> friends;
	private List<Friend> groups;

	private List<Map<String, Object>> informs;

	private int is_group;

	private Application application;
	private Handler handler;

	public User(Application application) {
		informs = new ArrayList<Map<String, Object>>();
		this.application = application;
	}

	public void setUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUserName() {
		return username;
	}

	public void setBooks(List<Map<String, Object>> books) {
		this.books = books;
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

	public void setIs_Group(int is_group) {
		this.is_group = is_group;
	}

	public int getIs_Group() {
		return this.is_group;
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

	public void addBook(String isbn, Handler handler) {
		this.handler = handler;
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
						User.this.handler.sendMessage(tmsg);
					} else {
						Message tmsg = Message.obtain();
						tmsg.what = NetAccess.NETMSG_PROCESS;
						Bundle data = new Bundle();
						data.putInt("time", 50);
						tmsg.setData(data);
						User.this.handler.sendMessage(tmsg);
						addToDB(repData);
					}
					break;
				}
			}
		});
	}

	public void addToDB(Bundle data) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("name", data.getString("name")));
		nvps.add(new BasicNameValuePair("isbn", data.getString("isbn")));
		nvps.add(new BasicNameValuePair("author", data.getString("authors")));
		nvps.add(new BasicNameValuePair("description", data
				.getString("description")));
		nvps.add(new BasicNameValuePair("publisher", data
				.getString("publisher")));
		nvps.add(new BasicNameValuePair("status", Integer
				.toString(Book.STATUS_BORROW | Book.STATUS_UNBUY)));

		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_add_book);
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_AFTER:
					msg = Message.obtain(msg);
					User.this.handler.sendMessage(msg);
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
		url += Integer.toString(((Integer) book.get("id")));
		net.createDeleteThread(url, handler);
		return true;
	}

	public void getBookList(String name, Handler handler) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_get_book);
		url += name;
		url += application.getResources().getString(R.string.action_book);
		net.createGetThread(url, handler);
	}

	public List<Map<String, Object>> getInitInformData() {
		return informs;
	}

	public List<Map<String, Object>> getCurInformData() {
		return informs;
	}

	public boolean addSendDataToList(String response) {

		JSONArray jsonarray;
		try {
			jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject item = jsonarray.getJSONObject(i);
				Map<String, Object> tmp = Inform.objToSend(item);
				if ((Integer) tmp.get("status") == Inform.REQUEST_STATUS_CONFIRM)
					continue;
				informs.add(tmp);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addReceiveDataToList(String response) {
		try {
			JSONArray jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject item = jsonarray.getJSONObject(i);
				Map<String, Object> tmp = Inform.objToReceive(item);
				if ((Integer) tmp.get("status") == Inform.REQUEST_STATUS_CONFIRM)
					continue;
				informs.add(tmp);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void getSendInformList(Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_send_inform);
		url += username;

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, handler);
	}

	public void getReceiveInformList(Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources()
				.getString(R.string.url_receive_inform);
		url += username;

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, handler);
	}

	public void updateRequest(int id, int status, Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_inform_update);
		url += Integer.toString(id);
		NetAccess net = NetAccess.getInstance();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("status", Integer.toString(status)));

		net.createPutThread(url, nvps, handler);
	}

	public void setFriend(List<Map<String, Object>> friend) {
		this.friends = new ArrayList<Friend>();
		for (Map<String, Object> item : friend)
			this.friends.add(new Friend(item));
	}

	public void setGroup(List<Map<String, Object>> group) {
		this.groups = new ArrayList<Friend>();
		for (Map<String, Object> item : group)
			this.groups.add(new Friend(item));
	}

	public List<Friend> getGroup() {
		return this.groups;
	}
	
	public void updateFriendship(Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_friendship_inform);
		
		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, handler);
	}
	
	public boolean deleteFriend(Map<String, Object> friend, Handler handler) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_friend);
		url += (String) friend.get("username");
		Log.i("delete user name is ",url);
		net.createDeleteThread(url, handler);
		return true;
	}

	public String getInformString(Map<String, Object> item) {
		String ret = (String) item.get("time");
		ret += ("\nfrom:" + item.get("from"));
		ret += ("\nto:" + item.get("to"));
		ret += "\n请求:";
		switch ((Integer) item.get("type")) {
		case Inform.REQUEST_TYPE_BORROW:
			ret += "借书";
			break;
		case Inform.REQUEST_TYPE_RETURN:
			ret += "还书";
			break;
		}

		ret += "\n";

		switch ((Integer) item.get("status")) {
		case Inform.REQUEST_STATUS_UNPROCESSED:
			ret += "未处理";
			break;
		case Inform.REQUEST_STATUS_PERMITTED:
			ret += "已允许";
			break;
		case Inform.REQUEST_STATUS_REFUSED:
			ret += "已拒绝";
			break;
		}
		return ret;
	}

	public void bookRequest(String aimName, int bookid, String message,
			int type, Handler handler) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("bookid", bookid);
			obj.put("message", message);
			String description = obj.toString();

			String url = application.getResources()
					.getString(R.string.url_host);
			url += application.getResources().getString(
					R.string.url_inform_create);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("type", Integer.toString(type)));
			nvps.add(new BasicNameValuePair("description", description));
			nvps.add(new BasicNameValuePair("to", aimName));

			NetAccess net = NetAccess.getInstance();
			net.createPostThread(url, nvps, handler);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
