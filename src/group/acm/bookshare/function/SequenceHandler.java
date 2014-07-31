package group.acm.bookshare.function;

import android.os.Handler;

public class SequenceHandler extends Handler {
	protected Handler handler;

	public SequenceHandler(Handler handler) {
		this.handler = handler;
	}
}
