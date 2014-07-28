package group.acm.bookshare.function;

import java.util.Map;

import android.app.Application;

public class OwnerBook extends Book {
	public OwnerBook(Application application) {
		super(application);
		// TODO Auto-generated constructor stub
	}

	protected String owner;
	protected String holder;
	protected String status;

	public OwnerBook(Map<String, Object> data) {
		super();
		this.isbn = (String)data.get("isbn");
		this.name = (String)data.get("name");
		this.coverurl = (String)data.get("coverurl");
		this.authors = (String)data.get("authors");
		this.description = (String)data.get("description");
		this.owner = (String)data.get("owner");
		this.holder = (String)data.get("holder");
		this.status = (String)data.get("status");
	}
}
