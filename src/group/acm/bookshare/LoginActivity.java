package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private User localUser;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		LocalApp localapp = (LocalApp) getApplication();
		localUser = localapp.getUser();
		fillInInfo();
	}

	private void fillInInfo() {
		SharedPreferences info = this.getSharedPreferences("user_info",
				Context.MODE_PRIVATE);
		((TextView) findViewById(R.id.USERNAME)).setText(info.getString(
				"username", ""));

		((TextView) findViewById(R.id.PASSWORD)).setText(info.getString(
				"password", ""));
	}

	public void Login(View v) { // 登录回调函数
		if (Utils.isQuickClick())
			return;
		String username, password;
		username = ((TextView) findViewById(R.id.USERNAME)).getText()
				.toString();
		password = ((TextView) findViewById(R.id.PASSWORD)).getText()
				.toString();

		recordInfo(username, password);

		localUser.setUser(username, password);
		localUser.login(new LoginHandler());
	}

	private void recordInfo(String username, String password) {
		SharedPreferences info = this.getSharedPreferences("user_info",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = info.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}

	@SuppressLint("HandlerLeak")
	private class LoginHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_BEFORE:
				findViewById(R.id.loginProgressBar).setVisibility(View.VISIBLE);
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
			localUser.clearBookData();
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
