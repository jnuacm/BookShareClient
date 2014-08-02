package group.acm.bookshare.function;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Inform {
	public static final int REQUEST_TYPE_BORROW = 1;
	public static final int REQUEST_TYPE_RETURN = 2;
	
	public static final int REQUEST_STATUS_UNPROCESSED = 0;
	public static final int REQUEST_STATUS_PERMITTED = 1;
	public static final int REQUEST_STATUS_REFUSED = 2;
	public static final int REQUEST_STATUS_CONFIRM = 3;

	public static Map<String, Object> objToSend(JSONObject item)
			throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();
		int id = item.getInt("id");
		String time = item.getString("time");
		String from = item.getString("from");
		int type = item.getInt("type");
		String description = item.getString("description");
		int status = item.getInt("status");

		map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("time", time);
		map.put("from", from);
		map.put("type", type);
		map.put("description", description);
		map.put("status", status);

		return map;
	}

	public static Map<String, Object> objToReceive(JSONObject item)
			throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();
		int id = item.getInt("id");
		String time = item.getString("time");
		String from = item.getString("from");
		int type = item.getInt("type");
		String description = item.getString("description");
		int status = item.getInt("status");

		map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("time", time);
		map.put("from", from);
		map.put("type", type);
		map.put("description", description);
		map.put("status", status);

		return map;
	}
}
