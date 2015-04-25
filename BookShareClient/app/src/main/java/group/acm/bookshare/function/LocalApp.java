package group.acm.bookshare.function;

import android.app.Application;

public class LocalApp extends Application {
	private User user = new User(this);

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public User getUser() {
		return user;
	}
}
