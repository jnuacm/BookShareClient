package com.example.bookshare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

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
import android.widget.TextView;

public class LoginActivity extends Activity {

	@SuppressLint("NewApi")
	private Handler mMainHandler, mChildHandler;
	ChildThread test;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mMainHandler = new Handler() {
			public void handleMessage(Message msg) {
				Log.i("hello", "我是主线程，我屌爆了子线程的屎忽窿！");
			}
		};

		new ChildThread().start();

	}

	public void Login(View v) throws JSONException, ClientProtocolException,
			IOException {
		String username, password;
		username = ((TextView) findViewById(R.id.USERNAME)).getText()
				.toString();
		password = ((TextView) findViewById(R.id.PASSWORD)).getText()
				.toString();

		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("username", username);
		data.putString("password", password);
		msg.setData(data);
		msg.what = 0x1234;
		mChildHandler.sendMessage(msg);
	}

	public void Register(View v)// 登录相应按钮
	{
		Intent intent = new Intent();
		intent.setClass(this, CaptureActivity.class);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class ChildThread extends Thread {

		private static final String CHILD_TAG = "ChildThread";

		public void run() {
			this.setName("ChildThread");

			// 初始化消息循环队列，需要在Handler创建之前
			Looper.prepare();

			mChildHandler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == 0x1234) {
						Bundle data = msg.getData();
						String username = data.getString("username");
						String password = data.getString("password");

						Log.i("username", username);
						Log.i("password", password);

						HttpClient httpLogin = new DefaultHttpClient();
						HttpResponse loginResponse = null;
						HttpPost post = new HttpPost(
								"http://192.168.1.10:8080/BookShareYii/index.php?r=user/mbsignin");
						List<NameValuePair> nvps = new ArrayList<NameValuePair>();
						nvps.add(new BasicNameValuePair("LoginForm[username]",
								username));
						nvps.add(new BasicNameValuePair("LoginForm[password]",
								password));
						try {
							post.setEntity(new UrlEncodedFormEntity(nvps,
									"UTF-8"));
							loginResponse = httpLogin.execute(post);
							String strMsg = EntityUtils.toString(loginResponse
									.getEntity());
						} catch (Exception e) {
							e.printStackTrace();
						}

						/*
						 * Message loginResponseMsg = new Message(); Bundle
						 * bundle = new Bundle();
						 * bundle.putString("loginResponse", strMsg);
						 * loginResponseMsg.setData(bundle);
						 * mMainHandler.sendMessage(loginResponseMsg);
						 */

					}
				}

			};
			Log.i(CHILD_TAG, "Child handler is bound to - "
					+ mChildHandler.getLooper().getThread().getName());
			// 启动子线程消息循环队列
			Looper.loop();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		mChildHandler.getLooper().quit();
	}

}
