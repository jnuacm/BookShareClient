package group.acm.bookshare.function;

import android.text.format.Time;

public abstract class Inform {
	public static final String URL_GETSEND_INFORM = "bookshareyii/index.php/api/request/from/";
	public static final String URL_GETRECEIVE_INFORM = "bookshareyii/index.php/api/request/to/";

	protected int id;
	protected Time time;
	protected String from;
	protected String to;
	protected int type;
	protected String description;
	protected int status;

}
