package group.acm.bookshare.function.http;

public abstract class HttpProcessBase extends HttpProgress implements
		HttpResponseProcess {

	@Override
	public void before() {
	}

	@Override
	public void process(int time) {
	}

	@Override
	public void after(int status, String response) {
		if (status == NetAccess.STATUS_SUCCESS) {
			statusSuccess(response);
		} else {
			statusError(response);
		}
	}

	@Override
	public void error(String content) {
	}
}
