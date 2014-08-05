package group.acm.bookshare;

import group.acm.bookshare.function.NoScrollListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BookInformationActivity extends Activity {
	String				bookName;
	String 				bookIsbn;
	String 				bookDescription;
	int    				bookImage;
	NoScrollListView	listview;
	
	List<Map<String, Object>> commentList;
	SimpleAdapter commentAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);
		
		TextView bookNameView = (TextView)findViewById(R.id.book_name);
		TextView bookDescriptionView = (TextView)findViewById(R.id.book_description);
		ImageView bookImageView = (ImageView)findViewById(R.id.book_image);
		
		Intent intent =getIntent();// 收取 email 
		Bundle bundle =intent.getBundleExtra("key");// 打开 email 
		bookName = bundle.getString("bookName");
		bookIsbn = bundle.getString("bookIsbn"); 
		bookDescription = bundle.getString("bookDescription");
		bookImage = bundle.getInt("bookImage");
		
		bookNameView.setText("《"+bookName+"》");
		bookNameView.setTextSize(30);
		bookDescriptionView.setText("        "+bookDescription);
		bookDescriptionView.setTextSize(15);
		bookImageView.setImageResource(bookImage);
		
		listview = (NoScrollListView) findViewById(R.id.bookDetail_comments);
		initListView();
		loadComments();
		commentAdapter.notifyDataSetChanged();
		setListViewHeightBasedOnChildren(listview); 
		
		setButtonEvent();
	}
	
	
	
    public void setListViewHeightBasedOnChildren(ListView listView) {
    	  // 获取ListView对应的Adapter
    	  ListAdapter listAdapter = listView.getAdapter();
    	  if (listAdapter == null) {
    	   return;
    	  }
    	  
    	  int totalHeight = 0;
    	  for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()返回数据项的数目
    		  View listItem = listAdapter.getView(i, null, listView);
    		  listItem.measure(0, 0); // 计算子项View 的宽高
    		  totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
    	  }
    	  
    	  ViewGroup.LayoutParams params = listView.getLayoutParams();
    	  params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

    	  listView.setLayoutParams(params);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.book_information, menu);
		return true;
	}

	private void initListView() {
		commentList = new ArrayList<Map<String, Object>>();
		
		commentAdapter = new SimpleAdapter(BookInformationActivity.this,
				commentList, R.layout.bookdetail_listview_item, new String[] {
						"owner", "content", "date" }, new int[] {
						R.id.comment_owner, R.id.comment_content,
						R.id.comment_time });
		listview.setAdapter(commentAdapter);
	}

	private void setButtonEvent() {
		Button button = (Button) findViewById(R.id.button_addcomment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadComments();
				commentAdapter.notifyDataSetChanged();
			}
		});
	}
	
	
	/////////////////////////////////////////////////////
	//////////////////以下用于测试///////////////////////////
	/////////////////////////////////////////////////////
	public String radomString() {

		Random rand = new Random();
		
		final int A = 'A', z = 'Z';
		int r = rand.nextInt(50)+rand.nextInt(200);
		StringBuilder sb = new StringBuilder();
		while(sb.length() < r){
			int number = rand.nextInt(z + 1);
			if(number >= A){
				sb.append((char)number);
			}
		}
					
		return sb.toString();

	}

	private void loadComments() {
		Map<String, Object> map;
		Random rand = new Random();
		int ll = rand.nextInt(50);
		Log.i("rand.nextInt(50); = ",ll+"");
		for (int i = 0; i < ll; i++) {
			map = new HashMap<String, Object>();
			map.put("owner", "luo" + i);
			map.put("content",radomString());
			map.put("date", "2014/07/17");
			commentList.add(map);
		}
	}
}
