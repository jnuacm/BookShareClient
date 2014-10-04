package group.acm.bookshare.function;

import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.function.http.ProgressNone;
import group.acm.bookshare.function.http.UrlStringFactory;
import group.acm.bookshare.function.http.NetAccess.EntityProcess;
import group.acm.bookshare.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	private int avatarVersion;
	private String userid;

	private List<Map<String, Object>> books; // ����user���鱾�б�����
	private List<Map<String, Object>> friends; // ����uesr�ĺ����б�����
	private List<Map<String, Object>> informs; // ����user����Ϣ�б�����

	private Application application;
	private ImageManage imgManage; // ͼ��������
	private UrlStringFactory urlFactory; // url������
	private NetAccess net; // ������ʶ���

	/**
	 * ����ı������û��Ĺ��캯��
	 */
	public User(Application application) {
		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		this.application = application;
		avatarVersion = 0;
		imgManage = new ImageManage(application);
		urlFactory = new UrlStringFactory(application);
		net = NetAccess.getInstance();
	}

	/**
	 * ���������û��Ĺ��캯��
	 */
	public User(Map<String, Object> userinfo, Application application) {
		setUsername((String) userinfo.get(Friend.NAME));
		setArea((String) userinfo.get(Friend.AREA));
		setEmail((String) userinfo.get(Friend.EMAIL));
		setIs_group((Integer) userinfo.get(Friend.IS_GROUP));
		setAvatarVersion((Integer) userinfo.get(Friend.AVATAR_VERSION));

		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		this.application = application;
		urlFactory = new UrlStringFactory(application);
		net = NetAccess.getInstance();
	}

	public void setUser(String username, String password, String userid) {
		this.setUsername(username);
		this.setPassword(password);
		this.setUserid(userid);
	}

	public void updateInfo(Map<String, Object> item) {
		setUsername((String) item.get(Friend.NAME));
		setEmail((String) item.get(Friend.EMAIL));
		setArea((String) item.get(Friend.AREA));
		setIs_group((Integer) item.get(Friend.IS_GROUP));
	}

	public void login(NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", getUsername()));
		nvps.add(new BasicNameValuePair("password", getPassword()));
		nvps.add(new BasicNameValuePair("userid", getUserid()));

		String url = urlFactory.getLoginUrl();

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		try {
			net.createPostThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8), progress);
		} catch (UnsupportedEncodingException e) {
			progress.setError(e.toString());
		}
	}

	public void register(String username, String password, String email,
			String area, NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("area", area));

		String url = urlFactory.getRegisterUrl();

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		try {
			net.createPostThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8), progress);
		} catch (UnsupportedEncodingException e) {
			progress.setError(e.toString());
		}
	}

	public void getPersonInfo(NetProgress progress) {
		String url = urlFactory.getAimUserUrl(getUsername());
		net.createGetThread(url, progress);
	}

	public void logout() {
		books.clear();
		friends.clear();
		informs.clear();
	}

	public int getPersonBookNum() {
		return books.size();
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

	/**
	 * ����ȡ��������鱾�б���Ϣresponseת����List
	 */
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

	/**
	 * ����ȡ��������鱾�б���Ϣarrayת����List
	 */
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

	/**
	 * ����ȡ������ĺ���/Ⱥ���б���Ϣresponseת����List
	 */
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

	/**
	 * ����ȡ���������Ϣ�б���Ϣresponseת����List
	 */
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

	/**
	 * ���鱾���뵽���ݿ�
	 */
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

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		String url = urlFactory.getCreateBookUrl();

		try {
			net.createPostThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8),
					new AddToDBProgress(progress));
		} catch (UnsupportedEncodingException e) {
			progress.setError(e.toString());
		}
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
		String url = urlFactory.getAimBookUrl((Integer) book.get(Book.ID));
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
		bookRequest(aimName, book, "������Ϣ", Inform.REQUEST_TYPE_BORROW, progress);
	}

	public void askReturn(Map<String, Object> book, NetProgress progress) {
		String holder = (String) book.get(Book.HOLDER);
		bookRequest(holder, book, "���㻹��", Inform.REQUEST_TYPE_RETURN, progress);
	}

	public void returnBook(Map<String, Object> book, NetProgress progress) {
		String owner = (String) book.get(Book.OWNER);
		bookRequest(owner, book, "������", Inform.REQUEST_TYPE_RETURN, progress);
	}

	/**
	 * �����鱾��description���Ҵ�������
	 */
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

	/**
	 * ��������
	 */
	private void createRequest(String aimName, int type, String description,
			NetProgress progress) {
		String url = urlFactory.getInformCreateUrl();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Inform.TYPE, Integer.toString(type)));
		nvps.add(new BasicNameValuePair(Inform.DESCRIPTION, description));
		nvps.add(new BasicNameValuePair(Inform.TO, aimName));

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		try {
			net.createPostThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8), progress);
		} catch (UnsupportedEncodingException e) {
			progress.setError(e.toString());
		}
	}

	/**
	 * ��ȡ�鱾�б�
	 */
	public void getBookList(NetProgress progress) {
		getBookList(getUsername(), progress);
	}

	public void getBookList(String name, NetProgress progress) {
		String url = urlFactory.getBookListUrl(name);
		net.createGetThread(url, progress);
	}

	/**
	 * ��ȡfromΪ������б�
	 */
	public void getSendInformList(NetProgress progress) {
		String url = urlFactory.getInformListFromUrl(getUsername());
		net.createGetThread(url, progress);
	}

	/**
	 * ��ȡtoΪ������б�
	 */
	public void getReceiveInformList(NetProgress progress) {
		String url = urlFactory.getInformListToUrl(getUsername());
		net.createGetThread(url, progress);
	}

	/**
	 * ������Ϣ��status
	 */
	public void updateRequest(int id, int status, NetProgress progress) {
		String url = urlFactory.getAimInformUrl(id);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Inform.STATUS, Integer.toString(status)));

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		try {
			net.createPutThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8), progress);
		} catch (UnsupportedEncodingException e) {
			progress.setError(e.toString());
		}
	}

	public void deleteRequest(int id, NetProgress progress) {
		String url = urlFactory.getAimInformUrl(id);
		net.createDeleteThread(url, progress);
	}

	/**
	 * ��ȡ�����б�
	 */
	public void getFriendList(NetProgress progress) {
		String url = urlFactory.getFriendListUrl();
		net.createGetThread(url, progress);
	}

	public void deleteFriend(Map<String, Object> friend, NetProgress progress) {
		String url = urlFactory.getAimFriendUrl((String) friend
				.get(Friend.NAME));
		net.createDeleteThread(url, progress);
	}

	public String informMapToStr(Map<String, Object> item) {
		String ret = (String) item.get(Inform.TIME);
		ret += ("\nfrom:" + item.get(Inform.FROM));
		ret += ("\nto:" + item.get(Inform.TO));
		ret += "\n����:";
		switch ((Integer) item.get(Inform.TYPE)) {
		case Inform.REQUEST_TYPE_BORROW:
			ret += "����";
			break;
		case Inform.REQUEST_TYPE_RETURN:
			ret += "����";
			break;
		}

		ret += "\n";

		switch ((Integer) item.get(Inform.STATUS)) {
		case Inform.REQUEST_STATUS_UNPROCESSED:
			ret += "δ����";
			break;
		case Inform.REQUEST_STATUS_PERMITTED:
			ret += "������";
			break;
		case Inform.REQUEST_STATUS_REFUSED:
			ret += "�Ѿܾ�";
			break;
		}
		return ret;
	}

	/**
	 * ͨ��id���鱾��list�л�ȡmap����
	 */
	public Map<String, Object> getBookById(int id) {
		for (Map<String, Object> book : books) {
			if ((Integer) book.get(Book.ID) == id)
				return book;
		}
		return null;
	}

	/**
	 * ͨ��name�Ӻ��ѵ�list�л�ȡmap����
	 */
	public Map<String, Object> getFriendByName(String name) {
		for (Map<String, Object> friend : friends) {
			if (name.equals(friend.get(Friend.NAME)))
				return friend;
		}
		return null;
	}

	public void loadBookImgs() {
		for (Map<String, Object> book : books) {
			loadBookImg((String) book.get(Book.ISBN));
		}
	}

	public void loadBookImg(String isbn) {
		if (!imgManage.loadBookImgFromCache(isbn)) {
			String url = urlFactory.getAimBookImgUrl(isbn);
			net.createFileGetThread(url, new ProgressNone(),
					new BookImgProcessImpl(isbn));
		}
	}

	/**
	 * �������е�ͷ��(�������˺ͺ���)
	 */
	public void loadAvatars() {
		loadAvatar(getUsername(), avatarVersion);
		for (Map<String, Object> friend : friends) {
			String name = (String) friend.get(Friend.NAME);
			int version = (Integer) friend.get(Friend.AVATAR_VERSION);
			loadAvatar(name, version);
		}
	}

	/**
	 * ����ָ��Ŀ��name��ͷ��
	 */
	public void loadAvatar(String name, int curVersion) {
		if (curVersion == ImageManage.AVATAR_VERSION_NONE)
			return;
		Log.i(Utils.getLineInfo(), "before load from cache : " + name
				+ " version: " + Integer.toString(curVersion));
		if (!imgManage.loadAvatarFromCache(name, curVersion)) {
			String url = urlFactory.getAimAvatarUrl(name);
			net.createFileGetThread(url, new ProgressNone(),
					new AvatarProcessImpl(name, curVersion));
		}
	}

	public void createAvatar(String path, NetProgress progress) {
		Bitmap avatar = Utils.fileToAvatarBitmap(path);
		if (avatar == null) {
			progress.setError("��С/��ʽ����");
			return;
		}
		setAvatar(avatar);

		File file = imgManage.getAvatarFile(getUsername());
		if (!file.exists()) {
			progress.setError("ͷ�񱣴����");
			return;
		}
		if (!Utils.isAvatarFileSize(file)) {
			progress.setError("ͷ�񱣴����:�ļ�����");
			return;
		}
		HttpEntity parts = getFilePart(file);

		String url = urlFactory.getAimAvatarUrl(getUsername());
		net.createPostThread(url, parts, progress);
	}

	/**
	 * ��ȡ�ϴ��ļ�ʱ���ļ�HttpEntity
	 */
	private HttpEntity getFilePart(File file) {
		FileBody fileBody = null;
		try {
			fileBody = new FileBody(file);
		} catch (Exception e) {
			Toast.makeText(application, e.toString(), Toast.LENGTH_LONG).show();
		}
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("file", fileBody);
		return builder.build();
	}

	/**
	 * �����ļ�ʱ��HttpEntity�����⴦��ʽ
	 */
	private class BookImgProcessImpl implements EntityProcess {
		private String isbn;

		public BookImgProcessImpl(String isbn) {
			this.isbn = isbn;
		}

		@Override
		public String getResponse(int status, HttpEntity responseEntity) {
			if (status != NetAccess.STATUS_SUCCESS)
				return responseEntity.toString();

			InputStream is;
			try {
				is = responseEntity.getContent();
				saveBookImg(isbn, BitmapFactory.decodeStream(is));
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "ok";
		}
	}

	/**
	 * �����ļ�ʱ��HttpEntity�����⴦��ʽ
	 */
	private class AvatarProcessImpl implements EntityProcess {
		private String aimName;
		private int curVersion;

		public AvatarProcessImpl(String aimName, int curVersion) {
			this.aimName = aimName;
			this.curVersion = curVersion;
		}

		@Override
		public String getResponse(int status, HttpEntity responseEntity) {
			if (status != NetAccess.STATUS_SUCCESS)
				return responseEntity.toString();

			InputStream is;
			try {
				is = responseEntity.getContent();
				saveAvatar(aimName, BitmapFactory.decodeStream(is), curVersion);
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "ok";
		}
	}

	public void setAvatar(Bitmap avatar) {
		setAvatarVersion(avatarVersion + 1);
		saveAvatar(getUsername(), avatar, getAvatarVersion());
	}

	public void saveBookImg(String isbn, Bitmap bookImg) {
		imgManage.saveBookImg(isbn, bookImg);
	}

	public void saveAvatar(String username, Bitmap avatar, int curVersion) {
		imgManage.saveAvatar(username, avatar, curVersion);
	}

	public Bitmap getAvatarBitmap(String aimName) {
		return imgManage.getAvatarBitmap(aimName);
	}

	public Bitmap getAvatarBitmap() {
		return imgManage.getAvatarBitmap(getUsername());
	}

	public Map<String, Bitmap> getBookImgs() {
		return imgManage.getBookImgs();
	}

	public Map<String, Bitmap> getAvatars() {
		return imgManage.getAvatars();
	}

	private void setAvatarVersion(int version) {
		avatarVersion = version;
	}

	public int getAvatarVersion() {
		return avatarVersion;
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
		if (username == null)
			Log.i(Utils.getLineInfo(), "somewhere to set null");
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
