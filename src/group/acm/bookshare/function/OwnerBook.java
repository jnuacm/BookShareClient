package group.acm.bookshare.function;

import android.app.Application;

public class OwnerBook extends Book {
	public OwnerBook(Application application) {
		super(application);
		// TODO Auto-generated constructor stub
	}
	protected String owner;
	protected String status;
}
