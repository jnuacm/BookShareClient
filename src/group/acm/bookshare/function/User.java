package group.acm.bookshare.function;

import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.EntityProcess;
import group.acm.bookshare.function.http.NetAccess.StreamProcess;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.function.http.ProgressNone;
import group.acm.bookshare.function.http.UrlStringFactory;
import group.acm.bookshare.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class User {
	public static final int PERTIME_LOAD_NUMBER = 10;

	private String username;
	private String password;
	private String area;
	private String email;
	private int is_group;
	private int avatarVersion;
	private int curLoadImgIndex;
	private int curLoadAvatarIndex;
	private String userid;

	private List<Map<String, Object>> books; // 保存user的书本列表数据
	private List<Map<String, Object>> friends; // 保存uesr的好友列表数据
	private List<Map<String, Object>> informs; // 保存user的消息列表数据
	private List<Map<String, Object>> comments; // 临时保存书本评论列表数据
	private List<Map<String, Object>> hotBooks; // 临时保存热书推荐列表数据

	private User curFriend;

	private Application application;
	private ImageManage imgManage; // 图像管理对象
	private UrlStringFactory urlFactory; // url构造类
	private NetAccess net; // 网络访问对象

	/**
	 * 程序的本地主用户的构造函数
	 */
	public User(Application application) {
		books = new ArrayList<Map<String, Object>>();
		friends = new ArrayList<Map<String, Object>>();
		informs = new ArrayList<Map<String, Object>>();
		comments = new ArrayList<Map<String, Object>>();
		hotBooks = new ArrayList<Map<String, Object>>();
		this.application = application;
		avatarVersion = 0;
		curLoadImgIndex = 0;
		curLoadAvatarIndex = 0;
		imgManage = ImageManage.getInstance(application);
		urlFactory = new UrlStringFactory(application);
		net = NetAccess.getInstance();
	}

	/**
	 * 其它好友用户的构造函数
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
		comments = new ArrayList<Map<String, Object>>();
		hotBooks = new ArrayList<Map<String, Object>>();
		this.application = application;
		curLoadImgIndex = 0;
		curLoadAvatarIndex = 0;
		imgManage = ImageManage.getInstance(application);
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
		setAvatarVersion((Integer) item.get(Friend.AVATAR_VERSION));
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
			int is_group, String area, NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("is_group", Integer.toString(is_group)));
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
		comments.clear();
		hotBooks.clear();
		imgManage.clearBitmap();
		imgManage.clearOverCacheFile();
		curLoadImgIndex = 0;
		curLoadAvatarIndex = 0;
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

	public List<Map<String, Object>> getCommentListData() {
		return comments;
	}

	public List<Map<String, Object>> getHotListData() {
		return hotBooks;
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

	public void clearCommentData() {
		comments.clear();
	}

	public void clearHotBookData() {
		hotBooks.clear();
	}

	/**
	 * 将获取的网络的书本列表信息response转化成List
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
	 * 将获取的网络的书本列表信息array转化成List
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
	 * 将获取的网络的好友/群组列表信息response转化成List
	 */
	public void addFriendDataToList(String response) {
		try {
			JSONObject jsonobj = new JSONObject(response);
			JSONArray jsonarray = jsonobj.getJSONArray("friend");
			addFriendDataToList(jsonarray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addFriendDataToList(JSONArray array) {
		List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < array.length(); i++) {
			try {
				Map<String, Object> friend = Friend.objToFriend(array
						.getJSONObject(i));
				// 此处为了将好友分为普通好友和群组而进行
				if (((Integer) friend.get(Friend.IS_GROUP)) == Friend.GROUP) {
					groups.add(friend);
				} else {
					friends.add(friend);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		friends.addAll(groups);
	}

	/**
	 * 将获取的网络的消息列表信息response转化成List
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

	public boolean addCommentDataToList(String response) {
		JSONArray jsonarray;
		try {
			jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject item = jsonarray.getJSONObject(i);
				Map<String, Object> tmp = Comment.objToComment(item);
				comments.add(tmp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addHotBooksDataToList(String response) {
		JSONArray jsonarray;
		try {
			jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject item = jsonarray.getJSONObject(i);
				Map<String, Object> tmp = Book.objToBook(item);
				hotBooks.add(tmp);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int findLocalBookByIsbn(String isbn) {
		int sum = 0;
		for (Map<String, Object> book : books) {
			if (isbn.equals(book.get(Book.ISBN))
					&& getUsername().equals(book.get(Book.OWNER)))
				sum++;
		}
		return sum;
	}

	public void addBook(String isbn, NetProgress progress) {
		Book book = new Book(this.application);
		book.getBookByIsbn(isbn, new DoubanBookProgress(isbn, progress));
	}

	private class DoubanBookProgress extends HttpProcessBase {
		private NetProgress progress;
		private String isbn;

		public DoubanBookProgress(String isbn, NetProgress progress) {
			this.progress = progress;
			this.isbn = isbn;
		}

		@Override
		public void statusError(String response) {
			progress.setError(response);
		}

		@Override
		public void statusSuccess(String response) {
			Map<String, Object> book = Book.doubanStrToBook(response);
			book.put(Book.ISBN, isbn);
			addToDB(book, progress);
			progress.setProcess(50);
		}
	}

	/**
	 * 将书本加入到数据库
	 */
	private void addToDB(Map<String, Object> data, NetProgress progress) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair(Book.NAME, (String) data.get(Book.NAME)));
		nvps.add(new BasicNameValuePair(Book.ISBN, (String) data.get(Book.ISBN)));
		nvps.add(new BasicNameValuePair(Book.AUTHOR, (String) data
				.get(Book.AUTHOR)));
		nvps.add(new BasicNameValuePair(Book.IMG_URL_SMALL, (String) data
				.get(Book.IMG_URL_SMALL)));
		nvps.add(new BasicNameValuePair(Book.IMG_URL_MEDIUM, (String) data
				.get(Book.IMG_URL_MEDIUM)));
		nvps.add(new BasicNameValuePair(Book.IMG_URL_LARGE, (String) data
				.get(Book.IMG_URL_LARGE)));
		nvps.add(new BasicNameValuePair(Book.DESCRIPTION, (String) data
				.get(Book.DESCRIPTION)));
		nvps.add(new BasicNameValuePair(Book.PUBLISHER, (String) data
				.get(Book.PUBLISHER)));
		nvps.add(new BasicNameValuePair(Book.STATUS, Integer
				.toString(Book.STATUS_BORROW)));
		nvps.add(new BasicNameValuePair(Book.TAGS, (String) data.get(Book.TAGS)));

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

	public void addComment(String isbn, String content, NetProgress progress) {
		String url = urlFactory.getCommentUrl(isbn);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair(Comment.PERSON, getUsername()));
		nvps.add(new BasicNameValuePair(Comment.CONTENT, content));
		Time now = new Time();
		now.setToNow();
		String timeStr = now.toString();
		nvps.add(new BasicNameValuePair(Comment.DATE, timeStr));

		for (NameValuePair i : nvps) {
			Log.i("nvps:", i.getName() + ":" + i.getValue());
		}

		try {
			net.createPostThread(url,
					new UrlEncodedFormEntity(nvps, HTTP.UTF_8), progress);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void deleteComment(Map<String, Object> comment, NetProgress progress) {
		String url = urlFactory
				.getCommentUrl((Integer) comment.get(Comment.ID));
		net.createDeleteThread(url, progress);
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

	/**
	 * 构造书本的description并且创建请求
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
	 * 创建请求
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
	 * 获取书本列表
	 */
	public void getBookList(NetProgress progress) {
		getBookList(getUsername(), progress);
	}

	public void getBookList(String name, NetProgress progress) {
		String url = urlFactory.getBookListUrl(name);
		net.createGetThread(url, progress);
	}

	/**
	 * 获取from为自身的列表
	 */
	public void getSendInformList(NetProgress progress) {
		String url = urlFactory.getInformListFromUrl(getUsername());
		net.createGetThread(url, progress);
	}

	/**
	 * 获取to为自身的列表
	 */
	public void getReceiveInformList(NetProgress progress) {
		String url = urlFactory.getInformListToUrl(getUsername());
		net.createGetThread(url, progress);
	}

	/**
	 * 通过isbn获取书本评论
	 */
	public void getCommentList(String isbn, NetProgress progress) {
		String url = urlFactory.getCommentUrl(isbn);
		net.createGetThread(url, progress);
	}

	/**
	 * 获取热书推荐列表
	 */
	public void getHotBookList(NetProgress progress) {
		clearHotBookData();
		if (books.size() <= 0)
			return;
		Map<String, Integer> tagsMap = new HashMap<String, Integer>();
		Map<String, Boolean> flags = new HashMap<String, Boolean>();
		for (Map<String, Object> book : books) {
			if (flags.containsKey(book.get(Book.NAME)))
				continue;
			flags.put((String) book.get(Book.NAME), true);
			List<Map<String, Object>> tags = Book.getTags(book);
			for (Map<String, Object> tag : tags) {
				String name = (String) tag.get("name");
				int count = (Integer) tag.get("count");
				if (!tagsMap.containsKey(name)) {
					tagsMap.put(name, count);
				} else {
					tagsMap.put(name, tagsMap.get(name) + count);
				}
			}
		}

		List<Map.Entry<String, Integer>> result = new ArrayList<Map.Entry<String, Integer>>(
				tagsMap.entrySet());
		Collections.sort(result, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> lhs,
					Map.Entry<String, Integer> rhs) {
				return rhs.getValue() - lhs.getValue();
			}
		});

		if (result.size() <= 0)
			return;

		Map.Entry<String, Integer> tag = result.get(0);
		String tagSearchUrl = urlFactory.getDoubanSearchUrl(tag.getKey());
		net.createGetThread(tagSearchUrl, new TagSearchProgress(0, result,
				progress));
	}

	private class TagSearchProgress extends HttpProcessBase {
		private int curTagIdx;
		private List<Map.Entry<String, Integer>> result;
		private NetProgress progress;

		public TagSearchProgress(int curIdx,
				List<Map.Entry<String, Integer>> result, NetProgress progress) {
			this.curTagIdx = curIdx;
			this.progress = progress;
			this.result = result;
		}

		@Override
		public void statusError(String response) {
		}

		@Override
		public void statusSuccess(String response) {
			hotBooks.addAll(Book.getWantedBooks(response));
			int nextIdx = curTagIdx + 1;
			if (curTagIdx == result.size() - 1 || curTagIdx == 4) { // 最后一个标签或第5个标签被搜索完成后
				progress.setAfter(NetAccess.STATUS_SUCCESS, "成功");
				return;
			}
			String tagSearchUrl = urlFactory.getDoubanSearchUrl(result.get(
					nextIdx).getKey());
			net.createGetThread(tagSearchUrl, new TagSearchProgress(nextIdx,
					result, progress));
		}
	}

	/**
	 * 更新消息的status
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
	 * 获取好友列表
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

	public void deleteFriendData(String aimName) {
		for (Map<String, Object> friend : friends) {
			if (aimName.equals(friend.get(Friend.NAME))) {
				friends.remove(friend);
				return;
			}
		}
	}

	/**
	 * 通过id从书本的list中获取map对象
	 */
	public Map<String, Object> getBookById(int id) {
		for (Map<String, Object> book : books) {
			if ((Integer) book.get(Book.ID) == id)
				return book;
		}
		return null;
	}

	public void getUrlBookImg(String url, NetProgress progress,
			StreamProcess process) {
		net.createUrlConntectionGetThread(url, progress, process);
	}

	/**
	 * 通过name从好友的list中获取map对象
	 */
	public Map<String, Object> getFriendByName(String name) {
		for (Map<String, Object> friend : friends) {
			if (name.equals(friend.get(Friend.NAME)))
				return friend;
		}
		return null;
	}

	public void loadInitImgs(NetProgress progress) {
		int i;
		for (i = 0; i < 2 * User.PERTIME_LOAD_NUMBER
				&& curLoadImgIndex < books.size(); i++, curLoadImgIndex++) {
			Map<String, Object> book = books.get(curLoadImgIndex);
			loadBookImg(book, progress);
		}
	}

	public int loadBookImgs() {
		int i;
		for (i = 0; i < User.PERTIME_LOAD_NUMBER
				&& curLoadImgIndex < books.size(); i++, curLoadImgIndex++) {
			Map<String, Object> book = books.get(curLoadImgIndex);
			loadBookImg(book);
		}
		return i;
	}

	public void loadBookImg(Map<String, Object> book) {
		loadBookImg(book, new ProgressNone());
	}

	public void loadBookImg(Map<String, Object> book, NetProgress progress) {
		if (!imgManage.loadBookImgFromCache((String) book.get(Book.ISBN))) {
			String url = (String) book.get(Book.IMG_URL_SMALL);
			net.createUrlConntectionGetThread(url, progress,
					new BookImgProcessImpl((String) book.get(Book.ISBN)));
		}
	}

	public void clearBookBitmap() {
		curLoadImgIndex = 0;
		imgManage.clearBookBitmap();
	}

	public void clearAvatarBitmap() {
		curLoadAvatarIndex = 0;
		imgManage.clearAvatarBitmap();
	}

	/**
	 * 加载初始头像
	 */
	public void loadInitAvatar(NetProgress progress) {
		int i;
		for (i = 0; i < 2 * User.PERTIME_LOAD_NUMBER
				&& curLoadAvatarIndex < friends.size(); i++, curLoadAvatarIndex++) {
			Map<String, Object> friend = friends.get(curLoadAvatarIndex);
			String name = (String) friend.get(Friend.NAME);
			int version = (Integer) friend.get(Friend.AVATAR_VERSION);
			loadAvatar(name, version, progress);
		}
	}

	public int loadAvatars() {
		int i;
		for (i = 0; i < User.PERTIME_LOAD_NUMBER
				&& curLoadAvatarIndex < friends.size(); i++, curLoadAvatarIndex++) {
			Map<String, Object> friend = friends.get(curLoadAvatarIndex);
			String name = (String) friend.get(Friend.NAME);
			int version = (Integer) friend.get(Friend.AVATAR_VERSION);
			loadAvatar(name, version);
		}
		return i;
	}

	public void loadAvatar(NetProgress progress) {
		loadAvatar(getUsername(), getAvatarVersion(), progress);
	}

	public void loadAvatar(String name, int curVersion) {
		loadAvatar(name, curVersion, new ProgressNone());
	}

	/**
	 * 加载指定目标name的头像
	 */
	public void loadAvatar(String name, int curVersion, NetProgress progress) {
		if (curVersion == ImageManage.AVATAR_VERSION_NONE)
			return;
		Log.i(Utils.getLineInfo(), "before load from cache : " + name
				+ " version: " + Integer.toString(curVersion));
		if (!imgManage.loadAvatarFromCache(name, curVersion)) {
			String url = urlFactory.getAimAvatarUrl(name);
			net.createFileGetThread(url, progress, new AvatarProcessImpl(name,
					curVersion));
		}
	}

	public void createAvatar(String path, NetProgress progress) {
		Bitmap avatar = Utils.fileToAvatarBitmap(path);
		if (avatar == null) {
			progress.setError("大小/格式不对");
			return;
		}
		setAvatar(avatar);

		File file = imgManage.getAvatarFile(getUsername());
		if (!file.exists()) {
			progress.setError("头像保存出错");
			return;
		}
		if (!Utils.isAvatarFileSize(file)) {
			progress.setError("头像保存出错:文件过大");
			return;
		}
		HttpEntity parts = getFilePart(file);

		String url = urlFactory.getAimAvatarUrl(getUsername());
		net.createPostThread(url, parts, progress);
	}

	public void bookSearch(String isbn, String name, String author,
			String publisher, NetProgress progress) {
		String url = urlFactory.getBookSearchUrl(isbn, name, author, publisher);
		net.createGetThread(url, progress);
	}

	/**
	 * 获取上传文件时的文件HttpEntity
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
	 * 下载文件时对HttpEntity的特殊处理方式
	 */
	private class BookImgProcessImpl implements StreamProcess {
		private String isbn;

		public BookImgProcessImpl(String isbn) {
			this.isbn = isbn;
		}

		@Override
		public String getResponse(int status, InputStream responseStream) {
			if (status != NetAccess.STATUS_SUCCESS)
				return "图片加载失败";
			String ret = "ok";
			try {
				saveBookImg(isbn, BitmapFactory.decodeStream(responseStream));
			} catch (Exception e) {
				ret = e.toString();
			}
			return ret;
		}
	}

	/**
	 * 下载文件时对HttpEntity的特殊处理方式
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

	public void setFriend(User friend) {
		curFriend = friend;
	}

	public User getFriend() {
		return curFriend;
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
