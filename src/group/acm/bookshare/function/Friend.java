package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.Map;

import android.app.Application;

public class Friend {
	protected int image = R.drawable.friend1;
	protected String name = "";
	protected String email = "";
	protected String area = "";
	protected int is_group = 0;

	protected Application application;

	public Friend() {
	}

	public Friend(Map<String, Object> data) {
		this.name = (String) data.get("name");
		image = R.drawable.friend1;

		//this.image = (String)data.get("image");
		this.email = (String)data.get("email");
		this.area = (String)data.get("area");
		this.is_group = (Integer)data.get("is_group");
	}

	public Friend(Application application) {
		this.application = application;
	}

	public int getImage() {
		return this.image;
	}

	public String getName() {
		return this.name;
	}

	public String getEmail() {
		return this.email;
	}

	public String getArea() {
		return this.area;
	}

	public int getIs_group() {
		return is_group;
	}

}
