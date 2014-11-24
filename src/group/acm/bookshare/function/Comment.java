package group.acm.bookshare.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Comment {
	public static final String PERSON = "person";
	public static final String CONTENT = "content";
	public static final String DATE = "date";

	public static List<Map<String, Object>> strToList(String response) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		return ret;
	}

	public static Map<String, Object> objToComment(JSONObject item) {
		Map<String, Object> ret = new HashMap<String, Object>();
		return ret;
	}

}
