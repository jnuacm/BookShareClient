package com.example.bookshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.entity.Book;
import com.entity.LocalAccount;

public class BrowseActivity extends Activity{
	private List<Book> books;
    private	LinearLayout  BrowseLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("AAA","AAA");
		setContentView(R.layout.activity_browse);
		
		BrowseLayout = (LinearLayout) findViewById(R.id.BrowseLayout);
		//BrowseLayout.setBackgroundColor(Color.argb(150, 22, 70, 150));
		
		ListView list_view = (ListView) findViewById(R.id.BrowseListView);
		
		LocalAccount localact = (LocalAccount)getApplicationContext();
		books = localact.getBooks();
		
		ArrayList<HashMap<String, Object> > listItem = new ArrayList<HashMap<String, Object> >();    
		
		for(int i = 0; i < books.size(); i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("elem_title",books.get(i).getTitle());
			listItem.add(map);
		}
		
		SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,//数据源   
	             R.layout.list_item, 
	             //动态数组与Item对应的子项          
	             new String[] {"elem_title"},   
	             //ImageItem的XML文件里面的一个ImageView,两个TextView ID  
	             new int[] {R.id.elem_title}  
	         );
		
		list_view.setAdapter(listItemAdapter);
		
		 list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() 
	        {
	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
	            {
	            	Intent intent = new Intent(BrowseActivity.this,BodyActivity.class);
	            	
	            	int id = arg2;
	            	
	            	intent.putExtra("ISBN",books.get(id).getISBN()); //选中的书在书列表中的下标
	            	
	            	startActivity(intent);
	            	finish();
	            }
	        }
	     );
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse, menu);
		return true;
	}

}


