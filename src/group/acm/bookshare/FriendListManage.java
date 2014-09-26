package group.acm.bookshare;

import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FriendListManage {
	ListView myfriendslistview;
	SimpleAdapter friendAdapter;
	List<Map<String, Object>> friendList;
	List<Map<String, Object>> groupList;
	View addFrienView;
	EditText addFriendEdit;
	AlertDialog addFriendDialog;
	AlertDialog.Builder builder;

	MainActivity activity;

	User localUser;

	public FriendListManage(MainActivity activity) {
		this.activity = activity;
		localUser = ((LocalApp) activity.getApplication()).getUser();
	}

	public View getView() {
		return myfriendslistview;
	}

	public void initFriendList() {
		View head = LayoutInflater.from(activity).inflate(
				R.layout.myfriends_listview_top, null);

		Button refresh = (Button) head.findViewById(R.id.button_friend_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				localUser.updateFriendship(new UpdateFriendshipProgress());
			}
		});

		Button group = (Button) head.findViewById(R.id.button_group);
		group.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(activity, GroupActivity.class);
				activity.startActivity(intent);

			}
		});

		addFrienView = LayoutInflater.from(activity).inflate(
				R.layout.add_friend_alert_dialog, null);
		addFriendEdit = (EditText) addFrienView
				.findViewById(R.id.add_friend_name);
		addFriendDialog = null;
		builder = null;
		builder = new AlertDialog.Builder(activity);
		builder.setTitle("Friend");
		builder.setMessage("Please input your firend'account.");
		builder.setView(addFrienView);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String addFriendName = addFriendEdit.getText().toString();
				Log.i("will add the friend'name is", addFriendName);
				addFriendEdit.setText("");
			}

		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addFriendEdit.setText("");
			}
		});
		addFriendDialog = builder.create();
		Button add_friend = (Button) head.findViewById(R.id.button_add_friend);
		add_friend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addFriendDialog.show();
			}
		});

		friendList = localUser.getFriendListData();
		groupList = localUser.getGroupListData();

		friendAdapter = new SimpleAdapter(activity, friendList,
				R.layout.myfriends_listview_item, new String[] { "image",
						"username" }, new int[] {
						R.id.myfriendslistitem_friendimage,
						R.id.myfriendslistitem_friendname });

		View view = LayoutInflater.from(activity).inflate(
				R.layout.activity_submain_friend, null);
		myfriendslistview = (ListView) view
				.findViewById(R.id.myfirendslistview);

		setListener();

		myfriendslistview.addHeaderView(head);
		myfriendslistview.setAdapter(friendAdapter);
		myfriendslistview.setOnItemLongClickListener(new JudgeListener());

	}

	public void reload(String response) {
		this.friendList.clear();
		this.groupList.clear();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			JSONArray jsonarray = new JSONArray(response);
			for (int i = 0; i < jsonarray.length(); i++) {

				JSONObject item = jsonarray.getJSONObject(i);
				String name = item.getString("username");
				String email = item.getString("email");
				String area = item.getString("area");
				int is_group = item.getInt("is_group");

				map = new HashMap<String, Object>();
				map.put("username", name);
				map.put("email", email);
				map.put("area", area);
				map.put("image", R.drawable.friend1);
				map.put("is_group", is_group);

				if (0 == is_group)// 朋友关系
					this.friendList.add(map);
				else
					// 组属关系
					this.groupList.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		localUser.setFriend(friendList);
		localUser.setGroup(groupList);
		friendAdapter.notifyDataSetChanged();
	}

	private class DeleteFriendProgress extends HttpProcessBase {
		
		public void error(String content){
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "删除好友失败";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			reload(response);
			// friendmanage.friendAdapter.notifyDataSetChanged();
			String content = "删除好友成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}
	}

	private class UpdateFriendshipProgress extends HttpProcessBase {
		public void error(String content) {
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "更新好友失败";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			Log.i("update_resposnse:", response);
			reload(response);

			String content = "更新好友成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}
	}

	private class JudgeListener implements OnItemLongClickListener {
		private int position;

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			this.position = position;
			if (0 == position)
				return false;

			new AlertDialog.Builder(activity)
					.setTitle("Confirm!")
					.setMessage("Are you sure to delete this friend?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									localUser.deleteFriend(
											friendList
													.get(JudgeListener.this.position - 1),
											new DeleteFriendProgress());
								}

							}).setNegativeButton("No", null).show();

			return true;
		}
	}

	private void setListener() {
		myfriendslistview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position) {
				} else {
					Intent intent = new Intent(activity,
							FriendsInformationActivity.class);

					Bundle bundle = new Bundle();
					bundle.putString("name",
							friendList.get(position - 1).get("username")
									.toString());
					bundle.putString("area",
							friendList.get(position - 1).get("area").toString());
					bundle.putString("email",
							friendList.get(position - 1).get("email")
									.toString());
					bundle.putString("image",
							friendList.get(position - 1).get("image")
									.toString());

					intent.putExtra("key", bundle);
					activity.startActivity(intent);
				}
			}
		});

	}

	public void updateDisplay() {
		friendAdapter.notifyDataSetChanged();
	}

}
