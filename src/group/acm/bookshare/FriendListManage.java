package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.HttpProgress;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListManage {
	MainActivity activity;
	User localUser;

	ListView myfriendslistview;
	FriendListAdapter friendAdapter;
	List<Map<String, Object>> friendList;

	public FriendListManage(MainActivity activity) {
		this.activity = activity;
		localUser = ((LocalApp) activity.getApplication()).getUser();
	}

	public View getView() {
		return myfriendslistview;
	}

	public void initFriendList() {
		friendList = localUser.getFriendListData();

		friendAdapter = new FriendListAdapter(activity, friendList);

		View view = LayoutInflater.from(activity).inflate(
				R.layout.activity_submain_friend, null);
		myfriendslistview = (ListView) view
				.findViewById(R.id.myfirendslistview);

		myfriendslistview.setAdapter(friendAdapter);

		setItemListener();
	}

	private class FriendListAdapter extends BaseAdapter {
		private Context context;
		private List<Map<String, Object>> datas;

		public FriendListAdapter(Context context, List<Map<String, Object>> data) {
			this.datas = data;
			this.context = context;
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView nameView;
			ImageView avatarView;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.myfriends_listview_item, null);
			}
			avatarView = (ImageView) convertView
					.findViewById(R.id.myfriendslistitem_friendimage);
			nameView = (TextView) convertView
					.findViewById(R.id.myfriendslistitem_friendname);

			Map<String, Object> item = datas.get(position);

			avatarView.setImageResource(R.drawable.friend_avatar_small_default);
			nameView.setText((String) item.get(Friend.NAME));
			setDivider(position, convertView, item); // 设置是否显示分界

			return convertView;
		}

		private void setDivider(int position, View convertView,
				Map<String, Object> item) {
			TextView dividerView = (TextView) convertView
					.findViewById(R.id.myfriendslistviewitem_divider);
			if (0 == position) {
				dividerView.setVisibility(View.VISIBLE);
				if (((Integer) item.get(Friend.IS_GROUP)) == Friend.GROUP)
					dividerView.setText("Groups");
				else
					dividerView.setText("Friends");
			} else if (position > 0
					&& ((Integer) item.get(Friend.IS_GROUP)) == Friend.GROUP
					&& ((Integer) datas.get(position - 1).get(Friend.IS_GROUP)) != Friend.GROUP) {
				dividerView.setVisibility(View.VISIBLE);
				dividerView.setText("Groups");
			} else {
				dividerView.setVisibility(View.GONE);
			}
		}
	}
	
	private void setItemListener() {
		myfriendslistview
				.setOnItemClickListener(new FriendsItemClickListener());
		myfriendslistview
				.setOnItemLongClickListener(new FriendsItemLongClickListener());
	}

	public void addFriend() {
		View addFrienView = LayoutInflater.from(activity).inflate(
				R.layout.add_friend_alert_dialog, null);
		EditText addFriendEdit = (EditText) addFrienView
				.findViewById(R.id.add_friend_name);
		AlertDialog addFriendDialog = null;
		AlertDialog.Builder builder = null;

		builder = new AlertDialog.Builder(activity);
		builder.setTitle("Friend");
		builder.setMessage("Please input your firend'account.");
		builder.setView(addFrienView);
		builder.setPositiveButton("Yes", new AddFriendConfirmDialogListener(
				addFriendEdit));
		builder.setNegativeButton("No", null);
		addFriendDialog = builder.create();
		addFriendDialog.show();
	}

	private class AddFriendConfirmDialogListener implements
			DialogInterface.OnClickListener {
		EditText addFriendEdit;

		public AddFriendConfirmDialogListener(EditText addFriendEdit) {
			this.addFriendEdit = addFriendEdit;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			String addFriendName = addFriendEdit.getText().toString();
			localUser.addFriend(addFriendName, "我想加你",
					HttpProgress.createShowProgress(activity, "发送成功", "发送失败"));
		}

	}

	private class DeleteFriendProgress extends HttpProcessBase {

		public void error(String content) {
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "删除好友失败";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			String content = "删除好友成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
			reload(response);
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
			reload(response);
			String content = "更新好友成功";
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}
	}

	private class FriendsItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(activity,
					FriendsInformationActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString(Friend.NAME,
					friendList.get(position).get(Friend.NAME).toString());

			intent.putExtra("friend_info", bundle);
			activity.startActivity(intent);
		}
	}

	private class FriendsItemLongClickListener implements
			OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			builder.setTitle("Confirm!");
			builder.setMessage("Are you sure to delete this friend?");
			builder.setPositiveButton("Yes", new DeleteFriendConfirmListener(
					position));
			builder.setNegativeButton("No", null).show();

			return true;
		}
	}

	private class DeleteFriendConfirmListener implements
			DialogInterface.OnClickListener {
		private int position;

		public DeleteFriendConfirmListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			localUser.deleteFriend(friendList.get(position),
					new DeleteFriendProgress());
		}

	}

	public void updateDisplay() {
		friendAdapter.notifyDataSetChanged();
	}

	public void reload() {
		localUser.getFriendList(new UpdateFriendshipProgress());
	}

	public void reload(String response) {
		localUser.clearFriendData();
		localUser.addFriendDataToList(response);
		updateDisplay();
	}
}
