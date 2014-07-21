package group.acm.bookshare.function;

import android.app.Application;

public class LocalApp extends Application {
	private User user = new User(this);
	public User getUser(){
		return user;
	}
}
