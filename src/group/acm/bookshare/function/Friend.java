package group.acm.bookshare.function;

import group.acm.bookshare.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Friend {
	public static final String NAME = "username";
	public static final String AREA = "area";
	public static final String EMAIL = "email";
	public static final String IS_GROUP = "is_group";
	public static final int GROUP = 1;
	
	public static List<Map<String, Object>> jsonArrayToFriends(
			JSONArray jsonarray) {
		try {
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < jsonarray.length(); i++) {
				data.add(objToFriend(jsonarray.getJSONObject(i)));
			}
			return data;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Map<String, Object> objToFriend(JSONObject item) {
		Map<String, Object> map;
		try {
			String name = item.getString(NAME);
			String email = item.getString(EMAIL);
			String area = item.getString(AREA);
			int is_group = item.getInt(IS_GROUP);

			map = new HashMap<String, Object>();
			map.put(NAME, name);
			map.put(EMAIL, email);
			map.put(AREA, area);
			map.put("image", R.drawable.friend_avatar_small_default);
			map.put(IS_GROUP, is_group);
			return map;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> objToGroup() {
		return null;
	}
}
