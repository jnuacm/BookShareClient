package group.acm.bookshare.function;

public interface HttpResponseProcess {
	public void statusError(String response);
	public void statusSuccess(String response);
}
