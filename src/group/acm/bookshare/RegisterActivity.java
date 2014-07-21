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
import android.widget.EditText;
import android.widget.Toast;

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
						Toast.makeText(RegisterActivity.this, "yes!success!",
								Toast.LENGTH_LONG).show();
						/*Intent intent = new Intent();
						intent.setClass(RegisterActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();*/
					} else {
						Toast.makeText(RegisterActivity.this, "no!fail!",
								Toast.LENGTH_LONG).show();

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
							nvps.add(new BasicNameValuePair("username",
									username));
							nvps.add(new BasicNameValuePair("password",
									password));
							nvps.add(new BasicNameValuePair("email", email));
							nvps.add(new BasicNameValuePair("area", area));

							String url = RegisterActivity.this
									.getString(R.string.url_host);
							url += RegisterActivity.this
									.getString(R.string.url_register);
							NetAccess network = NetAccess.getInstance();
							Map<String, Object> map = network.getResponse(url,
									nvps);
							String response;
							if (NetAccess.STATUS_SUCCESS == ((Integer) map
									.get("status"))) {
								response = "yes";
								Log.i("RegisterActivity",
										"Handler.handleMessage():yes");
							} else {
								response = "no";
								Log.i("RegisterActivity",
										"Handler.handleMessage():no");
							}
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
		intent.setClass(this, CaptureActivity.class);
		startActivity(intent);
		/*
		 * Intent intent = new Intent(); intent.setClass(this,
		 * LoginActivity.class);
		 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //�˳���ǰactivity
		 * startActivity(intent);
		 */
	}

}
