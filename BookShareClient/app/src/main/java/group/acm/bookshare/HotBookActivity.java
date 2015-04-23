package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.util.Utils;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HotBookActivity extends Activity {
    private User localUser;
    private ImageView backButton;
    private ListView bookListView;
    private BookListAdapter adapter;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_book);

        localUser = ((LocalApp) getApplication()).getUser();
        adapter = new BookListAdapter(this, localUser.getHotListData(),
                localUser.getBookImgs());
        bar = (ProgressBar) findViewById(R.id.hotbook_progressbar);
        backButton = (ImageView) findViewById(R.id.hotbook_bar_img);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bookListView = (ListView) findViewById(R.id.hotbook_listview);
        bookListView.setAdapter(adapter);
        bookListView.setOnItemClickListener(new BookInfoListener());
        localUser.getHotBookList(new HotBookProgress());
    }

    private class BookListAdapter extends BaseAdapter {
        private Context context;
        private List<Map<String, Object>> datas;
        private Map<String, Bitmap> bookImgMap;

        public BookListAdapter(Context context, List<Map<String, Object>> data,
                               Map<String, Bitmap> bookImgMap) {
            this.datas = data;
            this.context = context;
            this.bookImgMap = bookImgMap;
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
        public View getView(int viewPosition, View convertView, ViewGroup parent) {
            convertView = getDataView(viewPosition);
            return convertView;
        }

        // 获取书本显示View
        private View getDataView(int dataPosition) {
            TextView titleView;
            TextView statusView;
            ImageView coverView;
            View convertView = LayoutInflater.from(context).inflate(
                    R.layout.mybooks_listview_item, null);
            coverView = (ImageView) convertView
                    .findViewById(R.id.mybookslistitem_bookimage);
            titleView = (TextView) convertView
                    .findViewById(R.id.mybookslistitem_bookname);
            statusView = (TextView) convertView
                    .findViewById(R.id.mybookslistitem_bookstate);

            Map<String, Object> item = datas.get(dataPosition);

            if (bookImgMap.containsKey(item.get(Book.ISBN)))
                coverView.setImageBitmap(bookImgMap.get(item.get(Book.ISBN)));
            else
                coverView.setImageResource(R.drawable.default_book_big);
            titleView.setText((String) item.get(Book.NAME));
            statusView.setText("x个赞"); // 状态显示

            return convertView;
        }
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
            Toast.makeText(HotBookActivity.this, response, Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void statusSuccess(String response) {
            bar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent();
            Bundle data = new Bundle();
            JSONObject obj = Book.bookToObj(book);
            data.putString("person_book", obj.toString());
            data.putString(NetAccess.RESPONSE, response);
            intent.putExtras(data);
            intent.setClass(HotBookActivity.this, BookInformationActivity.class);
        }
    }

    // 热书列表获取过程界面处理
    private class HotBookProgress extends HttpProcessBase {

        @Override
        public void statusError(String response) {
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(HotBookActivity.this, response, Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void statusSuccess(String response) {
            bar.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
            Toast.makeText(HotBookActivity.this,
                    ":" + localUser.getHotListData().size(), Toast.LENGTH_LONG)
                    .show();
            List<Map<String, Object>> hotBooks = localUser.getHotListData();
            for (Map<String, Object> book : hotBooks) {
                Log.i(Utils.getLineInfo(), "name:" + book.get(Book.NAME)
                        + " url:" + book.get(Book.IMG_URL_SMALL));
                localUser.loadBookImg(book, new UpdateImgsProgress());
            }
        }

    }

    // 图像加载过程界面显示处理
    private class UpdateImgsProgress extends HttpProcessBase {

        @Override
        public void statusError(String response) {
        }

        @Override
        public void statusSuccess(String response) {
            adapter.notifyDataSetChanged();
        }
    }
}
