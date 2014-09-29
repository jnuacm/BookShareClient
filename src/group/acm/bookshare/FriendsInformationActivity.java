package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
		FriendImg.setImageResource(R.drawable.friend_avatar_small_default);
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

		friend.getAvatar(new DownloadFileProgress());
	}

	private class DownloadFileProgress extends HttpProcessBase {
		
		public void error(String content){
			Toast.makeText(FriendsInformationActivity.this, "´íÎó:"+content,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			Toast.makeText(FriendsInformationActivity.this, "Ê§°Ü",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			Bitmap bitmap = friend.getAvatarBitmap();
			Toast.makeText(FriendsInformationActivity.this, "³É¹¦",
					Toast.LENGTH_LONG).show();
			if (bitmap == null)
				Toast.makeText(FriendsInformationActivity.this, "Îª¿Õ",
						Toast.LENGTH_LONG).show();
			FriendImg.setImageBitmap(bitmap);
		}

	}
}
