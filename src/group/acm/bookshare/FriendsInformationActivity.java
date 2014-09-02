package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsInformationActivity extends Activity {

	private ImageView FriendImg;
	private TextView FriendName;

	private TextView FriendArea;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_friends_information);

		FriendName = (TextView) findViewById(R.id.IF_username);
		FriendArea = (TextView) findViewById(R.id.IF_area);
		FriendImg = (ImageView) findViewById(R.id.IF_img);
		/* 读取数据 */
		Intent intent = getIntent();// 收取 email
		Bundle bundle = intent.getBundleExtra("key");// 打开 email

		FriendImg.setImageResource(Integer.parseInt(bundle.getString("image")));
		FriendName.setText(bundle.getString("name"));
		FriendArea.setText(bundle.getString("area"));
		bundle.getString("image");

		Button button = (Button) findViewById(R.id.friend_checkbook_button);
		button.setOnClickListener(new OnClickListener() {
			private String friendName;

			@Override
			public void onClick(View v) {
				friendName = getIntent().getBundleExtra("key")
						.getString("name");
				((LocalApp) getApplication()).getUser().getBookList(friendName,
						new Handler() {
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case NetAccess.NETMSG_AFTER:
									if (msg.getData().getInt(NetAccess.STATUS) == NetAccess.STATUS_SUCCESS) {
										Bundle bookData = new Bundle();
										bookData.putString("friendname",
												friendName);
										bookData.putString(
												NetAccess.RESPONSE,
												msg.getData().getString(
														NetAccess.RESPONSE));
										Intent intent = new Intent(
												FriendsInformationActivity.this,
												FriendBooksActivity.class);
										intent.putExtras(bookData);
										startActivity(intent);
									} else {

									}
									break;
								case NetAccess.NETMSG_ERROR:
									break;
								}
							}
						});
			}

		});
	}

}
