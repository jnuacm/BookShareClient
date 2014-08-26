package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private User localUser;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		LocalApp localapp = (LocalApp) getApplication();
		localUser = localapp.getUser();
	}

	@SuppressLint("HandlerLeak")
	public void Login(View v) { // 登录回调函数
		if (Utils.isQuickClick())
			return;
		String username, password;
		username = ((TextView) findViewById(R.id.USERNAME)).getText()
				.toString();
		password = ((TextView) findViewById(R.id.PASSWORD)).getText()
				.toString();

		if (!(username.length() > 0 && password.length() > 0)) {
			username = "amy";
			password = "1234";
		}

		localUser.setUser(username, password);
		Handler loginHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					findViewById(R.id.loginProgressBar).setVisibility(
							View.VISIBLE);
					break;
				case NetAccess.NETMSG_AFTER:
					showResponse(msg.getData());
					break;
				case NetAccess.NETMSG_ERROR:
					Toast.makeText(LoginActivity.this,
							msg.getData().getString("error"), Toast.LENGTH_LONG)
							.show();
					break;
				}
			}
		};
		localUser.login(loginHandler);
	}

	public void Register(View v) { // 注册回调函数
		if (Utils.isQuickClick())
			return;
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
	}

	public void showResponse(Bundle data) {
		int status = data.getInt("status");
		if (status == NetAccess.STATUS_SUCCESS) {
			String response = data.getString("response");
			localUser.addBookDataToList(response);
			localUser.addFriendDataToList(response);
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else if (status == NetAccess.STATUS_ERROR) {
			findViewById(R.id.loginProgressBar).setVisibility(View.INVISIBLE);
			Toast.makeText(LoginActivity.this,
					this.getString(R.string.login_error), Toast.LENGTH_LONG)
					.show();
		}
	}
}
