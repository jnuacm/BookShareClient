package group.acm.bookshare;

import group.acm.bookshare.function.NetAccess;

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
import android.widget.Toast;

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
				 Toast.makeText(LoginActivity.this, (CharSequence)
				 msg.getData().get("Response"), Toast.LENGTH_LONG).show();
			}
		};

		new ChildThread().start();

	}

	public void Login(View v) {
		String username, password;
		//username = ((TextView) findViewById(R.id.USERNAME)).getText().toString();
		//password = ((TextView) findViewById(R.id.PASSWORD)).getText().toString();

		// ////////////////////////！！！注意！！！！///////////////////////////////////////////
		// ////////////////////////以下为测试代码//////////////////////////////////////////////
		username = "gg";
		password = "1234";
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
						nvps.add(new BasicNameValuePair("username", username));
						nvps.add(new BasicNameValuePair("password", password));

						// 访问网络
						NetAccess network = NetAccess.getInstance();
						String url = LoginActivity.this
								.getString(R.string.url_host);
						url += LoginActivity.this.getString(R.string.url_login);
						Map<String, Object> map = network
								.getResponse(url, nvps);
						
						int status = (Integer)map.get("status");
						
						if (status == NetAccess.STATUS_SUCCESS) {
							// LocalApp localapp = (LocalApp) getApplication();
							// localapp.setUsername(username);

							Bundle retdata = new Bundle();
							retdata.putString("response",
									(String) map.get("response"));

							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							intent.putExtras(retdata);
							startActivity(intent);
							finish();
						} else if (status == NetAccess.STATUS_ERROR){
							Bundle retdata = new Bundle();
							retdata.putString("Response","no");
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
