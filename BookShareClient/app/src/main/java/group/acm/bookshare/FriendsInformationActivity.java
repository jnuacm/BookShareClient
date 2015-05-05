package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.ImageManage;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.NetThread;
import group.acm.bookshare.util.Utils;
import group.acm.bookshare.util.WidgetUtil;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsInformationActivity extends BaseActivity {
    private User localUser;

    private ImageView FriendImg;
    private TextView FriendName;
    private TextView FriendArea;
    private TextView FriendEmail;

    private User friend;

    private FriendBooksAdapter adapter;
    private ListView bookslistview;
    private ProgressBar bar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_information);

        setActionBarTitle("好友信息");

        localUser = ((LocalApp) getApplication()).getUser();
        Intent intent = getIntent();
        Map<String, Object> info = localUser.getFriendByName(intent
                .getStringExtra(Friend.NAME));
        friend = new User(info, getApplication());

        FriendName = (TextView) findViewById(R.id.IF_username);
        FriendArea = (TextView) findViewById(R.id.IF_area);
        FriendImg = (ImageView) findViewById(R.id.IF_img);
        FriendEmail = (TextView) findViewById(R.id.IF_email);

        setInformation();
//        setButtons();

        // 设置界面
        bar = (ProgressBar) findViewById(R.id.friend_books_progressbar);
        adapter = new FriendBooksAdapter(this,
                friend.getBookListData());
        bookslistview = (ListView) findViewById(R.id.friend_book_listview);
        bookslistview.setAdapter(adapter);
        bookslistview.setOnItemClickListener(new BookInfoListener());
        friend.getBookList(new BooksLoadProgress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setInformation() {
        FriendName.setText(friend.getUsername());
        FriendArea.setText(friend.getArea());
        setAvatar();
        FriendEmail.setText(friend.getEmail());
    }

    private NetThread getBookThread;

    private void setButtons() {
        /*checkBooks.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if (getBookThread != null && !getBookThread.isCanceled())
                    return;
                getBookThread = friend.getBookList(new BooksLoadProgress());
            }
        });*/
        /*if (friend.getIs_group() == Friend.GROUP) {
            checkFriends.setVisibility(View.VISIBLE);
            checkFriends.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    friend.getFriendList(new UpdateFriendsProgress());
                }
            });
        } else*/
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
            String content = "ERROR:" + response;
            Toast.makeText(FriendsInformationActivity.this, content,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void statusSuccess(String response) {
            final String successResponse = response;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    friend.clearBookData();
                    JSONObject jsonobj;
                    try {
                        jsonobj = new JSONObject(successResponse);
                        JSONArray jsonarray = jsonobj.getJSONArray("own_book");
                        friend.addBookDataToList(jsonarray);
                        adapter.setDatas(friend.getBookListData());
                        adapter.notifyDataSetChanged();
                        bar.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        Toast.makeText(FriendsInformationActivity.this, e.toString(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });

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

    /////////////////////////////////////////////////////

    /**
     * 获取显示具体书本信息
     */
    private class BookInfoListener implements AdapterView.OnItemClickListener {
        @SuppressWarnings("unchecked")
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Log.i(Utils.getLineInfo(), "item click: position:" + position
                    + "  id:" + id);
            Map<String, Object> item = (Map<String, Object>) parent
                    .getItemAtPosition(position);
            if (item == null)
                return;
            Book book = new Book(getApplication());
            book.getBookByIsbn((String) item.get(Book.ISBN),
                    new BookInfoProcess(item));
        }
    }

    // 获取书本信息的过程处理
    private class BookInfoProcess extends HttpProcessBase {
        private Map<String, Object> book;

        public BookInfoProcess(Map<String, Object> book) {
            this.book = book;
        }

        public void before() {
            bar.setVisibility(View.VISIBLE);
        }

        public void error(String content) {
            bar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void statusError(String response) {
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(FriendsInformationActivity.this, response,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void statusSuccess(String response) {
            bar.setVisibility(View.INVISIBLE);

            Intent intent = new Intent();
            Bundle data = new Bundle();
            JSONObject obj = Book.bookToObj(book);
            int actionType = Utils.BOOK_BORROW;
            data.putInt("action_type", actionType);
            data.putString("person_book", obj.toString());
            data.putString(NetAccess.RESPONSE, response);
            data.putString("define", friend.getUsername());
            intent.putExtras(data);
            intent.setClass(FriendsInformationActivity.this,
                    BookInformationActivity.class);
            startActivityForResult(intent, actionType);
        }
    }

    private class FriendBooksAdapter extends BaseAdapter {
        List<Map<String, Object>> datas;
        Context context;

        public FriendBooksAdapter(Context context,
                                  List<Map<String, Object>> data) {
            this.context = context;
            datas = data;
        }

        public void setDatas(List<Map<String, Object>> datas) {
            this.datas = datas;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView title;
            TextView status;
            Button borrow;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.friendbooklistview_item, null);
            }

            title = (TextView) convertView
                    .findViewById(R.id.booklistviewitem_title);
            status = (TextView) convertView
                    .findViewById(R.id.booklistviewitem_status);
            borrow = (Button) convertView
                    .findViewById(R.id.booklistviewitem_borrow);

            Map<String, Object> item = datas.get(position);

            int bookStatus = (Integer) item.get(Book.STATUS);

            title.setText((String) item.get(Book.NAME));
            String[] text = {"不可借", "可借", "不可卖", "可卖"};
            status.setText(text[bookStatus]);
            if (bookStatus == 1)
                borrow.setOnClickListener(new ItemButtonClick(position));
            else
                borrow.setVisibility(View.INVISIBLE);

            return convertView;
        }

        private NetThread borrowThread;

        private class ItemButtonClick implements OnClickListener {
            private int position;

            public ItemButtonClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                WidgetUtil.createApmDialog(FriendsInformationActivity.this, new WidgetUtil.ApmConfirm() {
                    @Override
                    public void onInput(String time, String location) {
                        if (borrowThread != null && !borrowThread.isCanceled())
                            return;
                        try {
                            borrowThread = localUser.borrowBook(friend.getUsername(), datas.get(position), time, location,
                                    new BorrowBookProgress());
                        } catch (JSONException e) {
                            Toast.makeText(FriendsInformationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }).show();
            }

        }

        private class BorrowBookProgress extends HttpProcessBase {

            public void error(String content) {
                Toast.makeText(FriendsInformationActivity.this, "网络错误",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void statusError(String response) {
                Toast.makeText(FriendsInformationActivity.this, "发送失败",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void statusSuccess(String response) {
                Toast.makeText(FriendsInformationActivity.this, "发送成功",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // 调用扫描功能的返回结果需要在onActivitiyResult中获取
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case Utils.BOOK_BORROW:
                    break;
            }
        }
    }
}
