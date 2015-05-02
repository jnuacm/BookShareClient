package group.acm.bookshare;

import org.apache.http.util.TextUtils;

import group.acm.bookshare.function.http.UrlStringFactory;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������

		setContentView(R.layout.activity_welcome);
		// new Handler().postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// Intent intent = new Intent(WelcomeActivity.this,
		// LoginActivity.class);
		// startActivity(intent);
		// WelcomeActivity.this.finish();
		// }
		// }, 2000);

	}

	public void Enter(View v) {
		EditText input = (EditText) findViewById(R.id.input_host);
		String url = input.getText().toString();
		if (!TextUtils.isEmpty(url)) {
			UrlStringFactory.URL_HOST = url;
		}
		Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
		startActivity(intent);
		WelcomeActivity.this.finish();
	}

}
