package com.example.bookshare;

import com.entity.LocalAccount;
import com.socket.Ackownledge;
import com.socket.RSignIn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView username,password;
	public  String[] m_basename = { "ACM", "��ģ", "������" };
	private int now_flag;//��ǰ��������±�
	private Spinner m_Spinner;
	private ArrayAdapter<String> adapter;
	private RelativeLayout mainLayout;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (Build.VERSION.SDK_INT >= 11) {
		      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads     ().detectDiskWrites().detectNetwork().penaltyLog().build());
		   StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		  }
		
		username = (TextView) findViewById(R.id.USERNAME);
		password = (TextView) findViewById(R.id.PASSWORD);
		
		mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
		//mainLayout.setBackgroundColor(Color.argb(150, 22, 70, 150));
		

		LocalAccount localact = (LocalAccount)getApplicationContext();
		localact.Initia();
		
		m_Spinner = (Spinner) findViewById(R.id.BASENAME);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, m_basename);// ���ӿ�ѡ����

		m_Spinner.setAdapter(adapter);// ����������
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

	public void Login(View v)// ��¼��Ӧ��ť
	{
		String Str_name,Str_passw,Str_basen;

		Str_name = username.getText().toString();
		Str_passw = password.getText().toString();
		Str_basen = m_basename[now_flag];
		RSignIn RS = new RSignIn(Str_basen,Str_name,Str_passw);
		/*RS.send();
		boolean is_legal = Ackownledge.receive().getStatus();
		
		if(true == is_legal)
		{*/
			LocalAccount localact = (LocalAccount)getApplicationContext();
			localact.setAccount(RS.getAccount());
			Intent intent = new Intent();
			intent.setClass(this, CaptureActivity.class);
			startActivity(intent);
		/*}
		else
		{
			Toast.makeText(MainActivity.this,"�������򲻴����������˻�" ,Toast.LENGTH_LONG).show();
		}*/
		/*
		Intent intent = new Intent();
		intent.setClass(this, HomeActivity.class);
		startActivity(intent);*/
	}

	public void Register(View v)// ��¼��Ӧ��ť
	{
		Intent intent = new Intent();
		intent.putExtra("BaseNum", m_basename.length);
		for (int i = 0; i < m_basename.length; i++) {
			intent.putExtra(String.valueOf(i), m_basename[i]);
		}
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
