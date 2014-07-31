package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.Map;

import android.app.Application;

public class Friend {
	protected int image = R.drawable.friend1;
	protected String name = "";
	protected String email = "";
	protected String area = "";
	
	protected Application application;
	
	public Friend() {
	}
	
	public Friend(Map<String, Object> data) {
		this.name = (String)data.get("name");
		image = R.drawable.friend1;
		//this.image = (String)data.get("image");
		this.email = (String)data.get("email");
		this.area = (String)data.get("area");
	}

	public Friend(Application application) {
		this.application = application;
	}
	
	public int getImage() {
		return this.image;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public String getArea(){
		return this.area;
	}
	
}
