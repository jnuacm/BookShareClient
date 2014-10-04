package group.acm.bookshare.function.http;

public interface NetProgress {
	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_ERROR = 403;
	static final int STATUS_DEFAULT = STATUS_ERROR;

	public static final int NETMSG_BEFORE = 10;
	public static final int NETMSG_PROCESS = 20;
	public static final int NETMSG_AFTER = 30;
	public static final int NETMSG_ERROR = 40;

	public void setBefore();
	public void setProcess(int time);
	public void setAfter(int status, String response);
	public void setError(String content);
	public void before();
	public void process(int time);
	public void after(int status, String response);
	public void error(String content);
}
