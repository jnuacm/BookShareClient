package com.example.bookshare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.example.bookshare.util.NetAccess;

public class LoginActivity extends Activity {

	@SuppressLint("NewApi")
	private Handler mMainHandler, mChildHandler;
	private static int MSG_LOGIN = 0x1234;
	private static int MSG_RESPONSE = 0x1235;

	ChildThread test;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mMainHandler = new Handler() {
			public void handleMessage(Message msg) {
				/*
				 * Toast.makeText(LoginActivity.this, (CharSequence)
				 * msg.getData().get("Response"), Toast.LENGTH_LONG).show();
				 */
			}
		};

		new ChildThread().start();

	}

	public void Login(View v) {
		String username, password;
		username = ((TextView) findViewById(R.id.USERNAME)).getText()
				.toString();
		password = ((TextView) findViewById(R.id.PASSWORD)).getText()
				.toString();

		// ////////////////////////！！！注意！！！！///////////////////////////////////////////
		// ////////////////////////以下为测试代码//////////////////////////////////////////////
		username = "Luoluo";
		password = "123456";
		// ////////////////////////以上为测试代码//////////////////////////////////////////////
		Message msg = new Message();
		Bundle data = new Bundle();
		data.putString("username", username);
		data.putString("password", password);
		msg.setData(data);
		msg.what = MSG_LOGIN;
		mChildHandler.sendMessage(msg);
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

	class ChildThread extends Thread {

		private static final String CHILD_TAG = "ChildThread";

		@SuppressLint("HandlerLeak")
		public void run() {
			this.setName("ChildThread");

			// 初始化消息循环队列，需要在Handler创建之前
			Looper.prepare();

			mChildHandler = new Handler() {
				public void handleMessage(Message msg) {
					if (msg.what == MSG_LOGIN) {
						Bundle data = msg.getData();
						String username = data.getString("username");
						String password = data.getString("password");

						List<NameValuePair> nvps = new ArrayList<NameValuePair>();
						nvps.add(new BasicNameValuePair("LoginForm[username]",
								username));
						nvps.add(new BasicNameValuePair("LoginForm[password]",
								password));

						// 访问网络
						NetAccess network = NetAccess.getInstance();
						String strMsg = network
								.getResponse(
										"http://192.168.1.10:8080/BookShareYii/index.php?r=user/mbsignin",
										nvps);

						if (strMsg.matches("yes")) {
							LocalApp localapp = (LocalApp) getApplication();
							localapp.setUsername(username);
							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							startActivity(intent);
							finish();
						} else {
							Bundle retdata = new Bundle();
							retdata.putString("Response", strMsg);
							msg.setData(retdata);
							msg.what = MSG_RESPONSE;
							mMainHandler.sendMessage(msg);
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
