package group.acm.bookshare.function.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public abstract class HttpProgress implements NetProgress {
	private Handler handler;

	public HttpProgress() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					before();
					break;
				case NetAccess.NETMSG_PROCESS:
					Bundle data = msg.getData();
					process(data.getInt("time"));
					break;
				case NetAccess.NETMSG_AFTER:
					data = msg.getData();
					after(data.getInt(NetAccess.STATUS),
							data.getString(NetAccess.RESPONSE));
					break;
				case NetAccess.NETMSG_ERROR:
					data = msg.getData();
					error(data.getString(NetAccess.ERROR));
					break;
				}
			}
		};
	}

	@Override
	public void setBefore() {
		handler.sendEmptyMessage(NetAccess.NETMSG_BEFORE);
	}

	@Override
	public void setProcess(int time) {
		Bundle data = new Bundle();
		data.putInt("time", time);

		Message msg = Message.obtain();
		msg.what = NetAccess.NETMSG_PROCESS;
		msg.setData(data);
		handler.sendMessage(msg);
	}

	@Override
	public void setAfter(int status, String response) {
		Bundle data = new Bundle();
		data.putInt(NetAccess.STATUS, status);
		data.putString(NetAccess.RESPONSE, response);

		Message msg = Message.obtain();
		msg.what = NetAccess.NETMSG_AFTER;
		msg.setData(data);
		handler.sendMessage(msg);
	}

	@Override
	public void setError(String content) {
		Message msg = Message.obtain();
		msg.what = NetAccess.NETMSG_ERROR;
		Bundle data = new Bundle();
		data.putString(NetAccess.ERROR, content);
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
	public static ProgressShow createShowProgress(Context context, String success,
			String fail) {
		return new ProgressShow(context, success, fail);
	}
}
