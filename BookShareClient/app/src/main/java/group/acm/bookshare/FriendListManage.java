package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.HttpProgress;
import group.acm.bookshare.function.http.NetAccess.NetThread;

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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListManage {
    Activity activity;
    User curUser;

    ListView myfriendslistview;
    PageListAdapter friendAdapter;

    public FriendListManage(Activity activity, User user) {
        this.activity = activity;
        curUser = user;
    }

    public View getView() {
        return myfriendslistview;
    }

    public void initFriendList() {
        friendAdapter = new FriendListAdapter(activity, curUser.getFriendListData(),
                curUser.getAvatars());

        View view = LayoutInflater.from(activity).inflate(
                R.layout.activity_submain_friend, null);
        myfriendslistview = (ListView) view
                .findViewById(R.id.myfirendslistview);
        myfriendslistview.setVerticalFadingEdgeEnabled(false);

        myfriendslistview.setAdapter(friendAdapter);
        myfriendslistview.setOnScrollListener(friendAdapter);

        setItemListener();
    }

    protected class FriendListAdapter extends PageListAdapter {
        private Context context;
        private List<Map<String, Object>> datas;
        private Map<String, Bitmap> avatarMap;

        private int friendsSize;
        private int groupSize;

        public FriendListAdapter(Context context,
                                 List<Map<String, Object>> data, Map<String, Bitmap> avatarMap) {
            this.datas = data;
            this.context = context;
            this.avatarMap = avatarMap;
        }

        @Override
        public int getCount() {
            friendsSize = 0;
            groupSize = 0;
            for (Map<String, Object> data : datas) {
                if ((Integer) data.get(Friend.IS_GROUP) != Friend.GROUP)
                    friendsSize++;
                else
                    groupSize++;
            }
            int allViewSize;
            if (friendsSize == 0 && groupSize == 0)
                allViewSize = 0;
            else if (friendsSize > 0 && groupSize > 0)
                allViewSize = friendsSize + groupSize + 2;
            else
                allViewSize = friendsSize + groupSize + 1;
            if (curViewSize > allViewSize)
                curViewSize = allViewSize;
            return curViewSize;
        }

        @Override
        public Object getItem(int position) {
            if (friendsSize > 0 && groupSize > 0) {
                if (position <= friendsSize) {
                    if (position == 0)
                        return null;
                    else
                        return datas.get(position - 1);
                } else {
                    if (position - friendsSize - 1 == 0)
                        return null;
                    else
                        return datas.get(position - 2);
                }
            } else if (friendsSize > 0 || groupSize > 0) {
                if (position == 0) {
                    return null;
                } else {
                    return datas.get(position - 1);
                }
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int viewPosition, View convertView, ViewGroup parent) {
            if (friendsSize > 0 && groupSize > 0) {
                if (viewPosition <= friendsSize) {
                    if (viewPosition == 0)
                        convertView = getDivider("Friend");
                    else
                        convertView = getDataView(viewPosition - 1);
                } else {
                    if (viewPosition - friendsSize - 1 == 0)
                        convertView = getDivider("Group");
                    else
                        convertView = getDataView(viewPosition - 2);
                }
            } else if (friendsSize > 0 || groupSize > 0) {
                if (viewPosition == 0) {
                    if (friendsSize > 0)
                        convertView = getDivider("Friend");
                    else
                        convertView = getDivider("Group");
                } else {
                    convertView = getDataView(viewPosition - 1);
                }
            } else {
                return null;
            }

            return convertView;
        }

        private View getDivider(String title) {
            View convertView = LayoutInflater.from(context).inflate(
                    R.layout.myfriends_listview_item_divider, null);
            TextView dividerView = (TextView) convertView
                    .findViewById(R.id.myfriendslistviewitem_divider);
            dividerView.setText(title);
            return convertView;
        }

        private View getDataView(int dataPosition) {
            TextView nameView;
            TextView emailView;
            TextView numView;
            ImageView avatarView;
            View convertView;
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.myfriends_listview_item, null);

            avatarView = (ImageView) convertView
                    .findViewById(R.id.myfriendslistitem_friendimage);
            nameView = (TextView) convertView
                    .findViewById(R.id.myfriendslistitem_friendname);
            emailView = (TextView) convertView
                    .findViewById(R.id.myfriendslistitem_friendemail);
            numView = (TextView) convertView
                    .findViewById(R.id.myfriendslistitem_friendnum);

            Map<String, Object> item = datas.get(dataPosition);

            if (avatarMap.containsKey(item.get(Friend.NAME)))
                avatarView.setImageBitmap(avatarMap.get(item.get(Friend.NAME)));
            else if (Friend.GROUP != (Integer) item.get(Friend.IS_GROUP))
                avatarView
                        .setImageResource(R.drawable.default_friend_avatar_small);
            else
                avatarView
                        .setImageResource(R.drawable.default_group_avatar_small);
            nameView.setText((String) item.get(Friend.NAME));
            emailView.setText((String) item.get(Friend.EMAIL));
//            numView.setText("藏书"+"xx本");

            return convertView;
        }

        @Override
        public void loadData() {
            curUser.loadAvatars();
        }
    }

    private void setItemListener() {
        myfriendslistview
                .setOnItemClickListener(new FriendsItemClickListener());
        myfriendslistview
                .setOnItemLongClickListener(new FriendsItemLongClickListener());
    }

    private class FriendsItemClickListener implements OnItemClickListener {
        @SuppressWarnings("unchecked")
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Map<String, Object> item = (Map<String, Object>) parent
                    .getItemAtPosition(position);
            if (item == null)
                return;
            Intent intent = new Intent(activity,
                    FriendsInformationActivity.class);

            Bundle data = new Bundle();
            data.putString(Friend.NAME, item.get(Friend.NAME).toString());
            intent.putExtras(data);
            activity.startActivity(intent);
        }
    }

    private class FriendsItemLongClickListener implements
            OnItemLongClickListener {
        @SuppressWarnings("unchecked")
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {

            Map<String, Object> item = (Map<String, Object>) parent
                    .getItemAtPosition(position);
            if (item == null)
                return false;

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            builder.setTitle("Confirm!");
            builder.setMessage("Are you sure to delete "
                    + item.get(Friend.NAME) + " ?");
            builder.setPositiveButton("Yes", new DeleteFriendConfirmListener(
                    item));
            builder.setNegativeButton("No", null).show();

            return true;
        }
    }

    public void addFriend() {
        View addFrienView = LayoutInflater.from(activity).inflate(
                R.layout.add_friend_alert_dialog, null);
        EditText addFriendEdit = (EditText) addFrienView
                .findViewById(R.id.add_friend_name);
        EditText validateInput = (EditText) addFrienView
                .findViewById(R.id.validate_message);
        AlertDialog addFriendDialog = null;
        AlertDialog.Builder builder = null;

        builder = new AlertDialog.Builder(activity);
        builder.setTitle("加好友");
        builder.setMessage("请输入好友账户.");
        builder.setView(addFrienView);
        builder.setPositiveButton("确认", new AddFriendConfirmDialogListener(
                addFriendEdit, validateInput));
        builder.setNegativeButton("取消", null);
        addFriendDialog = builder.create();
        addFriendDialog.show();
    }

    private class AddFriendConfirmDialogListener implements
            DialogInterface.OnClickListener {
        EditText addFriendEdit;
        EditText validateInput;

        public AddFriendConfirmDialogListener(EditText addFriendEdit, EditText validateInput) {
            this.addFriendEdit = addFriendEdit;
            this.validateInput = validateInput;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String addFriendName = addFriendEdit.getText().toString();
            String validate = validateInput.getText().toString();
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", 1);
                obj.put("content", validate);
                obj.put("title", "我想加你");
                curUser.addFriend(addFriendName, obj.toString(),
                        HttpProgress.createShowProgress(activity, "发送成功", "发送失败"));
            } catch (JSONException e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

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

    private class DeleteFriendConfirmListener implements
            DialogInterface.OnClickListener {
        private Map<String, Object> friend;

        public DeleteFriendConfirmListener(Map<String, Object> friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            curUser.deleteFriend(friend, new DeleteFriendProgress());
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

    public void updateDisplay() {
        friendAdapter.reloadAdapter();
    }

    public void reload() {
        curUser.getFriendList(new UpdateFriendshipProgress());
    }

    public void reload(String response) {
        curUser.clearFriendData();
        try {
            curUser.addFriendDataToList(new JSONArray(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        curUser.clearAvatarBitmap();
        curUser.loadInitAvatar(new AvatarsUpdateProcess());
        updateDisplay();
    }

    private class AvatarsUpdateProcess extends HttpProcessBase {

        @Override
        public void statusError(String response) {
        }

        @Override
        public void statusSuccess(String response) {
            friendAdapter.notifyDataSetChanged();
        }

    }
}
