package group.acm.bookshare;

import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.util.Utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
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
			username = "amy";
			password = "12345";
			email = "kk@qd.com";
			area = "shaoguan";
		}
		if (area.length() <= 0)
			area = "无";

		// 访问网络
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("area", area));

		String url = RegisterActivity.this.getString(R.string.url_host);
		url += RegisterActivity.this.getString(R.string.url_register);
		NetAccess network = NetAccess.getInstance();

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case NetAccess.NETMSG_BEFORE:

					break;
				case NetAccess.NETMSG_AFTER:
					Bundle data = msg.getData();
					if (NetAccess.STATUS_SUCCESS == (data.getInt("status"))) {
						Toast.makeText(RegisterActivity.this, "yes!success!",
								Toast.LENGTH_LONG).show();
					} else if (NetAccess.STATUS_ERROR == (data.getInt("status"))) {
						Toast.makeText(RegisterActivity.this, "no!error!",
								Toast.LENGTH_LONG).show();
					}
					break;
				}
			}
		};
		network.createPostThread(url, nvps, handler);
	}

	public void cancel(View v) {
		finish();
	}

}
