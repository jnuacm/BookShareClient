package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.ImageManage;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsInformationActivity extends Activity {
	private User friend;
	private User localUser;

	private ImageView FriendImg;
	private TextView FriendName;
	private TextView FriendArea;
	private TextView FriendEmail;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_information);

		localUser = ((LocalApp) getApplication()).getUser();
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("friend_info");
		Map<String, Object> info = localUser.getFriendByName(bundle
				.getString(Friend.NAME));
		friend = new User(info, getApplication());

		FriendName = (TextView) findViewById(R.id.IF_username);
		FriendArea = (TextView) findViewById(R.id.IF_area);
		FriendImg = (ImageView) findViewById(R.id.IF_img);
		FriendEmail = (TextView) findViewById(R.id.IF_email);

		FriendName.setText(friend.getUsername());
		FriendArea.setText(friend.getArea());
		setAvatar();
		FriendEmail.setText(friend.getEmail());

		Button button = (Button) findViewById(R.id.friend_checkbook_button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Utils.isQuickClick())
					return;
				friend.getBookList(new HttpProcessBase() {

					@Override
					public void statusError(String response) {
					}

					@Override
					public void statusSuccess(String response) {
						Log.i(Utils.getLineInfo(), "��ȡ�ɹ�");
						Bundle bookData = new Bundle();
						bookData.putString(Friend.NAME, friend.getUsername());
						bookData.putString(NetAccess.RESPONSE, response);
						Intent intent = new Intent(
								FriendsInformationActivity.this,
								FriendBooksActivity.class);
						intent.putExtras(bookData);
						startActivity(intent);
					}
				});
			}

		});
	}

	private void setAvatar() {
		if (friend.getAvatarVersion() == ImageManage.AVATAR_VERSION_NONE) {
			if (Friend.GROUP == friend.getIs_group()) {
				FriendImg
						.setImageResource(R.drawable.default_group_avatar_small);
			} else {
				FriendImg
						.setImageResource(R.drawable.default_friend_avatar_small);
			}
		} else {
			FriendImg.setImageBitmap(localUser.getAvatarBitmap(friend
					.getUsername()));
		}
	}
}
