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
	public static final int TYPE_SUM = 3; // 状态数量

	public static final int REQUEST_STATUS_UNPROCESSED = 0;
	public static final int REQUEST_STATUS_PERMITTED = 1;
	public static final int REQUEST_STATUS_REFUSED = 2;
	public static final int REQUEST_STATUS_CONFIRM = 3;
	public static final int REQUEST_STATUS_CANCEL = 4;
	public static final int STATUS_SUM = 5; // 状态数量

	public static final int SELF = 1;
	public static final int NOT_SELF = 0;
	public static final int IDENTITY_SUM = 2; // 状态数量
	
	public static final int READ = 1;
	public static final int NOT_READ = 0;
	public static final int READ_SUM = 2; // 状态数量
	
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String CONFIRM = "confirm";
	public static final String CANCEL = "cancel";
	public static final String CONFIRM_VISIBILITY = "confirm_visibility";
	public static final String CANCEL_VISIBILITY = "cancel_visibility";

	public static final String[] confirmButton =
		{
		"", "同意", "显码", "扫码", "确认", "", "确认", "", "", "",
		"", "已还", "扫码", "显码", "确认", "", "", "确认", "", "",
		"", "同意", "确认", "", "确认", "", "", "", "", ""};
	public static final int[] confirmVisibility = 
		{
		4, 0, 0, 0, 0, 4, 0, 4, 4, 4,
		4, 0, 0, 0, 0, 4, 4, 0, 4, 4,
		4, 0, 0, 4, 0, 4, 4, 4, 4, 4};
	public static final String[] cancelButton =
		{
		"取消", "拒绝", "取消", "取消", "", "", "", "", "", "",
		"取消", "未还", "取消", "取消", "", "", "", "", "", "",
		"取消", "拒绝", "", "", "", "", "", "", "", "" };
	public static final int[] cancelVisibility =
		{
		0, 0, 0, 0, 4, 4, 4, 4, 4, 4,
		0, 0, 0, 0, 4, 4, 4, 4, 4, 4,
		0, 0, 4, 4, 4, 4, 4, 4, 4, 4};

	private Map<String, Object> inform;
	private User localUser;
	public int type;
	public int status;
	public int isSelf;
	public int isRead;
	public int state;

	private ButtonsAction action;

	public interface ButtonsAction {
		public abstract void permitted();

		public abstract void showCode();

		public abstract void scanCode();

		public abstract void readConfirm();

		public abstract void confirm();

		public abstract void refused();

		public abstract void cancel();
	}

	public Inform(Map<String, Object> inform, User localUser,
			ButtonsAction action) throws JSONException {
		this.inform = inform;
		this.localUser = localUser;
		this.action = action;
		type = (Integer) inform.get("type");
		status = (Integer) inform.get("status");
		isRead = 0;//(Integer) inform.get("read");
		isSelf = getIdentityJudge().isSelf(inform, localUser);
		int posSelf = (isSelf == SELF) ? 0 : 1;
		state = isRead * STATUS_SUM * IDENTITY_SUM * READ_SUM
				+ (type - 1) * STATUS_SUM * IDENTITY_SUM
				+ status * IDENTITY_SUM
				+ posSelf;
	}

	private IdentityJudge getIdentityJudge() {
		switch (type) {
		case Inform.REQUEST_TYPE_BORROW:
			return new IdentityJudge();
		case Inform.REQUEST_TYPE_RETURN:
			return new ReturnIdentityJudge();
		case Inform.REQUEST_TYPE_ADDFRIEND:
			return new IdentityJudge();
		}
		return null;
	}

	private class IdentityJudge {
		public int isSelf(Map<String, Object> inform, User localUser) {
			if (localUser.getUserName().equals(inform.get("from")))
				return SELF;
			else
				return NOT_SELF;
		}
	}

	private class ReturnIdentityJudge extends IdentityJudge {
		public int isSelf(Map<String, Object> inform, User localUser) {
			try {
				JSONObject obj = new JSONObject(
						(String) inform.get("description"));
				int id = obj.getInt("bookid");
				for (Map<String, Object> book : localUser.getBookListData()) {
					if ((Integer) book.get("id") == id) {
						if (localUser.getUserName().equals(book.get("holder"))) {
							return SELF;
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return NOT_SELF;
		}
	}

	public Map<String, Object> getText() {
		Log.i("Inform.java 145:","Inform state:"+Integer.toString(state));
		String title = "", content = "", notself = "", message = "";

		try {
			JSONObject obj;
			obj = new JSONObject((String) inform.get("description"));
			message = obj.getString("message");

			switch (type) {
			case Inform.REQUEST_TYPE_BORROW:
				title = "借书请求:";
				message = "借书," + obj.getString("bookname") + ",message:"
						+ message;
				if (isSelf == SELF) {
					notself = obj.getString("owner");
				} else {
					notself = (String) inform.get("from");
				}
				break;
			case Inform.REQUEST_TYPE_RETURN:
				title = "还书请求:";
				message = "接受还书," + obj.getString("bookname") + ",message:"
						+ message;
				if (isSelf == SELF) {
					notself = obj.getString("owner");
				} else {
					notself = obj.getString("holder");
				}
				break;
			case Inform.REQUEST_TYPE_ADDFRIEND:
				title = "加好友:";
				message = "加好友,message:" + message;
				if (isSelf == SELF) {
					notself = obj.getString("to");
				} else {
					notself = obj.getString("from");
				}
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		switch (status) {
		case Inform.REQUEST_STATUS_UNPROCESSED:
			content = "未处理:";
			break;
		case Inform.REQUEST_STATUS_PERMITTED:
			content = "同意:";
			break;
		case Inform.REQUEST_STATUS_REFUSED:
			content = "拒绝:";
			break;
		case Inform.REQUEST_STATUS_CONFIRM:
			content = "已完成:";
			break;
		case Inform.REQUEST_STATUS_CANCEL:
			content = "取消:";
			break;
		}

		if (isSelf == SELF) {
			content += "请求" + notself;
		} else {
			content += notself + "请求";
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TITLE, title);
		map.put(CONTENT, content);
		return map;
	}

	public Map<String, Object> getButtonShow() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put(CONFIRM, confirmButton[state]);
		ret.put(CONFIRM_VISIBILITY, confirmButton[state]);
		ret.put(CANCEL, cancelButton[state]);
		ret.put(CANCEL_VISIBILITY, cancelButton[state]);
		return ret;
	}

	public void clickConfirm() {
		switch(state){
		case 0:break;
		case 1:action.permitted();break;
		case 2:action.showCode();break;
		case 3:action.scanCode();break;
		case 4:action.readConfirm();break;
		case 5:break;
		case 6:action.readConfirm();break;
		case 7:break;
		case 8:break;
		case 9:break;
		case 10:break;
		case 11:action.permitted();break;
		case 12:action.scanCode();break;
		case 13:action.showCode();break;
		case 14:action.readConfirm();break;
		case 15:break;
		case 16:break;
		case 17:action.readConfirm();break;
		case 18:break;
		case 19:break;
		case 20:break;
		case 21:action.permitted();break;
		case 22:action.confirm();break;
		case 23:break;
		case 24:action.readConfirm();break;
		case 25:break;
		case 26:break;
		case 27:break;
		case 28:break;
		case 29:break;
		}
	}

	public void clickCancel() {
		switch(state){
		case 0:action.cancel();break;
		case 1:action.refused();break;
		case 2:action.cancel();break;
		case 3:action.cancel();break;
		case 4:break;
		case 5:break;
		case 6:break;
		case 7:break;
		case 8:break;
		case 9:break;
		case 10:action.cancel();break;
		case 11:action.refused();break;
		case 12:action.cancel();break;
		case 13:action.cancel();break;
		case 14:break;
		case 15:break;
		case 16:break;
		case 17:break;
		case 18:break;
		case 19:break;
		case 20:action.cancel();break;
		case 21:action.refused();break;
		case 22:break;
		case 23:break;
		case 24:break;
		case 25:break;
		case 26:break;
		case 27:break;
		case 28:break;
		case 29:break;
		}
	}

	public boolean isRead() {
		if (isRead == Inform.READ)
			return true;
		else
			return false;
	}
	
	public static Map<String, Object> objToInform(JSONObject item)
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
	
	public boolean showThisInform() {
		if (state > 29)
			return false;
		switch(state){
		/////confirm过滤///////
		case 6:
		case 17:
		/////////////
		case 5:
		case 7:
		case 8:
		case 9:
		case 15:
		case 16:
		case 18:
		case 19:
		case 23:
		case 25:
		case 26:
		case 27:
		case 28:
		case 29:
		case 30:return false;
		}
		return true;
	}
}
