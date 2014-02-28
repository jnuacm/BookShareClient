package com.example.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


public class FriendsInformationActivity extends Activity {
	
	private ImageView FriendImg;
	private TextView FriendName;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friends_information);
		FriendName = (TextView)findViewById(R.id.IF_username);
		FriendImg = (ImageView)findViewById(R.id.IF_img);
		/*读取数据*/
		Intent intent =getIntent();// 收取 email 
		Bundle bundle =intent.getBundleExtra("key");// 打开 email 
		
		FriendImg.setImageResource(Integer.parseInt(bundle.getString("image")));
		FriendName.setText(bundle.getString("friendName"));
		bundle.getString("image");
		

		
	}

	
}
