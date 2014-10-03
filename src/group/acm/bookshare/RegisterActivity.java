package group.acm.bookshare;

import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private User localUser;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		localUser = ((LocalApp) getApplication()).getUser();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	@SuppressLint("HandlerLeak")
	public void confirm(View v) { // 确定回调函数
		if (Utils.isQuickClick())
			return;
		String username, password, email, area;

		EditText edittext = (EditText) findViewById(R.id.registerusername);
		username = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registerpassword);
		password = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registeremail);
		email = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registerarea);
		area = edittext.getText().toString();

		if (!(username.length() > 0 && password.length() > 0 && email.length() > 0)) {
			Toast.makeText(this, "请输入完整信息", Toast.LENGTH_LONG).show();
			return;
		}
		if (area.length() <= 0)
			area = "无";

		localUser.register(username, password, email, area,
				HttpProcessBase.createShowProgress(this, "注册成功", "注册失败"));
	}

	public void cancel(View v) {
		finish();
	}

}
