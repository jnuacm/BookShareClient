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
	private WebView web;
	public  String[] m_basename = { "ACM", "数模", "机器人" };
	private int now_flag;//当前下拉框的下标
	private Spinner m_Spinner;
	private ArrayAdapter<String> adapter;
	private RelativeLayout mainLayout;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		/*if(Build.VERSION.SDK_INT >= 11) {
		      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads     ().detectDiskWrites().detectNetwork().penaltyLog().build());
		   StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		  }*/
		
		username = (TextView) findViewById(R.id.USERNAME);
		password = (TextView) findViewById(R.id.PASSWORD);
		
	 
		mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
		//mainLayout.setBackgroundColor(Color.argb(150, 22, 70, 150));
		

		
		m_Spinner = (Spinner) findViewById(R.id.BASENAME);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, m_basename);// 连接可选内容

		m_Spinner.setAdapter(adapter);// 设置下拉框
		m_Spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2,long arg3)
			{
				now_flag = arg2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
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
