package com.example.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.entity.LocalAccount;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.socket.AQuery;
import com.socket.Ackownledge;
import com.socket.RBorrow;
import com.socket.RQuery;
import com.socket.RReturn;
import com.socket.RShare;

public class HomeActivity extends Activity {

	private String isbn;
	private TextView editbookname, editauthor, editpublisher;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		editbookname = (TextView) findViewById(R.id.editbookname);
		editauthor = (TextView) findViewById(R.id.editauthor);
		editpublisher = (TextView) findViewById(R.id.editpublisher);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	public void Saomiao(View v) {
		type = 1;
		IntentIntegrator.initiateScan(HomeActivity.this);

	}

	public void Search(View v) {
		String Str_bookN = editbookname.getText().toString();
		String Str_editauthor = editauthor.getText().toString();
		String Str_editpublisher = editpublisher.getText().toString();

		RQuery RQ = new RQuery(Str_editauthor, Str_bookN, Str_editpublisher);
		RQ.send();

		LocalAccount localact = (LocalAccount)getApplicationContext();
		localact.setBooks(AQuery.receiveBooklist());

		Intent intent = new Intent();
		intent.setClass(this, BrowseActivity.class);
		startActivity(intent);
	}

	public void Borrow(View v) {
		type = 2;
		IntentIntegrator.initiateScan(HomeActivity.this);
	}

	public void Return(View v) {
		type = 3;
		IntentIntegrator.initiateScan(HomeActivity.this);
	}
	
	public void Share(View v){
		type = 4;
		IntentIntegrator.initiateScan(HomeActivity.this);
	}

	@Override
	/*以下是调用扫描仪,这里在前后台拼接的时候改过了*/
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, data);
		if (scanResult != null) 
		{
			isbn = scanResult.getContents();
			if(1 == type)//saomiao
			{
				Intent intent = new Intent();
				intent.setClass(this, BodyActivity.class);
				intent.putExtra("ISBN", isbn);
				startActivity(intent);
			}
			else if(2 == type)//borrow
			{
				
				String userName,baseName;
				LocalAccount localact = (LocalAccount)getApplicationContext();
				userName = localact.getAccount().getAccount();
				baseName = localact.getAccount().getLibrary();
				RBorrow RB = new RBorrow(isbn,userName,baseName);
				RB.send();
				boolean is_legal = Ackownledge.receive().getStatus();
				if(true == is_legal)
					Toast.makeText(this,"借书成功!" ,Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this,"借书失败!" ,Toast.LENGTH_LONG).show();
			}
			else if(3 == type)//return
			{
				String userName,baseName;
				LocalAccount localact = (LocalAccount)getApplicationContext();
				userName = localact.getAccount().getAccount();
				baseName = localact.getAccount().getLibrary();
				RReturn RR = new RReturn(isbn,userName,baseName);
				RR.send();
				boolean is_legal = Ackownledge.receive().getStatus();
				if(true == is_legal)
					Toast.makeText(this,"还书成功!" ,Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this,"还书失败!" ,Toast.LENGTH_LONG).show();
			}
			else if (4 == type)//Share
			{
				String userName,baseName;
				LocalAccount localact = (LocalAccount)getApplicationContext();
				userName = localact.getAccount().getAccount();
				baseName = localact.getAccount().getLibrary();
				RShare RS = new RShare(baseName,userName,isbn);
				RS.send();
				boolean is_legal = Ackownledge.receive().getStatus();
				if(true == is_legal)
					Toast.makeText(this,"分享成功!" ,Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this,"分享失败!" ,Toast.LENGTH_LONG).show();
			}
			else
				Toast.makeText(this,"错误!" ,Toast.LENGTH_LONG).show();
		}
	}
}
