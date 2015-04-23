package group.acm.bookshare.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
	public static final String ID = "id";
	public static final String PERSON = "author";
	public static final String CONTENT = "content";
	public static final String DATE = "time";

	public static List<Map<String, Object>> strToList(String response) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				ret.add(Comment.objToComment(array.getJSONObject(i)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static Map<String, Object> objToComment(JSONObject item) {
		Map<String, Object> ret = new HashMap<String, Object>();
		int id = 0;
		String content = "empty";
		String date = "empty";
		String person = "empty";
		try {
			id = item.getInt(Comment.ID);
			content = item.getString(Comment.CONTENT);
			person = item.getString(Comment.PERSON);
			date = item.getString(Comment.DATE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ret.put(Comment.ID, id);
		ret.put(Comment.PERSON, person);
		ret.put(Comment.CONTENT, content);
		ret.put(Comment.DATE, date);
		return ret;
	}

}
