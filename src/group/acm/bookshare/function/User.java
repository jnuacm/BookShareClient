package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.HashMap;
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
	private List<Map<String, Object>> friends;
	private List<Map<String, Object>> groups;
	private List<Map<String, Object>> informs;

	private int is_group;

	private Application application;

	public User(Application application) {
		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		groups = new ArrayList<Map<String, Object>>();
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

	public void login(Handler mainHandler) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_login);

		NetAccess network = NetAccess.getInstance();
		network.createPostThread(url, nvps, mainHandler);
	}

	public void setBooks(List<Map<String, Object>> books) {
		this.books = books;
	}

	public void setFriend(List<Map<String, Object>> friend) {
		this.friends = friend;
	}

	public void setGroup(List<Map<String, Object>> group) {
		this.groups = group;
	}

	public void setIs_Group(int is_group) {
		this.is_group = is_group;
	}

	public int getIs_Group() {
		return this.is_group;
	}

	public List<Map<String, Object>> getBookListData() {
		return books;
	}

	public List<Map<String, Object>> getFriendListData() {
		return friends;
	}

	public List<Map<String, Object>> getGroupListData() {
		return groups;
	}

	public List<Map<String, Object>> getInformListData() {
		return informs;
	}

	public void clearBookData() {
		books.clear();
	}

	public void clearInformData() {
		informs.clear();
	}

	public void addBookDataToList(String response) {
		JSONObject jsonobj;
		try {
			jsonobj = new JSONObject(response);
			JSONArray jsonarray = jsonobj.getJSONArray("own_book");
			addBookDataToList(jsonarray);

			jsonarray = jsonobj.getJSONArray("borrowed_book");
			addBookDataToList(jsonarray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addBookDataToList(JSONArray jsonarray) {
		for (int i = 0; i < jsonarray.length(); i++) {
			JSONObject item;
			try {
				item = jsonarray.getJSONObject(i);
				books.add(Book.objToBook(item));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void addFriendDataToList(String response) {
		friends.clear();
		groups.clear();
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject jsonobj;
		try {
			jsonobj = new JSONObject(response);
			JSONArray jsonarray = jsonobj.getJSONArray("friend");

			for (int i = 0; i < jsonarray.length(); i++) {

				JSONObject item = jsonarray.getJSONObject(i);
				String name = item.getString("username");
				String email = item.getString("email");
				String area = item.getString("area");
				int is_group = item.getInt("is_group");
				map = new HashMap<String, Object>();
				map.put("username", name);
				map.put("email", email);
				map.put("area", area);
				map.put("image", R.drawable.friend1);
				map.put("is_group", is_group);
				// Log.i("is_group",name+" is "+is_group+"!!!");

				if (0 == is_group)// 朋友关系
					friends.add(map);
				else
					// 组属关系
					groups.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean addInformDataToList(String response) {
		JSONArray jsonarray;
		try {
			jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject item = jsonarray.getJSONObject(i);
				Map<String, Object> tmp = Inform.objToInform(item);
				if (!showThisInform(tmp))
					continue;
				informs.add(tmp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean showThisInform(Map<String, Object> item) {
		if ((Integer) item.get("read") == Inform.READ)
			return false;
		try {
			Inform inform = new Inform(item,this,null);
			switch(inform.state){
			case 5:
			case 7:
			case 8:
			case 9:
			case 15:
			case 16:
			case 18:
			case 19:
			case 23:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void addBook(String isbn, Handler handler) {
		Book book = new Book(this.application);
		book.getBookByIsbn(isbn, new DoubanBookHandler(handler));
	}

	private class DoubanBookHandler extends Handler {
		public Handler handler;

		public DoubanBookHandler(Handler handler) {
			this.handler = handler;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				Bundle repData = msg.getData();
				if (repData.getInt("status") == NetAccess.STATUS_ERROR) {
					Message tmsg = Message.obtain();
					tmsg.what = NetAccess.NETMSG_AFTER;
					Bundle data = new Bundle();
					data.putInt("status", NetAccess.STATUS_ERROR);
					data.putString("response", repData.getString("response"));
					tmsg.setData(data);
					handler.sendMessage(tmsg);
				} else {
					Message tmsg = Message.obtain();
					tmsg.what = NetAccess.NETMSG_PROCESS;
					Bundle data = new Bundle();
					data.putInt("time", 50);
					tmsg.setData(data);
					handler.sendMessage(tmsg);
					addToDB(repData, handler);
				}
				break;
			}
		}
	}

	private void addToDB(Bundle data, Handler handler) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("name", data.getString("name")));
		nvps.add(new BasicNameValuePair("isbn", data.getString("isbn")));
		nvps.add(new BasicNameValuePair("author", data.getString("authors")));
		nvps.add(new BasicNameValuePair("description", data
				.getString("description")));
		nvps.add(new BasicNameValuePair("publisher", data
				.getString("publisher")));
		nvps.add(new BasicNameValuePair("status", Integer
				.toString(Book.STATUS_BORROW)));

		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_add_book);
		network.createPostThread(url, nvps, new AddToDBHandler(handler));
	}

	private class AddToDBHandler extends Handler {
		private Handler handler;

		public AddToDBHandler(Handler handler) {
			this.handler = handler;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_AFTER:
				msg = Message.obtain(msg);
				handler.sendMessage(msg);
				break;
			}
		}
	}

	public boolean deleteBook(Map<String, Object> book, Handler handler) {
		if (!((String) book.get("owner")).equals(book.get("holder")))
			return false;
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_book);
		url += Integer.toString(((Integer) book.get("id")));
		net.createDeleteThread(url, handler);
		return true;
	}

	public void borrowBook(String aimName, Map<String, Object> book,
			Handler handler) {
		bookRequest(aimName, book, "借书消息", Inform.REQUEST_TYPE_BORROW, handler);
	}

	public void askReturn(Map<String, Object> book, Handler handler) {
		String holder = (String) book.get("holder");
		bookRequest(holder, book, "请快点还书", Inform.REQUEST_TYPE_RETURN, handler);
	}

	public void returnBook(Map<String, Object> book, Handler handler) {
		String owner = (String) book.get("owner");
		bookRequest(owner, book, "还书啦", Inform.REQUEST_TYPE_RETURN, handler);
	}

	private void bookRequest(String aimName, Map<String, Object> book,
			String message, int type, Handler handler) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("message", message);
			obj.put("bookid", (Integer) book.get("id"));
			obj.put("bookname", (String) book.get("name"));
			Log.i("owner/holder", "owner:" + (String) book.get("owner")
					+ " holder:" + (String) book.get("holder"));
			obj.put("holder", (String) book.get("holder"));
			obj.put("owner", (String) book.get("owner"));
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
			e.printStackTrace();
		}
	}

	public void getBookList(Handler handler) {
		getBookList(username, handler);
	}

	public void getBookList(String name, Handler handler) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_get_book);
		url += name;
		url += application.getResources().getString(R.string.action_book);
		net.createGetThread(url, handler);
	}

	public boolean informIgnoreJudge() {
		return false;
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

	public void updateRead(Integer id, boolean readState, Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_inform_update);
		url += Integer.toString(id);
		NetAccess net = NetAccess.getInstance();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		int read;
		if (readState)
			read = 1;
		else
			read = 0;
		nvps.add(new BasicNameValuePair("read", Integer.toString(read)));

		net.createPutThread(url, nvps, handler);
	}

	public void updateFriendship(Handler handler) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(
				R.string.url_friendship_inform);

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, handler);
	}

	public boolean deleteFriend(Map<String, Object> friend, Handler handler) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_friend);
		url += (String) friend.get("username");
		Log.i("delete user name is ", url);
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

	public boolean deleteInformById(int id) {
		for (int i = 0; i < informs.size(); i++) {
			Map<String, Object> item = informs.get(i);
			if ((Integer) item.get("id") == id) {
				informs.remove(i);
				break;
			}
		}
		return true;
	}

	public Map<String, Object> getBookById(int id) {
		for (Map<String, Object> book : books) {
			if ((Integer) book.get("id") == id)
				return book;
		}
		return null;
	}
}
