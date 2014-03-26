package com.example.bookshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.Toast;

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
				Log.i("hello","�������̣߳��Ҍű������̵߳�ʺ������");
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
		
		mChildHandler.sendEmptyMessage(100);
	}

	public void Register(View v)// ��¼��Ӧ��ť
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

	        //��ʼ����Ϣѭ�����У���Ҫ��Handler����֮ǰ
	        Looper.prepare();

	        mChildHandler = new Handler() {
	        	public void handleMessage(Message msg){
	        		Log.i("hello","�������̣߳��ұ����߳̌ű���ʺ������");
	        		mMainHandler.sendEmptyMessage(100);
	        		}
	        	};
	        	Log.i(CHILD_TAG, "Child handler is bound to - "+ mChildHandler.getLooper().getThread().getName());
	        	//�������߳���Ϣѭ������
	        	Looper.loop();
	    }
	}

	public void onDestroy() {
		super.onDestroy();
		mChildHandler.getLooper().quit();
		}

}

