package group.acm.bookshare;

import group.acm.bookshare.function.Book;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.HttpProgress;
import group.acm.bookshare.function.http.NetAccess;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.util.Utils;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * BookListManage负责对书本列表的界面、动作交互进行管理
 */
public class BookListManage {
    ListView mybookslistview;
    BookListAdapter bookAdapter;
    MainActivity activity;
    User localUser;

    public BookListManage(MainActivity activity) {
        this.activity = activity;
        localUser = ((LocalApp) activity.getApplication()).getUser();
    }

    public View getView() {
        return mybookslistview;
    }

    public NetProgress getBookChangeProgress() {
        return new BookChangeProgress();
    }

    private class BookChangeProgress extends HttpProcessBase {

        public void error(String content) {
            Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
        }

        @Override
        public void statusError(String response) {
            String content = "失败:" + response;
            Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
        }

        @Override
        public void statusSuccess(String response) {
            reload(response);
            String content = "成功";
            Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("InflateParams")
    public void initBookList() {
        bookAdapter = new BookListAdapter(activity,
                localUser.getBookListData(), localUser.getBookImgs());

        View view = LayoutInflater.from(activity).inflate(
                R.layout.activity_submain_book, null);
        mybookslistview = (ListView) view.findViewById(R.id.mybookslistview);
        mybookslistview.setVerticalFadingEdgeEnabled(false);
        mybookslistview.setAdapter(bookAdapter);
        mybookslistview.setOnScrollListener(bookAdapter);
        setItemListener();
    }

    private class BookListAdapter extends PageListAdapter {
        private Context context;
        private List<Map<String, Object>> datas;
        private Map<String, Bitmap> bookImgMap;
        private int personalSize;
        private int borrowedSize;

        public BookListAdapter(Context context, List<Map<String, Object>> data,
                               Map<String, Bitmap> bookImgMap) {
            this.datas = data;
            this.context = context;
            this.bookImgMap = bookImgMap;
        }

        @Override
        public int getCount() {
            personalSize = 0;
            borrowedSize = 0;
            for (Map<String, Object> data : datas) {
                if (localUser.getUsername().equals(data.get(Book.OWNER)))
                    personalSize++;
                else
                    borrowedSize++;
            }

            int allViewSize;
            if (personalSize == 0 && borrowedSize == 0)
                allViewSize = 0;
            else if (personalSize > 0 && borrowedSize > 0)
                allViewSize = personalSize + borrowedSize + 2;
            else
                allViewSize = personalSize + borrowedSize + 1;
            if (curViewSize > allViewSize)
                curViewSize = allViewSize;
            return curViewSize;
        }

        @Override
        public void loadData() {
            localUser.loadBookImgs();
        }

        @Override
        public Object getItem(int position) {
            if (personalSize > 0 && borrowedSize > 0) {
                if (position <= personalSize) {
                    if (position == 0)
                        return null;
                    else
                        return datas.get(position - 1);
                } else {
                    if (position - personalSize - 1 == 0)
                        return null;
                    else
                        return datas.get(position - 2);
                }
            } else if (personalSize > 0 || borrowedSize > 0) {
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
            return 0;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int viewPosition, View convertView, ViewGroup parent) {
            if (personalSize > 0 && borrowedSize > 0) {
                if (viewPosition <= personalSize) {
                    if (viewPosition == 0)
                        convertView = getDivider("Personal");
                    else
                        convertView = getDataView(viewPosition - 1);
                } else {
                    if (viewPosition - personalSize - 1 == 0)
                        convertView = getDivider("Borrowed");
                    else
                        convertView = getDataView(viewPosition - 2);
                }
            } else if (personalSize > 0 || borrowedSize > 0) {
                if (viewPosition == 0) {
                    if (personalSize > 0)
                        convertView = getDivider("Personal");
                    else
                        convertView = getDivider("Borrowed");
                } else {
                    convertView = getDataView(viewPosition - 1);
                }
            } else {
                return null;
            }
            return convertView;
        }

        // 获取分界View
        private View getDivider(String title) {
            View convertView = LayoutInflater.from(context).inflate(
                    R.layout.mybooks_listview_item_divider, null);
            TextView dividerView = (TextView) convertView
                    .findViewById(R.id.mybookslistviewitem_divider);
            dividerView.setText(title);
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
            statusView.setText(getText(item)); // 状态显示

            return convertView;
        }

        // 获取当前状态的图书需要显示的文本
        private String getText(Map<String, Object> item) {
            String text = "";
            if (localUser.getUsername().equals(item.get(Book.OWNER))) {
                switch ((Integer) item.get(Book.STATUS)) {
                    case Book.STATUS_BUY | Book.STATUS_BORROW:
                        text += "可卖/可借";
                        break;
                    case Book.STATUS_BUY | Book.STATUS_UNBORROW:
                        text += "可卖/不可借";
                        break;
                    case Book.STATUS_UNBUY | Book.STATUS_BORROW:
                        text += "不可卖/可借";
                        break;
                    case Book.STATUS_UNBUY | Book.STATUS_UNBORROW:
                        text += "已借出";
                        break;
                }
            } else {
                text = "非本人";
            }

            return text;
        }
    }

    private void setItemListener() {
        mybookslistview.setOnItemClickListener(new BookInfoListener());
        mybookslistview.setOnItemLongClickListener(new JudgeListener());
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
            Book book = new Book(activity.getApplication());
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
            activity.showProgressBar();
        }

        public void error(String content) {
            activity.hideProgressBar();
        }

        @Override
        public void statusError(String response) {
            activity.hideProgressBar();
            activity.showToast(response);
        }

        @Override
        public void statusSuccess(String response) {
            activity.hideProgressBar();

            Intent intent = new Intent();
            Bundle data = new Bundle();
            JSONObject obj = Book.bookToObj(book);
            int actionType;
            if (localUser.getUsername().equals(book.get(Book.OWNER))) {
                if (((String) book.get(Book.OWNER)).equals(book
                        .get(Book.HOLDER))) { // 自己的书且未借出可删除
                    actionType = Utils.BOOK_DELETE;
                } else { // 自己的书但已借出则请求对方还书
                    actionType = Utils.BOOK_ASKRETURN;
                }
            } else { // 非本人的书则选择还书
                actionType = Utils.BOOK_RETURN;
            }
            data.putInt("action_type", actionType);
            data.putString("person_book", obj.toString());
            data.putString(NetAccess.RESPONSE, response);
            intent.putExtras(data);
            intent.setClass(activity, BookInformationActivity.class);
            activity.startActivityForResult(intent, actionType);
        }

    }

    /**
     * 根据该图书的状态判断相应的动作
     */
    private class JudgeListener implements OnItemLongClickListener {
        @SuppressWarnings("unchecked")
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            Map<String, Object> book = (Map<String, Object>) parent
                    .getItemAtPosition(position);
            if (book == null)
                return false;
            Builder builder = new AlertDialog.Builder(activity)
                    .setTitle("Confirm");
            String text;
            int actionType;
            if (localUser.getUsername().equals(book.get(Book.OWNER))) {
                if (((String) book.get(Book.OWNER)).equals(book
                        .get(Book.HOLDER))) { // 自己的书且未借出可删除
                    text = "删书";
                    actionType = Utils.BOOK_DELETE;
                } else { // 自己的书但已借出则请求对方还书
                    text = "请求对方还书";
                    actionType = Utils.BOOK_ASKRETURN;
                }
            } else { // 非本人的书则选择还书
                text = "还书";
                actionType = Utils.BOOK_RETURN;
            }
            builder = builder.setMessage(text).setPositiveButton("Yes",
                    new ActionConfirmListener(actionType, book));
            builder = builder.setNegativeButton("No", null);
            builder.show();

            return true;
        }
    }

