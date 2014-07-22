package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.Update;
import group.acm.bookshare.function.User;

import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity implements Update {
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	public void Login(View v) {
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
		user.login(this);
	}

	public void Register(View v)// 登录相应按钮
	{

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

	@Override
	public void before() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(Map<String, Object> map) {
		// TODO Auto-generated method stub
		int status = (Integer) map.get("status");

		if (status == NetAccess.STATUS_SUCCESS) {
			Bundle retdata = new Bundle();
			retdata.putString("response", (String) map.get("response"));

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

	@Override
	public void error(String content) {
		// TODO Auto-generated method stub
		
	}

}
