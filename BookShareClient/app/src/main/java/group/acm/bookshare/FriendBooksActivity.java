package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetAccess.NetThread;
import group.acm.bookshare.util.Utils;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendBooksActivity extends Activity {
    private User localUser;
    private User friend;

    private FriendBooksAdapter adapter;

    private ListView bookslistview;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_books);

        localUser = ((LocalApp) getApplication()).getUser();
        friend = localUser.getFriend();

        // 设置界面
        bar = (ProgressBar) findViewById(R.id.friend_books_progressbar);
        adapter = new FriendBooksAdapter(this,
                friend.getBookListData());
        bookslistview = (ListView) findViewById(R.id.friend_book_listview);
        bookslistview.setAdapter(adapter);
        bookslistview.setOnItemClickListener(new BookInfoListener());
    }

    /**
     * 获取显示具体书本信息
     */
    private class BookInfoListener implements OnItemClickListener {
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
            Toast.makeText(FriendBooksActivity.this, response,
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
            intent.setClass(FriendBooksActivity.this,
                    BookInformationActivity.class);
            startActivityForResult(intent, actionType);
        }

    }

    private class FriendBooksAdapter extends PageListAdapter {
        List<Map<String, Object>> datas;
        Context context;

        public FriendBooksAdapter(Context context,
                                  List<Map<String, Object>> data) {
            this.context = context;
            datas = data;
        }

        @Override
        public void loadData() {
        }

        @Override
        public int getCount() {
            if (curViewSize > datas.size())
                curViewSize = datas.size();
            return curViewSize;
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
            String[] text = { "不可借", "可借", "不可卖", "可卖" };
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
                if (borrowThread != null && !borrowThread.isCanceled())
                    return;
                borrowThread = localUser.borrowBook(friend.getUsername(), datas.get(position),
                        new BorrowBookProgress());
            }

        }

        private class BorrowBookProgress extends HttpProcessBase {

            public void error(String content) {
                Toast.makeText(FriendBooksActivity.this, "网络错误",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void statusError(String response) {
                Toast.makeText(FriendBooksActivity.this, "发送失败",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void statusSuccess(String response) {
                Toast.makeText(FriendBooksActivity.this, "发送成功",
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
