package group.acm.bookshare.function;

import com.baidu.frontia.FrontiaApplication;

public class LocalApp extends FrontiaApplication {
	private User user = new User(this);

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public User getUser() {
		return user;
	}
}
