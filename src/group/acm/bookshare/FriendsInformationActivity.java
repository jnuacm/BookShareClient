package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.ImageManage;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.util.Utils;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private TextView FriendCollection;
	private TextView FriendName;
	private TextView FriendArea;
	private TextView FriendEmail;
	private Button button;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_information);

		localUser = ((LocalApp) getApplication()).getUser();
		Intent intent = getIntent();
		Map<String, Object> info = localUser.getFriendByName(intent
				.getStringExtra(Friend.NAME));
		friend = new User(info, getApplication());
		localUser.setFriend(friend);

		FriendName = (TextView) findViewById(R.id.IF_username);
		FriendArea = (TextView) findViewById(R.id.IF_area);
		FriendImg = (ImageView) findViewById(R.id.IF_img);
		FriendEmail = (TextView) findViewById(R.id.IF_email);
		FriendCollection = (TextView) findViewById(R.id.IF_collection);
		button = (Button) findViewById(R.id.friend_checkbook_button);

		setInformation();
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utils.isQuickClick())
					return;
				friend.getBookList(new BooksLoadProgress());
			}
		});
	}
	
	private void setInformation(){
		FriendName.setText(friend.getUsername());
		FriendArea.setText(friend.getArea());
		setAvatar();
		FriendEmail.setText(friend.getEmail());
		FriendCollection.setText(Integer.toString(friend.getPersonBookNum()));
	}

	private class BooksLoadProgress extends HttpProcessBase {

		public void error(String content) {
			Toast.makeText(FriendsInformationActivity.this, content,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "Ê§°Ü:" + response;
			Toast.makeText(FriendsInformationActivity.this, content,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			friend.clearBookData();
			JSONObject jsonobj;
			try {
				jsonobj = new JSONObject(response);
				JSONArray jsonarray = jsonobj.getJSONArray("own_book");
				friend.addBookDataToList(jsonarray);
			} catch (JSONException e) {
				Toast.makeText(FriendsInformationActivity.this, e.toString(),
						Toast.LENGTH_LONG).show();
				return;
			}

			Intent intent = new Intent(FriendsInformationActivity.this,
					FriendBooksActivity.class);
			startActivity(intent);
		}
	}

	private void setAvatar() {
		Bitmap avatar = friend.getAvatarBitmap();
		if (friend.getAvatarVersion() == ImageManage.AVATAR_VERSION_NONE
				|| avatar == null) {
			if (Friend.GROUP == friend.getIs_group()) {
				FriendImg
						.setImageResource(R.drawable.default_group_avatar_small);
			} else {
				FriendImg
						.setImageResource(R.drawable.default_friend_avatar_small);
			}
		} else {
			FriendImg.setImageBitmap(avatar);
		}
	}
}
