package group.acm.bookshare.function;

import android.content.Context;
import android.widget.Toast;

public class ProgressShow extends HttpProcessBase {
	private Context context;
	private String success;
	private String fail;

	public ProgressShow(Context context, String success, String fail) {
		this.context = context;
		this.success = success;
		this.fail = fail;
	}

	public void error(String content) {
		Toast.makeText(context, content, Toast.LENGTH_LONG).show();
	}

	@Override
	public void statusError(String response) {
		Toast.makeText(context, fail, Toast.LENGTH_LONG).show();
	}

	@Override
	public void statusSuccess(String response) {
		Toast.makeText(context, success, Toast.LENGTH_LONG).show();
	}

}
