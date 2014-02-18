package com.example.bookshare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.entity.Book;
import com.entity.Comment;
import com.entity.LocalAccount;
import com.socket.AQuery;
import com.socket.Ackownledge;
import com.socket.RComment;
import com.socket.RFetchComment;

public class BodyActivity extends Activity {
	TabHost mTabHost;

	private String userName, baseName;
	private static String isbn;
	private static SimpleAdapter simpleAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_body);

		// 获取接下来整个Activity均需用到的变量值，且该值不再更改
		isbn = getIntent().getStringExtra("ISBN");
		Log.i("isbn",isbn);
		
		LocalAccount localact = (LocalAccount) getApplicationContext();
		userName = localact.getAccount().getAccount();
		baseName = localact.getAccount().getLibrary();

		// 显示Tab1
		showBook(getInfobyIsbn(isbn));
		
		Log.i("text","after show book");

		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		mTabHost.addTab(mTabHost
				.newTabSpec("tab_test1")
				.setIndicator("书籍信息",
						getResources().getDrawable(R.drawable.indicator))
				.setContent(R.id.textview1));
		mTabHost.addTab(mTabHost
				.newTabSpec("tab_test2")
				.setIndicator("评论",
						getResources().getDrawable(R.drawable.indicator))
				.setContent(R.id.textview2));

		// 设置TabHost的背景颜色
		//mTabHost.setBackgroundColor(Color.argb(150, 22, 70, 150));
		// 设置TabHost的背景图片资源
		// mTabHost.setBackgroundResource(R.drawable.bg0);

		// 设置当前显示哪一个标签
		mTabHost.setCurrentTab(0);

		// Tab2评论查看、添加

		
		RFetchComment RF = new RFetchComment(baseName, isbn);
		RF.send();
		
		Log.i("text","before list");
		List<Comment> lscomment = AQuery.receiveCommentlist();

		final List<Map<String, Object>> commentslist = new ArrayList<Map<String, Object>>();

		//Timestamp.valueOf("yyyy-MM-dd HH:mm:ss");
		Log.i("text","after list");
		if (lscomment != null) {
			for (int i = 0; i < lscomment.size(); i++) {
				Comment tcom = lscomment.get(i);
				Map<String, Object> item = new HashMap<String, Object>();
				String content = tcom.getContent();
				String commentperson = tcom.getSubmitterAccount();
				Timestamp commenttime = tcom.getTs();
				item.put("content", content); // 评价内容
				item.put("commentperson", commentperson); // 评价人
				item.put("commenttime", commenttime.toString()); // 评价时间
				commentslist.add(item);
			}

			simpleAdapter = new SimpleAdapter(this, commentslist,
					R.layout.comments_list, new String[] { "content",
							"commentperson", "commenttime" }, new int[] {
							R.id.commentscontent, R.id.commentsauthor,
							R.id.commentstime });
			ListView list = (ListView) findViewById(R.id.commentslist);
			list.setAdapter(simpleAdapter);
		}

		Log.i("text","after comment");
		Button button = (Button) findViewById(R.id.commentbutton);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				EditText comments = (EditText) findViewById(R.id.commentsinput);
				String commentsStr = comments.getText().toString(); // 获取评价

				if (commentsStr.isEmpty()) {
					// 为空时显示toast并且不响应
					Toast.makeText(getApplicationContext(), "评论为空",
							Toast.LENGTH_SHORT).show();
					return;
				}

				Timestamp ts = new Timestamp(System.currentTimeMillis());

				RComment RC = new RComment(baseName, isbn, userName,
						commentsStr, ts);
				RC.send();

				boolean is_legal = Ackownledge.receive().getStatus();
				if (true == is_legal) {
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("content", commentsStr);
					item.put("commentperson", userName);
					item.put("commenttime", ts.toString());

					if (commentslist.size() == 0) {
						commentslist.add(0, item);
						simpleAdapter = new SimpleAdapter(
								BodyActivity.this,
								commentslist,
								R.layout.comments_list,
								new String[] { "content", "commentperson",
										"commenttime" },
								new int[] { R.id.commentscontent,
										R.id.commentsauthor, R.id.commentstime });
						ListView list = (ListView) findViewById(R.id.commentslist);
						list.setAdapter(simpleAdapter);
					} else {
						commentslist.add(0, item);
						simpleAdapter.notifyDataSetChanged();
					}
					Toast.makeText(getApplicationContext(), "评价成功！",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(BodyActivity.this, "评论失败!",
							Toast.LENGTH_LONG).show();
					return;
				}
			}
		});
	}

	// 通过isbn获取书本信息
	public Book getInfobyIsbn(String tisbn) {
		Book retBook;
		String bookname // 书名
		, authorname // 作者名
		, summary // 内容简介
		, imageurl // 图片地址
		, isbn10 // 10位ISBN
		, isbn13 // 13位ISBN
		, publisher // 出版社
		, price; // 价格
		bookname = authorname = summary = imageurl = isbn10 = isbn13 = publisher = price = null;
		String url = "http://api.douban.com/book/subject/isbn/" + tisbn
				+ "?alt=json";
		try {
			URL turl = new URL(url);
			StringBuilder builder = new StringBuilder();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(turl.openStream()));
			for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
					.readLine()) {
				builder.append(s);
			}
			String jsonstr = builder.toString();

			JSONObject json = new JSONObject(jsonstr);

			if (!json.isNull("title")) {
				bookname = json.getJSONObject("title").getString("$t");
			}

			if (!json.isNull("author")) {
				JSONArray namearray = json.getJSONArray("author");
				for (int i = 0; i < namearray.length(); i++) {
					JSONObject nameobj = namearray.getJSONObject(i);
					authorname = "";
					authorname = authorname
							+ nameobj.getJSONObject("name").getString("$t")
							+ " ";
				}
			}

			if (!json.isNull("summary")) {
				summary = json.getJSONObject("summary").getString("$t");
			}

			if (!json.isNull("db:attribute")) {
				JSONArray attribute = json.getJSONArray("db:attribute");

				for (int i = 0; i < attribute.length(); i++) {
					JSONObject attrobj = attribute.optJSONObject(i);
					String tmp;
					tmp = "isbn10";
					if (tmp.equals(attrobj.getString("@name"))) {
						isbn10 = attrobj.getString("$t");
					}
					tmp = "isbn13";
					if (tmp.equals(attrobj.getString("@name"))) {
						isbn13 = attrobj.getString("$t");
					}
					tmp = "publisher";
					if (tmp.equals(attrobj.getString("@name"))) {
						publisher = attrobj.getString("$t");
					}
					tmp = "price";
					if (tmp.equals(attrobj.getString("@name"))) {
						price = attrobj.getString("$t");
					}
				}
			}

			if (!json.isNull("link")) {
				JSONArray jsonarray = json.getJSONArray("link");
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject linkobj = jsonarray.optJSONObject(i);
					String tmp = "image";
					if (tmp.equals(linkobj.getString("@rel"))) {
						imageurl = linkobj.getString("@href");
						break;
					}
				}
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		retBook = new Book(tisbn, bookname, authorname, publisher, summary,
				imageurl, price);

		return retBook;
	}

	public void showBook(Book book) {
		// 显示具体信息
		TextView textview = (TextView) findViewById(R.id.booktitle);
		textview.setText(book.getTitle());

		textview = (TextView) findViewById(R.id.bookauthor);
		textview.setText(book.getAuthor());

		textview = (TextView) findViewById(R.id.bookpublisher);
		textview.setText(book.getPublisher());

		textview = (TextView) findViewById(R.id.bookprice);
		textview.setText(book.getPrice());

		textview = (TextView) findViewById(R.id.bookcontent);
		textview.setText(book.getSummary());

		// 获取图片
		String imageurl = book.getCover();
		imageurl = imageurl.replace("spic", "lpic");

		URL myFileUrl = null;
		Bitmap bitmap = null;

		try {
			myFileUrl = new URL(imageurl);

			HttpURLConnection iconn;

			iconn = (HttpURLConnection) myFileUrl.openConnection();

			iconn.setDoInput(true);
			iconn.connect();

			InputStream is = iconn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ImageView imView = (ImageView) findViewById(R.id.bookpicture);
		imView.setImageBitmap(bitmap);
	}
}
