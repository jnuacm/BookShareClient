package com.example.bookshare;

import java.util.ArrayList;
import java.util.List;

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

import com.example.bookshare.util.NetAccess;

public class RegisterActivity extends Activity {

	private Handler mMainHandler, mChildHandler;
	private static int MSG_REGISTER = 0x1234;
	private static int MSG_RESPONSE = 0x1235;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mMainHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == MSG_RESPONSE) {
					String tmp = (String) msg.getData().get("response");
					if (tmp.matches("yes")) {
						/*
						 * ע��ɹ�����Ҫ��¼���û��ʺ����룬������Ҫ�ڴ˴����������̼߳�����ҳ����
						 */
						Intent intent = new Intent();
						intent.setClass(RegisterActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();
					} else if (tmp.matches("username")) {
						// �ʺ��Ѵ���

					} else if (tmp.matches("email")) {
						// email�Ѵ���
					}
				}
			}
		};

		new Thread() { // ���̸߳���ͨ����������ע��

			@SuppressLint("HandlerLeak")
			public void run() {
				// ��ʼ����Ϣѭ�����У���Ҫ��Handler����֮ǰ
				Looper.prepare();

				mChildHandler = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == MSG_REGISTER) {
							Bundle data = msg.getData();
							String username = data.getString("username");
							String password = data.getString("password");
							String email = data.getString("email");
							String area = data.getString("area");

							// ��������
							List<NameValuePair> nvps = new ArrayList<NameValuePair>();
							nvps.add(new BasicNameValuePair("User[username]",
									username));
							nvps.add(new BasicNameValuePair("User[password]",
									password));
							nvps.add(new BasicNameValuePair("User[email]",
									email));
							if (area != "")
								nvps.add(new BasicNameValuePair("User[area]",
										area));

							NetAccess network = NetAccess.getInstance();
							String response = network
									.getResponse(
											"http://192.168.1.10:8080/BookShareYii/index.php?r=user/mbsignup",
											nvps);

							// ���ؽ���ش�
							data = new Bundle();
							data.putString("response", response);
							msg = new Message();
							msg.setData(data);
							msg.what = MSG_RESPONSE;

							mMainHandler.sendMessage(msg);

						}
					}

				};
				// �������߳���Ϣѭ������
				Looper.loop();

			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	public void confirm(View v) // ȷ��
	{
		// ��ȡע�������
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

		if (username == "" || password == "" || email == "")
			return;

		Log.i("username", username);
		Log.i("password", password);
		Log.i("email", email);
		Log.i("area", area);

		// ��Ϣ���ݸ����̷߳�������
		Bundle bundle = new Bundle();
		bundle.putString("username", username);
		bundle.putString("password", password);
		bundle.putString("email", email);
		bundle.putString("area", area);
		Message msg = new Message();
		msg.setData(bundle);
		msg.what = MSG_REGISTER;

		mChildHandler.sendMessage(msg);
	}

	public void cancel(View v) // ȡ��
	{
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //�˳���ǰactivity
		startActivity(intent);
		finish();
	}

}
