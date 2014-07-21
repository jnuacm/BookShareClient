package group.acm.bookshare.function;

import android.app.Application;

public class LocalApp extends Application {

	private String username = "";
	private String isbn = "";

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getISBN() {
		return isbn;
	}

	public void setISBN(String isbn) {
		this.isbn = isbn;
	}
}
