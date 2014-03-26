package com.example.bookshare;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private TextView username,password;
	
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		username = (TextView) findViewById(R.id.USERNAME);
		password = (TextView) findViewById(R.id.PASSWORD);
		
	}

	public void Login(View v)// 登录相应按钮
	{
		
		
		
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
	
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

}
