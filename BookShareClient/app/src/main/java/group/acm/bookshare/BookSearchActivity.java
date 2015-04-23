package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BookSearchActivity extends Activity {
    private User localUser;

    private Spinner typeSpinner; // 下拉列表
    private EditText searchText; // 搜索输入框
    private Button searchButton; // 搜索按钮
    private ListView bookListview; // 显示搜索结果列表
    private BookListAdapter bookAdapter;

    private int curChoosed = 0; // 记录当前选中搜索类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        localUser = ((LocalApp) getApplication()).getUser();

        searchText = (EditText) findViewById(R.id.search_edittext);
        initSpinner();
        initSearchButton();
        initListView();
    }

    /**
     * 构建spinner
     */
    private void initSpinner() {
        typeSpinner = (Spinner) findViewById(R.id.search_spinner);
        ArrayList<String> list = new ArrayList<String>();
        list.add("书名");
        list.add("作者");
        list.add("isbn");
        list.add("出版社");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner
                .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        curChoosed = position;
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });
    }

    private void initSearchButton() {
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new SearchClickListener(
                new SearchProcess()));
    }

    /**
     * 获取显示的图书列表
     *
     * @param response
     * @return
     */
    private void initListView() {
        bookAdapter = new BookListAdapter(this,
                new ArrayList<Map<String, Object>>(), localUser.getBookImgs());
        bookListview = (ListView) findViewById(R.id.book_search_listview);
        bookListview.setAdapter(bookAdapter);
        bookListview.setOnItemClickListener(new SearchItemClick());
    }

    /**
     * 搜索列表Adapter
     *
     */
    private class BookListAdapter extends PageListAdapter {
        private Context context;
        private List<Map<String, Object>> datas;
        private Map<String, Bitmap> bookImgMap;

        public BookListAdapter(Context context, List<Map<String, Object>> data,
                               Map<String, Bitmap> bookImgMap) {
            this.datas = data;
            this.context = context;
            this.bookImgMap = bookImgMap;
        }

        public void setDatas(List<Map<String, Object>> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            int allViewSize = datas.size();
            if (curViewSize > allViewSize)
                curViewSize = allViewSize;
            return curViewSize;
        }

        @Override
        public void loadData() {
            for (int i = curViewSize + 1; i < datas.size()
                    && i < curViewSize + User.PERTIME_LOAD_NUMBER; i++) {
                localUser.loadBookImg(datas.get(i));
            }
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int viewPosition, View convertView, ViewGroup parent) {
            convertView = getDataView(viewPosition);
            return convertView;
        }

        private View getDataView(int dataPosition) {
            TextView titleView;
            TextView ownerView;
            TextView holderView;
            ImageView coverView;
            View convertView = LayoutInflater.from(context).inflate(
                    R.layout.search_book_listview_item, null);
            coverView = (ImageView) convertView
                    .findViewById(R.id.search_book_listitem_bookimage);
            titleView = (TextView) convertView
                    .findViewById(R.id.search_book_listitem_bookname);
            ownerView = (TextView) convertView
                    .findViewById(R.id.search_book_listitem_bookowner);
            holderView = (TextView) convertView
                    .findViewById(R.id.search_book_listitem_bookholder);

            Map<String, Object> item = datas.get(dataPosition);

            if (bookImgMap.containsKey(item.get(Book.ISBN)))
                coverView.setImageBitmap(bookImgMap.get(item.get(Book.ISBN)));
            else
                coverView.setImageResource(R.drawable.default_book_big);
            titleView.setText((String) item.get(Book.NAME));
            ownerView.setText((String) item.get(Book.OWNER));
            holderView.setText((String) item.get(Book.HOLDER));

            return convertView;
        }
    }



    /**
     * 搜索按钮点击响应
     */
    private class SearchClickListener implements OnClickListener {
        NetProgress progress;

        public SearchClickListener(NetProgress progress) {
            this.progress = progress;
        }

        @Override
        public void onClick(View v) {
            String content = searchText.getText().toString();
            if (content.length() <= 0)
                return;
            String isbn = "";
            String name = "";
            String author = "";
            String publisher = "";
            switch (curChoosed) {
                case 0:
                    name = content;
                    break;
                case 1:
                    author = content;
                    break;
                case 2:
                    isbn = content;
                    break;
                case 3:
                    publisher = content;
                    break;
            }
            Log.i(Utils.getLineInfo(), "isbn:" + isbn + " name:" + name
                    + " author:" + author + " publisher:" + publisher);
            localUser.bookSearch(isbn, name, author, publisher, progress);
        }
    }

    /**
     * 搜索处理
     */
    private class SearchProcess extends HttpProcessBase {
        public void before(){
            showProgressBar();
        }

        @Override
        public void statusError(String response) {
            hideProgressBar();
            Toast.makeText(BookSearchActivity.this, response, Toast.LENGTH_LONG)
                    .show();
            // showResponse("[{\"id\":\"1\",\"name\":\"design\",\"publisher\":\"jp\",\"description\":\"empty\",\"author\":\"fans\",\"isbn\":\"9787302251200\",\"owner\":\"amy\",\"holder\":\"cc\",\"status\":\"0\",\"small_img\":\"url\",\"medium_img\":\"url\",\"large_img\":\"url\"}]");
        }

        @Override
        public void statusSuccess(String response) {
            hideProgressBar();
            showResponse(response);
        }

    }

    private void showResponse(String response) {
        JSONArray array;
        try {
            array = new JSONArray(response);
        } catch (JSONException e) {
            array = new JSONArray();
            e.printStackTrace();
        }
        List<Map<String, Object>> datas = Book.jsonArrayToBooks(array);

        // 预先加载部分图片
        for (int i = 0; i < datas.size() && i < User.PERTIME_LOAD_NUMBER; i++) {
            localUser.loadBookImg(datas.get(i));
        }

        bookAdapter.setDatas(datas);
        bookAdapter.reloadAdapter();
    }

    /**
     * 搜索结果列表item点击响应
     *
     */
    private class SearchItemClick implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Map<String, Object> item = (Map<String, Object>) parent
                    .getItemAtPosition(position);
            Book book = new Book(getApplication());
            book.getBookByIsbn((String) item.get(Book.ISBN),
                    new BookInfoProcess(item));
        }
    }

    /**
     * 搜书过程处理
     *
     */
    private class BookInfoProcess extends HttpProcessBase {
        private Map<String, Object> book;

        public BookInfoProcess(Map<String, Object> book) {
            this.book = book;
        }

        public void before() {
            showProgressBar();
        }

        public void error(String content) {
            hideProgressBar();
        }

        @Override
        public void statusError(String response) {
            hideProgressBar();
            showToast(response);
        }

        @Override
        public void statusSuccess(String response) {
            hideProgressBar();

            Intent intent = new Intent();
            Bundle data = new Bundle();
            JSONObject obj = Book.bookToObj(book);
            data.putString("person_book", obj.toString());
            data.putString(NetAccess.RESPONSE, response);
            intent.putExtras(data);
            intent.setClass(BookSearchActivity.this,
                    BookInformationActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 添加记录item，且不重复，按输入顺序排列
     *
     * @param content
     */
    private void addHistory(String content) {
        SharedPreferences sp = getSharedPreferences(localUser.getUsername()
                + "_history", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Set<String> data = new LinkedHashSet<String>();
        List<String> list = new ArrayList<String>(); //
        for (String item : list) {
            data.add(item);
        }
        data.add(content);
        switch (curChoosed) {
            case 0:
                editor.putStringSet(Book.NAME, data);
                break;
            case 1:
                editor.putStringSet(Book.AUTHOR, data);
                break;
            case 2:
                editor.putStringSet(Book.ISBN, data);
                break;
            case 3:
                editor.putStringSet(Book.PUBLISHER, data);
                break;
        }
        editor.commit();
    }

    /**
     * 从文件读取当前用户历史记录
     *
     * @return
     */
    private List<String> getHistoryList() {
        List<String> list = new ArrayList<String>();
        SharedPreferences sp = getSharedPreferences(localUser.getUsername()
                + "_history", Activity.MODE_PRIVATE);

        Set<String> data = new LinkedHashSet<String>();

        switch (curChoosed) {
            case 0:
                data = sp.getStringSet(Book.NAME, new LinkedHashSet<String>());
                break;
            case 1:
                data = sp.getStringSet(Book.AUTHOR, new LinkedHashSet<String>());
                break;
            case 2:
                data = sp.getStringSet(Book.ISBN, new LinkedHashSet<String>());
                break;
            case 3:
                data = sp.getStringSet(Book.PUBLISHER, new LinkedHashSet<String>());
                break;
        }

        for (String item : data) {
            list.add(item);
        }

        return list;
    }

    public void hideProgressBar() {
        findViewById(R.id.book_search_progressbar)
                .setVisibility(View.INVISIBLE);
    }

    public void showProgressBar() {
        findViewById(R.id.book_search_progressbar).setVisibility(View.VISIBLE);
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }
}
