package group.acm.bookshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class BookInformationActivity extends Activity {
	List<Map<String, Object>> commentList;
	SimpleAdapter commentAdapter;
	ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		initListView();
		setButtonEvent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.book_information, menu);
		return true;
	}

	private void initListView() {
		commentList = new ArrayList<Map<String, Object>>();
		listview = (ListView) findViewById(R.id.bookDetail_comments);
		commentAdapter = new SimpleAdapter(BookInformationActivity.this,
				commentList, R.layout.bookdetail_listview_item, new String[] {
						"owner", "content", "date" }, new int[] {
						R.id.comment_owner, R.id.comment_content,
						R.id.comment_time });
		listview.setAdapter(commentAdapter);
	}

	private void setButtonEvent() {
		Button button = (Button) findViewById(R.id.button_showcomment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadComments();
				commentAdapter.notifyDataSetChanged();
			}
		});
	}

	private void loadComments() {
		Map<String, Object> map;

		for (int i = 0; i < 5; i++) {
			map = new HashMap<String, Object>();
			map.put("owner", "luo" + i);
			map.put("content",
					"today is a goooooooooooooooooooooooooooooooooooooooooood day");
			map.put("date", "2014/07/17");
			commentList.add(map);
		}
	}
}
