package group.acm.bookshare.function.http;

public interface HttpResponseProcess {
	public void statusError(String response);
	public void statusSuccess(String response);
}
