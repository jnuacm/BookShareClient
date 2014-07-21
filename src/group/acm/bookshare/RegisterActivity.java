package group.acm.bookshare;

import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements Update {
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

	public void confirm(View v) {
		// 获取注册的内容
		String username, password, email, area;

		EditText edittext = (EditText) findViewById(R.id.registerusername);
		username = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registerpassword);
		password = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registeremail);
		email = edittext.getText().toString();

		edittext = (EditText) findViewById(R.id.registerarea);
		area = "";
		area = edittext.getText().toString();

		username = "google";
		password = "123456";
		email = "abc@qd.com";
		area = "jnu";

		if (username == "" || password == "" || email == "")
			return;

		Log.i("username", username);
		Log.i("password", password);
		Log.i("email", email);
		Log.i("area", area);

		// 访问网络
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("area", area));

		String url = RegisterActivity.this.getString(R.string.url_host);
		url += RegisterActivity.this.getString(R.string.url_register);
		NetAccess network = NetAccess.getInstance();

		List<Update> updates = new ArrayList<Update>();
		updates.add(this);
		network.getPostThread(url, nvps, updates).start();
	}

	public void cancel(View v) {
		finish();
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

		if (NetAccess.STATUS_SUCCESS == ((Integer) map.get("status"))) {
			Toast.makeText(RegisterActivity.this, "yes!success!",
					Toast.LENGTH_LONG).show();
		} else if (NetAccess.STATUS_ERROR == ((Integer) map.get("status"))) {
			Toast.makeText(RegisterActivity.this, "no!error!",
					Toast.LENGTH_LONG).show();
		}
	}

}
