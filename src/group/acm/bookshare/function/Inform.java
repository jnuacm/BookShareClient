package group.acm.bookshare.function;

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
	public static final int REQUEST_TYPE_ADDFRIEND = 3;

	public static final int REQUEST_STATUS_UNPROCESSED = 0;
	public static final int REQUEST_STATUS_PERMITTED = 1;
	public static final int REQUEST_STATUS_REFUSED = 2;
	public static final int REQUEST_STATUS_CONFIRM = 3;
	public static final int REQUEST_STATUS_CANCEL = 4;

	public static final int BORROW_UNPROCESSED_SELF = 0;
	public static final int BORROW_UNPROCESSED_NOTSELF = 1;
	public static final int BORROW_PERMITTED_SELF = 2;
	public static final int BORROW_PERMITTED_NOTSELF = 3;
	public static final int BORROW_REFUSED_SELF = 4;
	public static final int RETURN_UNPROCESSED_SELF = 6;
	public static final int RETURN_UNPROCESSED_NOTSELF = 7;
	public static final int RETURN_PERMITTED_SELF = 8;
	public static final int RETURN_PERMITTED_NOTSELF = 9;
	public static final int RETURN_REFUSED_SELF = 10;
	public static final int ADDFRIEND_UNPROCESSED_SELF = 12;
	public static final int ADDFRIEND_UNPROCESSED_NOTSELF = 13;
	public static final int ADDFRIEND_PERMITTED_SELF = 14;
	public static final int ADDFRIEND_PERMITTED_NOTSELF = 15;
	public static final int ADDFRIEND_REFUSED_SELF = 16;

	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String CONFIRM = "confirm";
	public static final String CANCEL = "cancel";
	public static final String CONFIRM_VISIBILITY = "confirm_visibility";
	public static final String CANCEL_VISIBILITY = "cancel_visibility";

	private InformState curState = null;

	public static final int EMPTY_STATUS = -1;

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

	public boolean stateIsNull() {
		if (curState == null)
			return true;
		return false;
	}

	public Map<String, Object> getTextByType(int type, Map<String, Object> item) {
		Log.i("Inform type", Integer.toString(type));
		String title = "", content = "", confirm = "", cancel = "";
		int confirmVisibility = View.INVISIBLE;
		int cancelVisibility = View.INVISIBLE;
		try {
			JSONObject obj = new JSONObject((String) item.get("description"));

			switch (type) {
			case BORROW_UNPROCESSED_SELF:
				title = "借书未确认:" + (String) item.get("from");
				content = "向" + (String) item.get("to") + "借书;  " + "Message:"
						+ obj.getString("message");
				cancel = "取消";
				confirmVisibility = View.INVISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case BORROW_UNPROCESSED_NOTSELF:
				title = "借书未处理请求:" + (String) item.get("to");
				content = "来自" + (String) item.get("from") + "的借书请求:"
						+ obj.getString("message");
				confirm = "同意";
				cancel = "拒绝";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case BORROW_PERMITTED_SELF:
				title = "借书对方已允许:" + (String) item.get("from");
				content = (String) item.get("to") + "实在是太好人了";
				confirm = "显码";
				cancel = "取消";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case BORROW_PERMITTED_NOTSELF:
				title = "借书我已允许:" + (String) item.get("to");
				content = (String) item.get("to") + "实在是太好人了";
				confirm = "扫码";
				cancel = "取消";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case BORROW_REFUSED_SELF:
				title = "借书，被拒绝了:" + (String) item.get("from");
				content = (String) item.get("to") + "实在太残忍了";
				confirm = "确认";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.INVISIBLE;
				break;
			case RETURN_UNPROCESSED_SELF:
				title = "还书未确认:" + (String) item.get("from");
				content = "向" + (String) item.get("to") + "还书;  " + "Message:"
						+ obj.getString("message");
				cancel = "取消";
				confirmVisibility = View.INVISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case RETURN_UNPROCESSED_NOTSELF:
				title = "还书未处理:" + (String) item.get("to");
				content = "来自" + (String) item.get("from") + "的还书请求:"
						+ obj.getString("message");
				confirm = "已还";
				cancel = "未还";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case RETURN_PERMITTED_SELF:
				title = "还书对方已确认:" + (String) item.get("from");
				content = "来自:" + (String) item.get("to");
				confirm = "扫码";
				cancel = "取消";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case RETURN_PERMITTED_NOTSELF:
				title = "还书我已确认:" + (String) item.get("to");
				content = "来自:" + (String) item.get("to");
				confirm = "显码";
				cancel = "取消";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case RETURN_REFUSED_SELF:
				title = "还书失败:" + (String) item.get("from");
				content = "来自:" + (String) item.get("to");
				confirm = "确认";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.INVISIBLE;
				break;
			case ADDFRIEND_UNPROCESSED_SELF:
				title = "加好友未处理：" + (String) item.get("from");
				content = "我是发送方";
				confirmVisibility = View.INVISIBLE;
				cancelVisibility = View.INVISIBLE;
				break;
			case ADDFRIEND_UNPROCESSED_NOTSELF:
				title = "加好友未处理" + (String) item.get("to");
				content = "我是接收方";
				confirm = "同意";
				cancel = "拒绝";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.VISIBLE;
				break;
			case ADDFRIEND_PERMITTED_SELF:
				title = "加好友同意" + (String) item.get("from");
				content = "对方已同意";
				confirm = "确认";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.INVISIBLE;
				break;
			case ADDFRIEND_REFUSED_SELF:
				title = "加好友拒绝" + (String) item.get("to");
				content = "对方已拒绝";
				confirm = "确认";
				confirmVisibility = View.VISIBLE;
				cancelVisibility = View.INVISIBLE;
				break;

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TITLE, title);
		map.put(CONTENT, content);
		map.put(CONFIRM, confirm);
		map.put(CANCEL, cancel);
		map.put(CONFIRM_VISIBILITY, confirmVisibility);
		map.put(CANCEL_VISIBILITY, cancelVisibility);
		return map;
	}

	public void setState(Map<String, Object> item, String username) throws JSONException {
		curState = getCurState(item, username);
	}

	public Map<String, Object> getContent() {
		return curState.getContent();
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

		public Map<String, Object> getContent() {
			return showingData;
		}

		public abstract int getNextConfirmStatus();

		public abstract int getNextCancelStatus();
	}

	public class UnprocessFromState extends InformState {

		public UnprocessFromState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public int getNextConfirmStatus() {
			return EMPTY_STATUS;
		}

		@Override
		public int getNextCancelStatus() {
			return Inform.REQUEST_STATUS_CANCEL;
		}

	}

	public class UnprocessToState extends InformState {

		public UnprocessToState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_PERMITTED;
		}

		@Override
		public int getNextCancelStatus() {
			return REQUEST_STATUS_REFUSED;
		}
	}

	public class PermittedFromState extends InformState {

		public PermittedFromState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_CONFIRM;
		}

		@Override
		public int getNextCancelStatus() {
			return REQUEST_STATUS_CANCEL;
		}

	}

	public class RefusedFromState extends InformState {

		public RefusedFromState(Map<String, Object> data) {
			super(data);
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

	public class RefusedToState extends InformState {

		public RefusedToState(Map<String, Object> data) {
			super(data);
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

	public class PermittedToState extends InformState {

		public PermittedToState(Map<String, Object> data) {
			super(data);
		}

		@Override
		public int getNextConfirmStatus() {
			return REQUEST_STATUS_CONFIRM;
		}

		@Override
		public int getNextCancelStatus() {
			return REQUEST_STATUS_CANCEL;
		}

	}

	private InformState getCurState(Map<String, Object> item, String username) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject obj = new JSONObject((String) item.get("description"));
		obj.getInt("bookid");
		switch ((Integer) item.get("status")) {
		case Inform.REQUEST_STATUS_UNPROCESSED:
			// /////////////////////////////////////////
			switch ((Integer) item.get("type")) {
			case Inform.REQUEST_TYPE_BORROW:
				if (username.equals(item.get("from"))) {
					map = Inform.this.getTextByType(
							Inform.BORROW_UNPROCESSED_SELF, item);
					return new UnprocessFromState(map);
				} else {
					map = Inform.this.getTextByType(
							Inform.BORROW_UNPROCESSED_NOTSELF, item);
					return new UnprocessToState(map);
				}
			case Inform.REQUEST_TYPE_RETURN:
				if (username.equals(obj.getString("holder"))) {
					map = Inform.this.getTextByType(
							Inform.RETURN_UNPROCESSED_SELF, item);
					return new UnprocessFromState(map);
				} else {
					map = Inform.this.getTextByType(
							Inform.RETURN_UNPROCESSED_NOTSELF, item);
					return new UnprocessToState(map);
				}
			case Inform.REQUEST_TYPE_ADDFRIEND:
				if (username.equals(item.get("from"))) {
					map = Inform.this.getTextByType(
							Inform.ADDFRIEND_UNPROCESSED_SELF, item);
					return new UnprocessFromState(map);
				} else {
					map = Inform.this.getTextByType(
							Inform.ADDFRIEND_UNPROCESSED_NOTSELF, item);
					return new UnprocessToState(map);
				}
			}

			// ///////////////////////////////////////////
			/*
			 * if (username.equals(item.get("from"))) { switch ((Integer)
			 * item.get("type")) { case Inform.REQUEST_TYPE_BORROW: map =
			 * Inform.this.getTextByType( Inform.BORROW_UNPROCESSED_SELF, item);
			 * break; case Inform.REQUEST_TYPE_RETURN: map =
			 * Inform.this.getTextByType( Inform.RETURN_UNPROCESSED_SELF, item);
			 * break; case Inform.REQUEST_TYPE_ADDFRIEND: map =
			 * Inform.this.getTextByType( Inform.ADDFRIEND_UNPROCESSED_SELF,
			 * item); break; } return new UnprocessFromState(map);
			 * 
			 * } else { switch ((Integer) item.get("type")) { case
			 * Inform.REQUEST_TYPE_BORROW: map = Inform.this.getTextByType(
			 * Inform.BORROW_UNPROCESSED_NOTSELF, item); break; case
			 * Inform.REQUEST_TYPE_RETURN: map = Inform.this.getTextByType(
			 * Inform.RETURN_UNPROCESSED_NOTSELF, item); break; case
			 * Inform.REQUEST_TYPE_ADDFRIEND: map = Inform.this.getTextByType(
			 * Inform.ADDFRIEND_UNPROCESSED_NOTSELF, item); break; } return new
			 * UnprocessToState(map); } //
			 * ///////////////////////////////////////////////////////////
			 */
		case Inform.REQUEST_STATUS_PERMITTED: {
			switch ((Integer) item.get("type")) {
			case Inform.REQUEST_TYPE_BORROW:

				if (username.equals(item.get("from"))) {
					map = Inform.this.getTextByType(
							Inform.BORROW_PERMITTED_SELF, item);
					return new PermittedFromState(map);
				} else {
					map = Inform.this.getTextByType(
							Inform.BORROW_PERMITTED_NOTSELF, item);
					return new PermittedToState(map);
				}
			case Inform.REQUEST_TYPE_RETURN:

				if (username.equals(obj.getString("holder"))) {
					map = Inform.this.getTextByType(
							Inform.RETURN_PERMITTED_SELF, item);
					return new PermittedFromState(map);
				} else {
					map = Inform.this.getTextByType(
							Inform.RETURN_PERMITTED_NOTSELF, item);
					return new PermittedToState(map);
				}
			case Inform.REQUEST_TYPE_ADDFRIEND:
				map = Inform.this.getTextByType(
						Inform.ADDFRIEND_PERMITTED_SELF, item);
				return new PermittedFromState(map);
			}
		}
		case Inform.REQUEST_STATUS_REFUSED: {
			switch ((Integer) item.get("type")) {
			case Inform.REQUEST_TYPE_BORROW:
				map = Inform.this.getTextByType(Inform.BORROW_REFUSED_SELF,
						item);
				break;
			case Inform.REQUEST_TYPE_RETURN:
				map = Inform.this.getTextByType(Inform.RETURN_REFUSED_SELF,
						item);
				break;
			case Inform.REQUEST_TYPE_ADDFRIEND:
				map = Inform.this.getTextByType(Inform.ADDFRIEND_REFUSED_SELF,
						item);
				break;
			}
			return new RefusedFromState(map);
		}
		}
		return null;
	}
}
