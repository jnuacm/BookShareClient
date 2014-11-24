package group.acm.bookshare.function.http;

import group.acm.bookshare.R;
import android.content.Context;

// 工厂类，负责构造url
public class UrlStringFactory {
	private Context appContext;

	public UrlStringFactory(Context appContext) {
		this.appContext = appContext;
	}

	public String getLoginUrl() {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_login);
		return url;
	}

	public String getRegisterUrl() {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_register);
		return url;
	}

	public String getAimUserUrl(String username) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_user);
		url += username;
		return url;
	}

	public String getCreateBookUrl() {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_book);
		return url;
	}

	public String getAimBookUrl(int id) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_book);
		url += Integer.toString(id);
		return url;
	}

	public String getBookListUrl(String aimName) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_book);
		url += aimName;
		url += appContext.getString(R.string.url_book_all);
		return url;
	}

	public String getFriendListUrl() {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_friend);
		return url;
	}

	public String getAimFriendUrl(String aimName) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_friend);
		url += aimName;
		return url;
	}

	public String getInformCreateUrl() {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_inform);
		return url;
	}

	public String getInformListFromUrl(String aimName) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_inform);
		url += appContext.getString(R.string.url_inform_from);
		url += aimName;
		return url;
	}

	public String getInformListToUrl(String aimName) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_inform);
		url += appContext.getString(R.string.url_inform_to);
		url += aimName;
		return url;
	}

	public String getAimInformUrl(int id) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_inform);
		url += Integer.toString(id);
		return url;
	}

	public String getAimAvatarUrl(String aimName) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_avatar);
		url += aimName;
		return url;
	}

	public String getAimBookImgUrl(String isbn) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_bookimg);
		url += isbn;
		return url;
	}

	public String getBookSearchUrl(String isbn, String name, String author,
			String publisher) {
		String url = appContext.getString(R.string.url_host);
		url += appContext.getString(R.string.url_book);
		url += appContext.getString(R.string.url_book_search);
		url += ("?isbn=" + isbn);
		url += ("&name=" + name);
		url += ("&author=" + author);
		url += ("&publisher=" + publisher);
		return url;
	}

	public String getCommentListUrl(String isbn) {
		return null;
	}
}
