package group.acm.bookshare;

import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class GroupActivity extends Activity {
	private ListView listView;
	private User localUser;
	private MyAdapter adapter;
	float x, y, upx, upy;
	List<Map<String, Object>> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		localUser = ((LocalApp) getApplication()).getUser();
		
		data = localUser.getGroupListData();

		listView = new ListView(this);

		adapter = new MyAdapter(this);

		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener(new JudgeListener());
		listView.setLayoutAnimation(getListAnim());
		setContentView(listView);
	}

	private LayoutAnimationController getListAnim() {
		AnimationSet set = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(300);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(500);
		set.addAnimation(animation);
		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		return controller;
	}

	protected void removeListItem(View rowView, final int positon) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(
				rowView.getContext(), R.anim.item_anim);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				// animation.cancel();
				adapter.notifyDataSetChanged();
			}
		});

		rowView.startAnimation(animation);
	}

	public void showToast(String content) {
		Toast.makeText(GroupActivity.this, content, Toast.LENGTH_LONG).show();
	}

	private class JudgeListener implements OnItemLongClickListener {
		private int position;

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			this.position = position;

			new AlertDialog.Builder(GroupActivity.this)
					.setTitle("Confirm!")
					.setMessage("Are you sure to exit this group?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									localUser.deleteFriend(data
											.get(JudgeListener.this.position),
											new DeleteFriendHandler());
									removeListItem(
											listView.getChildAt(JudgeListener.this.position),
											JudgeListener.this.position);
								}

							}).setNegativeButton("No", null).show();

			return true;
		}
	}

	public void reload(String response) {
		data.clear();
		Map<String, Object> map = new HashMap<String, Object>();

		try {

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

				if (1 == is_group)
					data.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		localUser.setGroup(data);
		/*
		 * resetData(); adapter.notifyDataSetChanged();
		 */
	}

	private class DeleteFriendHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetAccess.NETMSG_BEFORE:
				break;
			case NetAccess.NETMSG_AFTER:
				if (msg.getData().getInt(NetAccess.STATUS) == NetAccess.STATUS_SUCCESS) {
					reload(msg.getData().getString(NetAccess.RESPONSE));
					GroupActivity.this.showToast("退出组群成功");
				} else
					GroupActivity.this.showToast("退出组群失败");
				break;
			case NetAccess.NETMSG_ERROR:
				GroupActivity.this.showToast(msg.getData().getString(NetAccess.ERROR));
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}

	class ViewHolder {
		public TextView groupname;
		public ImageView image;
		public boolean isVisible = true;
	}

	public class MyAdapter extends BaseAdapter {
		private Context mContext;

		private MyAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(mContext);
				convertView = inflater.inflate(R.layout.mygroup_listview_item,
						null);

				holder.image = (ImageView) convertView
						.findViewById(R.id.mygrouplistitem_groupimage);
				holder.groupname = (TextView) convertView
						.findViewById(R.id.mygrouplistitem_groupname);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.image.setBackgroundResource((Integer) data.get(position)
					.get("groupimage"));
			holder.groupname.setText((String) (data.get(position)
					.get("username")));

			return convertView;
		}
	}
}
