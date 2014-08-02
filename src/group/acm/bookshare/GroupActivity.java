package group.acm.bookshare;

import group.acm.bookshare.function.Friend;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class GroupActivity extends Activity {
	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		listView = new ListView(this);

		SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.mygroup_listview_item,
				new String[]{"groupimage","groupname"},
				new int[]{R.id.mygrouplistitem_groupimage,R.id.mygrouplistitem_groupname});
		listView.setAdapter(adapter);
		setContentView(listView);
	}
	
	 private List<Map<String,Object>> getData(){
         	
		 	User user = ((LocalApp) getApplication()).getUser();
	        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	        List<Friend> list = new ArrayList<Friend>(user.getGroup());
	        for(Friend i:list){
	        	Map<String, Object> map = new HashMap<String, Object>();
	        	map.put("groupimage",R.drawable.group);
	        	map.put("groupname",i.getName());
	        	data.add(map);
	        }
	        return data;
	    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}

}
