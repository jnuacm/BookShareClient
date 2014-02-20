package com.example.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class RegisterActivity extends Activity {

	private Spinner m_Spinner;
	private ArrayAdapter<String> adapter;
	private TextView username,password,aginpassword,realname;
	private int now_flag;//当前下拉框的下标
	private RelativeLayout registerLayout;
	private String[] m_basename = { "", "", "", "", "" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		username = (TextView) findViewById(R.id.NewUsername);
		password = (TextView) findViewById(R.id.NewPassword);
		aginpassword = (TextView) findViewById(R.id.AginNewPassword);
		realname = (TextView)findViewById(R.id.realname);
		registerLayout = (RelativeLayout) findViewById(R.id.registerLayout);
		//registerLayout.setBackgroundColor(Color.argb(150, 22, 70, 150));

		int baseNum = this.getIntent().getIntExtra("BaseNum", -1);

		for (int i = 0; i < baseNum; i++) {
			m_basename[i] = this.getIntent().getStringExtra(String.valueOf(i));
		}

		m_Spinner = (Spinner) findViewById(R.id.NewUserBaseName);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	
	public void Enter(View v)//注册确认
	{
	
	}
	
	public void Back(View v)//注册确认
	{
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);////退出当前activity
		startActivity(intent);
		finish();
	}

}
