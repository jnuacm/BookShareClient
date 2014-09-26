package group.acm.bookshare.function;

import group.acm.bookshare.R;
import group.acm.bookshare.util.Utils;

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
	private String userid;

	private List<Map<String, Object>> books;
	private List<Map<String, Object>> friends;
	private List<Map<String, Object>> groups;
	private List<Map<String, Object>> informs;

	private int is_group;

	private Application application;

	private ImageProcess image;

	public User(Application application) {
		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		groups = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		image = new ImageProcess(application);
		this.application = application;
	}

	public void setUser(String username, String password, String userid) {
		this.username = username;
		this.password = password;
		this.userid = userid;
	}

	public String getUserName() {
		return username;
	}

	public void login(NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("userid", userid));

		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_login);

		NetAccess network = NetAccess.getInstance();
		network.createPostThread(url, nvps, progress);
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
		Utils.setHasUpdate(application, false);
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
				Inform inform = new Inform(tmp, this, null);
				if (!inform.showThisInform())
					continue;
				informs.add(tmp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void addBook(String isbn, NetProgress progress) {
		Book book = new Book(this.application);
		book.getBookByIsbn(isbn, new DoubanBookProgress(progress));
	}

	private class DoubanBookProgress extends HttpProcessBase {
		public NetProgress progress;

		public DoubanBookProgress(NetProgress progress) {
			this.progress = progress;
		}

		@Override
		public void statusError(String response) {
			progress.setError(response);
		}

		@Override
		public void statusSuccess(String response) {
			
			try {
				addToDB(Book.doubanStrToBundle(response), progress);
				progress.setProcess(50);
			} catch (JSONException e) {
				progress.setError(e.toString());
			}
			
		}
	}

	private void addToDB(Bundle data, NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair(Book.NAME, data.getString(Book.NAME)));
		nvps.add(new BasicNameValuePair(Book.ISBN, data.getString(Book.ISBN)));
		nvps.add(new BasicNameValuePair(Book.AUTHOR, data
				.getString(Book.AUTHOR)));
		nvps.add(new BasicNameValuePair(Book.DESCRIPTION, data
				.getString(Book.DESCRIPTION)));
		nvps.add(new BasicNameValuePair(Book.PUBLISHER, data
				.getString(Book.PUBLISHER)));
		nvps.add(new BasicNameValuePair(Book.STATUS, Integer
				.toString(Book.STATUS_BORROW)));

		NetAccess network = NetAccess.getInstance();
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_add_book);
		network.createPostThread(url, nvps, new AddToDBProgress(progress));
	}

	private class AddToDBProgress extends HttpProcessBase {
		private NetProgress progress;

		public AddToDBProgress(NetProgress progress) {
			this.progress = progress;
		}

		@Override
		public void statusError(String response) {
			progress.setError(response);
		}

		@Override
		public void statusSuccess(String response) {
			progress.setAfter(NetAccess.STATUS_SUCCESS, response);
		}
	}

	public boolean deleteBook(Map<String, Object> book, NetProgress progress) {
		if (!((String) book.get(Book.OWNER)).equals(book.get(Book.HOLDER)))
			return false;
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_book);
		url += Integer.toString(((Integer) book.get(Book.ID)));
		net.createDeleteThread(url, progress);
		return true;
	}

	public void borrowBook(String aimName, Map<String, Object> book,
			NetProgress progress) {
		bookRequest(aimName, book, "借书消息", Inform.REQUEST_TYPE_BORROW, progress);
	}

	public void askReturn(Map<String, Object> book, NetProgress progress) {
		String holder = (String) book.get(Book.HOLDER);
		bookRequest(holder, book, "请快点还书", Inform.REQUEST_TYPE_RETURN, progress);
	}

	public void returnBook(Map<String, Object> book, NetProgress progress) {
		String owner = (String) book.get(Book.OWNER);
		bookRequest(owner, book, "还书啦", Inform.REQUEST_TYPE_RETURN, progress);
	}

	private void bookRequest(String aimName, Map<String, Object> book,
			String message, int type, NetProgress progress) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("message", message);
			obj.put("bookid", (Integer) book.get(Book.ID));
			obj.put(Book.NAME, (String) book.get(Book.NAME));
			Log.i("owner/holder", "owner:" + (String) book.get(Book.OWNER)
					+ " holder:" + (String) book.get(Book.HOLDER));
			obj.put(Book.HOLDER, (String) book.get(Book.HOLDER));
			obj.put(Book.OWNER, (String) book.get(Book.OWNER));
			String description = obj.toString();

			String url = application.getResources()
					.getString(R.string.url_host);
			url += application.getResources().getString(
					R.string.url_inform_create);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair(Inform.TYPE, Integer.toString(type)));
			nvps.add(new BasicNameValuePair(Inform.DESCRIPTION, description));
			nvps.add(new BasicNameValuePair(Inform.TO, aimName));

			NetAccess net = NetAccess.getInstance();
			net.createPostThread(url, nvps, progress);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getBookList(NetProgress progress) {
		getBookList(username, progress);
	}

	public void getBookList(String name, NetProgress progress) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_get_book);
		url += name;
		url += application.getResources().getString(R.string.action_book);
		net.createGetThread(url, progress);
	}

	public boolean informIgnoreJudge() {
		return false;
	}

	public void getSendInformList(NetProgress progress) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_send_inform);
		url += username;

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public void getReceiveInformList(NetProgress progress) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources()
				.getString(R.string.url_receive_inform);
		url += username;

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public void updateRequest(int id, int status, NetProgress progress) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_inform_update);
		url += Integer.toString(id);
		NetAccess net = NetAccess.getInstance();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Inform.STATUS, Integer.toString(status)));

		net.createPutThread(url, nvps, progress);
	}

	public void updateRead(Integer id, boolean readState, NetProgress progress) {
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

		net.createPutThread(url, nvps, progress);
	}

	public void updateFriendship(NetProgress progress) {
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(
				R.string.url_friendship_inform);

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public boolean deleteFriend(Map<String, Object> friend, NetProgress progress) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getResources().getString(R.string.url_delete_friend);
		url += (String) friend.get("username");
		Log.i("delete user name is ", url);
		net.createDeleteThread(url, progress);
		return true;
	}

	public String getInformString(Map<String, Object> item) {
		String ret = (String) item.get(Inform.TIME);
		ret += ("\nfrom:" + item.get(Inform.FROM));
		ret += ("\nto:" + item.get(Inform.TO));
		ret += "\n请求:";
		switch ((Integer) item.get(Inform.TYPE)) {
		case Inform.REQUEST_TYPE_BORROW:
			ret += "借书";
			break;
		case Inform.REQUEST_TYPE_RETURN:
			ret += "还书";
			break;
		}

		ret += "\n";

		switch ((Integer) item.get(Inform.STATUS)) {
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
			if ((Integer) item.get(Inform.ID) == id) {
				informs.remove(i);
				break;
			}
		}
		return true;
	}

	public Map<String, Object> getBookById(int id) {
		for (Map<String, Object> book : books) {
			if ((Integer) book.get(Book.ID) == id)
				return book;
		}
		return null;
	}

	public void createAvatar(String path, Handler handler) {
		image.createAvatar(username, path, handler);
	}
}
