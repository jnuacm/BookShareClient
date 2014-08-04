package group.acm.bookshare.function;

import group.acm.bookshare.MainActivity.InformListManage.InformListAdapter.ViewHolder;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class Inform {
	public static final int REQUEST_TYPE_BORROW = 1;
	public static final int REQUEST_TYPE_RETURN = 2;

	public static final int REQUEST_STATUS_UNPROCESSED = 0;
	public static final int REQUEST_STATUS_PERMITTED = 1;
	public static final int REQUEST_STATUS_REFUSED = 2;
	public static final int REQUEST_STATUS_CONFIRM = 3;

	public static final int BORROW_UNPROCESSED_SELF = 0;
	public static final int BORROW_UNPROCESSED_NOTSELF = 1;
	public static final int BORROW_PERMITTED_SELF = 2;
	public static final int BORROW_REFUSED_SELF = 3;
	public static final int RETURN_UNPROCESSED_SELF = 4;
	public static final int RETURN_UNPROCESSED_NOTSELF = 5;
	public static final int RETURN_PERMITTED_SELF = 6;
	public static final int RETURN_REFUSED_SELF = 7;

	private InformState curState = null;

	public static final int EMPTY_STATUS = -1;

	public static Map<String, Object> getTextByType(int type,
			Map<String, Object> item) {
		Log.i("Inform type", Integer.toString(type));
		String title = "", content = "", confirm = "", cancel = "";
		try {
			JSONObject obj = new JSONObject((String) item.get("description"));

			switch (type) {
			case BORROW_UNPROCESSED_SELF:
				title = "未确认:";
				content = "向" + (String) item.get("to") + "借书;  " + "Message:"
						+ obj.getString("message");
				break;
			case BORROW_UNPROCESSED_NOTSELF:
				title = "请求:";
				content = "来自" + (String) item.get("from") + "的借书请求:"
						+ obj.getString("message");
				confirm = "同意";
				cancel = "拒绝";
				break;
			case BORROW_PERMITTED_SELF:
				title = "对方已允许:";
				content = (String) item.get("to") + "实在是太好人了";
				confirm = "确认";
				break;
			case BORROW_REFUSED_SELF:
				title = "逗逼,你被拒绝了:";
				content = (String) item.get("to") + "实在太残忍了";
				confirm = "确认";
				break;
			case RETURN_UNPROCESSED_SELF:
				title = "未确认:";
				content = "向" + (String) item.get("to") + "还书;  " + "Message:"
						+ obj.getString("message");
				break;
			case RETURN_UNPROCESSED_NOTSELF:
				title = "未处理:";
				content = "来自" + (String) item.get("from") + "的还书请求:"
						+ obj.getString("message");
				confirm = "已还";
				cancel = "未还";
				break;
			case RETURN_PERMITTED_SELF:
				title = "对方已确认:";
				content = "来自:" + (String) item.get("to");
				confirm = "确认";
				break;
			case RETURN_REFUSED_SELF:
				title = "还书失败:";
				content = "来自:" + (String) item.get("to");
				confirm = "确认";
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", title);
		map.put("content", content);
		map.put("confirm", confirm);
		map.put("cancel", cancel);
		return map;
	}

	public static Map<String, Object> objToSend(JSONObject item)
			throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();
		int id = item.getInt("id");
		String time = item.getString("time");
		String from = item.getString("from");
		String to = item.getString("to");
		int type = item.getInt("type");
		String description = item.getString("description");
		int status = item.getInt("status");

		map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("time", time);
		map.put("from", from);
		map.put("to", to);
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
		String to = item.getString("to");
		int type = item.getInt("type");
		String description = item.getString("description");
		int status = item.getInt("status");

		map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("time", time);
		map.put("from", from);
		map.put("to", to);
		map.put("type", type);
		map.put("description", description);
		map.put("status", status);

		return map;
	}

	public void setState(Map<String, Object> item, String username) {
		curState = getCurState(item, username);
	}

	public void setView(ViewHolder views, int position) {
		curState.setView(views, position);
	}

	public int getNextConfirmStatus() {
		return curState.getNextConfirmStatus();
	}

	public int getNextCancelStatus() {
		return curState.getNextCancelStatus();
	}

	public abstract class InformState {
		protected OnClickListener listenerConfirm;
		protected OnClickListener listenerCancel;
		protected Map<String, Object> showingData;

		public InformState(Map<String, Object> showingData) {
			this.showingData = showingData;
		}

		public void setView(ViewHolder holder, int position) {
			holder.title.setText((String) showingData.get("title"));
			holder.content.setText((String) showingData.get("content"));
			holder.confirm.setText((String) showingData.get("confirm"));
			holder.cancel.setText((String) showingData.get("cancel"));
		}

		public abstract int getNextConfirmStatus();

		public abstract int getNextCancelStatus();
	}

	public class UnprocessFromState extends InformState {

		public UnprocessFromState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public void setView(ViewHolder holder, int position) {
			super.setView(holder, position);
			holder.confirm.setVisibility(View.INVISIBLE);
			holder.cancel.setVisibility(View.INVISIBLE);
		}

		@Override
		public int getNextConfirmStatus() {
			return EMPTY_STATUS;
		}

		@Override
		public int getNextCancelStatus() {
			return EMPTY_STATUS;
		}

	}

	public class UnprocessToState extends InformState {

		public UnprocessToState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public void setView(ViewHolder holder, int position) {
			super.setView(holder, position);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_PERMITTED;
		}

		@Override
		public int getNextCancelStatus() {
			// TODO Auto-generated method stub
			return REQUEST_STATUS_REFUSED;
		}
	}

	public class PermittedFromState extends InformState {

		public PermittedFromState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public void setView(ViewHolder holder, int position) {
			super.setView(holder, position);
			holder.cancel.setVisibility(View.INVISIBLE);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_CONFIRM;
		}

		@Override
		public int getNextCancelStatus() {
			return EMPTY_STATUS;
		}

	}

	public class RefusedFromState extends InformState {

		public RefusedFromState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public void setView(ViewHolder holder, int position) {
			super.setView(holder, position);
			holder.cancel.setVisibility(View.INVISIBLE);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_CONFIRM;
		}

		@Override
		public int getNextCancelStatus() {
			return EMPTY_STATUS;
		}
	}

	private InformState getCurState(Map<String, Object> item, String username) {
		Map<String, Object> map = new HashMap<String, Object>();
		switch ((Integer) item.get("status")) {
		case Inform.REQUEST_STATUS_UNPROCESSED:
			if (username.equals(item.get("from"))) {
				switch ((Integer) item.get("type")) {
				case Inform.REQUEST_TYPE_BORROW:
					map = Inform.getTextByType(Inform.BORROW_UNPROCESSED_SELF,
							item);
					break;
				case Inform.REQUEST_TYPE_RETURN:
					map = Inform.getTextByType(Inform.RETURN_UNPROCESSED_SELF,
							item);
					break;
				}
				return new UnprocessFromState(map);
			} else {
				switch ((Integer) item.get("type")) {
				case Inform.REQUEST_TYPE_BORROW:
					map = Inform.getTextByType(
							Inform.BORROW_UNPROCESSED_NOTSELF, item);
					break;
				case Inform.REQUEST_TYPE_RETURN:
					map = Inform.getTextByType(
							Inform.RETURN_UNPROCESSED_NOTSELF, item);
					break;
				}
				return new UnprocessToState(map);
			}
		case Inform.REQUEST_STATUS_PERMITTED: {
			switch ((Integer) item.get("type")) {
			case Inform.REQUEST_TYPE_BORROW:
				map = Inform.getTextByType(Inform.BORROW_PERMITTED_SELF, item);
				break;
			case Inform.REQUEST_TYPE_RETURN:
				map = Inform.getTextByType(Inform.RETURN_PERMITTED_SELF, item);
				break;
			}
			return new PermittedFromState(map);
		}
		case Inform.REQUEST_STATUS_REFUSED: {
			switch ((Integer) item.get("type")) {
			case Inform.REQUEST_TYPE_BORROW:
				map = Inform.getTextByType(Inform.BORROW_REFUSED_SELF, item);
				break;
			case Inform.REQUEST_TYPE_RETURN:
				map = Inform.getTextByType(Inform.RETURN_REFUSED_SELF, item);
				break;
			}
			return new RefusedFromState(map);
		}
		}
		return null;
	}
}
