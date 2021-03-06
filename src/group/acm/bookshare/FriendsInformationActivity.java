package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.ImageManage;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess.NetThread;
import group.acm.bookshare.util.Utils;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsInformationActivity extends Activity {
	private User friend;
	private User localUser;

	private ImageView backButton;
	private ImageView FriendImg;
	private TextView FriendCollection;
	private TextView FriendName;
	private TextView FriendArea;
	private TextView FriendEmail;
	private Button checkBooks;
	private Button checkFriends;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_information);

		localUser = ((LocalApp) getApplication()).getUser();
		Intent intent = getIntent();
		Map<String, Object> info = localUser.getFriendByName(intent
				.getStringExtra(Friend.NAME));
		friend = new User(info, getApplication());
		localUser.setFriend(friend);

		backButton = (ImageView) findViewById(R.id.friend_info_bar_img);
		FriendName = (TextView) findViewById(R.id.IF_username);
		FriendArea = (TextView) findViewById(R.id.IF_area);
		FriendImg = (ImageView) findViewById(R.id.IF_img);
		FriendEmail = (TextView) findViewById(R.id.IF_email);
		FriendCollection = (TextView) findViewById(R.id.IF_collection);
		checkBooks = (Button) findViewById(R.id.friend_checkbook_button);
		checkFriends = (Button) findViewById(R.id.group_checkfriend_button);

		setInformation();
		setButtons();
	}

	private void setInformation() {
		FriendName.setText(friend.getUsername());
		FriendArea.setText(friend.getArea());
		setAvatar();
		FriendEmail.setText(friend.getEmail());
		FriendCollection.setText(Integer.toString(friend.getPersonBookNum()));
	}

	private NetThread getBookThread;
	private void setButtons() {
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		checkBooks.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (getBookThread != null && !getBookThread.isCanceled())
					return;
				getBookThread = friend.getBookList(new BooksLoadProgress());
			}
		});
		if (friend.getIs_group() == Friend.GROUP) {
			checkFriends.setVisibility(View.VISIBLE);
			checkFriends.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					friend.getFriendList(new UpdateFriendsProgress());
				}
			});
		} else
			checkFriends.setVisibility(View.INVISIBLE);
	}

	private class UpdateFriendsProgress extends HttpProcessBase {
		public void error(String content) {
			Toast.makeText(FriendsInformationActivity.this, content,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			Toast.makeText(FriendsInformationActivity.this, response,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			friend.clearFriendData();
			try {
				friend.addFriendDataToList(new JSONArray(response));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			friend.deleteFriendData(localUser.getUsername());
			Intent intent = new Intent();
			intent.setClass(FriendsInformationActivity.this,
					GroupMemberActivity.class);
			startActivity(intent);
		}
	}

	private class BooksLoadProgress extends HttpProcessBase {

		public void error(String content) {
			Toast.makeText(FriendsInformationActivity.this, content,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "ʧ��:" + response;
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
		if (avatar == null)
			Log.i(Utils.getLineInfo(), "null avatar");
		Log.i(Utils.getLineInfo(),
				"avatar version" + Integer.toString(friend.getAvatarVersion()));
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
