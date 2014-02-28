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
		/*��ȡ����*/
		Intent intent =getIntent();// ��ȡ email 
		Bundle bundle =intent.getBundleExtra("key");// �� email 
		
		FriendImg.setImageResource(Integer.parseInt(bundle.getString("image")));
		FriendName.setText(bundle.getString("friendName"));
		bundle.getString("image");
		

		
	}

	
}
