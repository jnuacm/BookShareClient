package group.acm.bookshare.function;

import group.acm.bookshare.R;
import group.acm.bookshare.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class User {
	private String username;
	private String password;
	private String area;
	private String email;
	private int is_group;
	private String userid;

	private List<Map<String, Object>> books;
	private List<Map<String, Object>> friends;
	private List<Map<String, Object>> informs;

	private Application application;
	
	private Bitmap avatar = null;

	public User(Application application) {
		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		this.application = application;
	}

	public User(Map<String, Object> userinfo, Application application) {
		setUsername((String) userinfo.get(Friend.NAME));
		setArea((String) userinfo.get(Friend.AREA));
		setEmail((String) userinfo.get(Friend.EMAIL));
		setIs_group((Integer) userinfo.get(Friend.IS_GROUP));

		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		this.application = application;
	}

	public void setUser(String username, String password, String userid) {
		this.setUsername(username);
		this.setPassword(password);
		this.setUserid(userid);
	}

	public void login(NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", getUsername()));
		nvps.add(new BasicNameValuePair("password", getPassword()));
		nvps.add(new BasicNameValuePair("userid", getUserid()));

		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_login);

		NetAccess network = NetAccess.getInstance();
		network.createPostThread(url, nvps, progress);
	}

	public void logout() {
		books.clear();
		friends.clear();
		informs.clear();
	}

	public List<Map<String, Object>> getBookListData() {
		return books;
	}

	public List<Map<String, Object>> getFriendListData() {
		return friends;
	}

	public List<Map<String, Object>> getInformListData() {
		return informs;
	}

	public void clearBookData() {
		books.clear();
	}

	public void clearFriendData() {
		friends.clear();
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
		try {
			JSONObject jsonobj = new JSONObject(response);
			JSONArray jsonarray = jsonobj.getJSONArray("friend");

			for (int i = 0; i < jsonarray.length(); i++) {
				friends.add(Friend.objToFriend(jsonarray.getJSONObject(i)));
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
		url += application.getString(R.string.url_book);
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
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_book);
		url += Integer.toString(((Integer) book.get(Book.ID)));
		net.createDeleteThread(url, progress);
		return true;
	}

	public void addFriend(String aimName, String message, NetProgress progress) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("message", message);
			String description = obj.toString();
			createRequest(aimName, Inform.REQUEST_TYPE_ADDFRIEND, description,
					progress);
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			createRequest(aimName, type, description, progress);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void createRequest(String aimName, int type, String description,
			NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_inform);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Inform.TYPE, Integer.toString(type)));
		nvps.add(new BasicNameValuePair(Inform.DESCRIPTION, description));
		nvps.add(new BasicNameValuePair(Inform.TO, aimName));

		NetAccess net = NetAccess.getInstance();
		net.createPostThread(url, nvps, progress);
	}

	public void getBookList(NetProgress progress) {
		getBookList(getUsername(), progress);
	}

	public void getBookList(String name, NetProgress progress) {
		NetAccess net = NetAccess.getInstance();
		String url = application.getResources().getString(R.string.url_host);
		url += application.getString(R.string.url_book);
		url += name;
		url += application.getString(R.string.url_book_all);
		net.createGetThread(url, progress);
	}

	public void getSendInformList(NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_inform);
		url += application.getString(R.string.url_inform_from);
		url += getUsername();

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public void getReceiveInformList(NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_inform);
		url += application.getString(R.string.url_inform_to);
		url += getUsername();

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public void updateRequest(int id, int status, NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_inform);
		url += Integer.toString(id);
		NetAccess net = NetAccess.getInstance();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Inform.STATUS, Integer.toString(status)));

		net.createPutThread(url, nvps, progress);
	}

	public void deleteRequest(int id, NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_inform);
		url += Integer.toString(id);
		NetAccess net = NetAccess.getInstance();
		net.createDeleteThread(url, progress);
	}

	public void getFriendList(NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_friend);

		NetAccess net = NetAccess.getInstance();
		net.createGetThread(url, progress);
	}

	public void deleteFriend(Map<String, Object> friend, NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_friend);
		url += (String) friend.get(Friend.NAME);

		NetAccess net = NetAccess.getInstance();
		net.createDeleteThread(url, progress);
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

	public Map<String, Object> getFriendByName(String name) {
		for (Map<String, Object> friend : friends) {
			if (name.equals(friend.get("username")))
				return friend;
		}
		return null;
	}

	public void createAvatar(String path, NetProgress progress) {
		FileBody fileBody = null;
		try {
			File file = new File(path);
			fileBody = new FileBody(file);
		} catch (Exception e) {
			Toast.makeText(application, e.toString(), Toast.LENGTH_LONG).show();
		}
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("file", fileBody);
		HttpEntity parts = builder.build();

		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_avatar);
		url += getUsername();

		NetAccess network = NetAccess.getInstance();
		network.createPostFileThread(url, parts, progress);
	}

	public void getAvatar(NetProgress progress) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_avatar);
		url += getUsername();

		NetAccess network = NetAccess.getInstance();
		network.createGetFileThread(url, progress, this);
	}
	
	public void setAvatarBitmap(Bitmap bitmap) {
		avatar = bitmap;
	}
	
	public Bitmap getAvatarBitmap() {
		return avatar;
	}

	public String getArea() {
		return area;
	}

	private void setArea(String area) {
		this.area = area;
	}

	public String getUsername() {
		return username;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	private void setEmail(String email) {
		this.email = email;
	}

	public int getIs_group() {
		return is_group;
	}

	private void setIs_group(int is_group) {
		this.is_group = is_group;
	}

	public String getUserid() {
		return userid;
	}

	private void setUserid(String userid) {
		this.userid = userid;
	}
}
