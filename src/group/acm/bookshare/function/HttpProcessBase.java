package group.acm.bookshare.function;

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
		switch (status) {
		case NetAccess.STATUS_SUCCESS:
			statusSuccess(response);
			break;
		case NetAccess.STATUS_ERROR:
			statusError(response);
			break;
		}
	}

	@Override
	public void error(String content) {
	}

}