    // 相应长按动作
    private class ActionConfirmListener implements
            DialogInterface.OnClickListener {
        private int type;
        private Map<String, Object> book;

        public ActionConfirmListener(int type, Map<String, Object> book) {
            this.type = type;
            this.book = book;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (type) {
                case Utils.BOOK_DELETE:
                    localUser.deleteBook(book, new BookChangeProgress());
                    break;
                case Utils.BOOK_RETURN:
                    localUser.returnBook(book, HttpProgress.createShowProgress(
                            activity, "发送成功", "发送失败"));
                    break;
                case Utils.BOOK_ASKRETURN:
                    localUser.askReturn(book, HttpProgress.createShowProgress(
                            activity, "发送成功", "发送失败"));
                    break;
            }
        }
    }

    public void reload() {
        localUser.getBookList(getBookChangeProgress());
    }

    public void reload(String response) {
        localUser.clearBookData();
        localUser.addBookDataToList(response);
        localUser.clearBookBitmap();
        localUser.loadInitImgs(new BookImgsUpdateProcess());
        reloadDisplay();
    }

    // 书本图像更新的过程处理
    private class BookImgsUpdateProcess extends HttpProcessBase {

        @Override
        public void statusError(String response) {
        }

        @Override
        public void statusSuccess(String response) {
            bookAdapter.notifyDataSetChanged();
        }

    }

    public void reloadDisplay() {
        bookAdapter.reloadAdapter();
    }
}