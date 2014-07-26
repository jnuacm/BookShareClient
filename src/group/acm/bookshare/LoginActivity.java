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
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity {
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	public void Login(View v) { // 登录回调函数
		if (Utils.isQuickClick())
			return;
		String username, password;
		// username = ((TextView)
		// findViewById(R.id.USERNAME)).getText().toString();
		// password = ((TextView)
		// findViewById(R.id.PASSWORD)).getText().toString();

		// ////////////////////////！！！注意！！！！///////////////////////////////////////////
		// ////////////////////////以下为测试代码//////////////////////////////////////////////
		username = "gg";
		password = "1234";
		// ////////////////////////以上为测试代码//////////////////////////////////////////////
		LocalApp localapp = (LocalApp) getApplication();
		User user = localapp.getUser();
		
		user.setUser(username, password);
		Handler mainHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:
					break;
				case NetAccess.NETMSG_AFTER:
					showResponse(msg.getData());
					break;
				}
			}
		};
		user.login(mainHandler);
	}

	public void Register(View v) { // 注册回调函数
		if (Utils.isQuickClick())
			return;
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showResponse(Bundle data) {
		// TODO Auto-generated method stub

		int status = data.getInt("status");
		if (status == NetAccess.STATUS_SUCCESS) {
			Bundle retdata = new Bundle();
			retdata.putString("response", data.getString("response"));

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtras(retdata);
			startActivity(intent);
			finish();
		} else if (status == NetAccess.STATUS_ERROR) {
			Toast.makeText(LoginActivity.this,
					this.getString(R.string.login_error), Toast.LENGTH_LONG)
					.show();
		}
	}

	public void error(String content) {
		// TODO Auto-generated method stub

	}

}
