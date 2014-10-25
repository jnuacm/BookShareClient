package group.acm.bookshare;

import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.PageListAdapter;
import group.acm.bookshare.function.User;
import group.acm.bookshare.function.http.HttpProcessBase;
import group.acm.bookshare.function.http.NetProgress;
import group.acm.bookshare.util.Utils;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InformListManage {
	private ListView informlistview;
	private InformListAdapter informAdapter;

	private MainActivity activity;
	private User localUser;

	public InformListManage(MainActivity activity) {
		this.activity = activity;
		localUser = ((LocalApp) activity.getApplication()).getUser();
	}

	public View getView() {
		return informlistview;
	}

	public void initInformList() {
		List<Map<String, Object>> informList = localUser.getInformListData();

		informAdapter = new InformListAdapter(activity, informList);

		informlistview = (ListView) LayoutInflater.from(activity).inflate(
				R.layout.activity_submain_inform, null);
		informlistview.setVerticalFadingEdgeEnabled(false);

		informlistview.setOnItemClickListener(new InformClickListener());

		informlistview.setAdapter(informAdapter);
		informlistview.setOnScrollListener(informAdapter);
	}

	public NetProgress getConfirmProgress(int id) {
		return new ConfirmInformProgress(id);
	}

	private class ConfirmInformProgress extends HttpProcessBase {
		int id;

		public ConfirmInformProgress(int id) {
			this.id = id;
		}

		public void error(String content) {
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusError(String response) {
			String content = "失败:" + response;
			Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
		}

		@Override
		public void statusSuccess(String response) {
			reload();
		}
	}

	public void updateInformData() {
		informAdapter.notifyDataSetChanged();
	}

	private class InformClickListener implements OnItemClickListener {
		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Map<String, Object> item = (Map<String, Object>) parent
					.getItemAtPosition(position);
			if (item == null)
				return;
			new AlertDialog.Builder(activity).setTitle("Message")
					.setMessage(localUser.informMapToStr(item))
					.setPositiveButton("确认", null).show();
		}

	}

	private class SendInformProgress extends HttpProcessBase {

		@Override
		public void statusError(String response) {
		}

		@Override
		public void statusSuccess(String response) {
			localUser.addInformDataToList(response);
			localUser.getReceiveInformList(new ReceiveInformProgress());
		}
	}

	private class ReceiveInformProgress extends HttpProcessBase {
		@Override
		public void statusError(String response) {
		}

		@Override
		public void statusSuccess(String response) {
			localUser.addInformDataToList(response);
			informAdapter.initViewItemSize();
			updateDisplay();
			Utils.setHasUpdate(activity, false);
		}
	}

	public class InformListAdapter extends PageListAdapter {
		List<Map<String, Object>> informs;
		Context context;

		public InformListAdapter(Context context, List<Map<String, Object>> data) {
			this.context = context;
			informs = data;
		}
		
		@Override
		public void loadData() {
		}

		@Override
		public int getCount() {
			if (curViewSize > informs.size())
				curViewSize = informs.size();
			return curViewSize;
		}

		@Override
		public Object getItem(int position) {
			return informs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public class PermittedProgress extends HttpProcessBase {
			public void error(String content) {
				Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusError(String response) {
				Toast.makeText(activity, response, Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusSuccess(String response) {
				String tmp = "完成";
				Toast.makeText(activity, tmp, Toast.LENGTH_LONG).show();
			}
		}

		public class ViewHolder {
			public TextView title;
			public TextView content;
			public Button confirm;
			public Button cancel;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder views;
			if (convertView == null) {
				views = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.myinform_listview_item, null);
				views.title = (TextView) convertView
						.findViewById(R.id.informlistviewitem_title);
				views.content = (TextView) convertView
						.findViewById(R.id.informlistviewitem_content);
				views.confirm = (Button) convertView
						.findViewById(R.id.informlistviewitem_confirm);
				views.cancel = (Button) convertView
						.findViewById(R.id.informlistviewitem_cancel);
				convertView.setTag(views);
			} else {
				views = (ViewHolder) convertView.getTag();
			}

			Map<String, Object> item = informs.get(position);

			Inform inform = null;
			try {
				inform = new Inform(item, localUser, new InformAction(item));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Map<String, Object> textData = inform.getText();
			views.title.setText((String) textData.get(Inform.TITLE));
			views.content.setText((String) textData.get(Inform.CONTENT));

			Map<String, Object> buttonData = inform.getButtonShow();

			views.confirm.setText((String) buttonData.get(Inform.CONFIRM));
			views.cancel.setText((String) buttonData.get(Inform.CANCEL));
			views.confirm.setVisibility((Integer) buttonData
					.get(Inform.CONFIRM_VISIBILITY));
			views.cancel.setVisibility((Integer) buttonData
					.get(Inform.CANCEL_VISIBILITY));

			views.confirm.setOnClickListener(new ConfirmClickListener(inform));
			views.cancel.setOnClickListener(new CancelClickListener(inform));

			return convertView;
		}

		private class ConfirmClickListener implements OnClickListener {
			private Inform inform;

			public ConfirmClickListener(Inform inform) {
				this.inform = inform;
			}

			@Override
			public void onClick(View v) {
				if (Utils.isQuickClick())
					return;
				inform.clickConfirm();
			}
		}

		private class CancelClickListener implements OnClickListener {
			private Inform inform;

			public CancelClickListener(Inform inform) {
				this.inform = inform;
			}

			@Override
			public void onClick(View v) {
				if (Utils.isQuickClick())
					return;
				inform.clickCancel();
			}
		}

		private class InformAction implements Inform.ButtonsAction {
			private Map<String, Object> item;

			public InformAction(Map<String, Object> item) {
				this.item = item;
			}

			@Override
			public void permitted() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_PERMITTED,
						new PermittedProgress());
				reload();
			}

			@Override
			public void permittedAndRefreshFriend() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_PERMITTED,
						new PermittedProgress());
				reload();
				activity.friendListReload();
			}

			@Override
			public void showCode() {
				Intent intent = new Intent(activity,
						GenerateQRCodeActivity.class);
				intent.putExtra("ContentString", (Integer) item.get(Inform.ID)
						+ "");
				activity.startActivityForResult(intent,
						Utils.ACTIVITY_REQUEST_SHOWCODE);
			}

			@Override
			public void scanCode() {
				Intent openCameraIntent = new Intent(activity,
						CaptureActivity.class);
				activity.startActivityForResult(openCameraIntent,
						Utils.REQUEST_SCANBOOK_UPDATESTATUS);
			}

			@Override
			public void refused() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_REFUSED, new PermittedProgress());
				reload();
			}

			@Override
			public void delete() {
				localUser.deleteRequest((Integer) item.get(Inform.ID),
						new PermittedProgress());
				reload();
			}

			@Override
			public void deleteAndRefreshFriend() {
				localUser.deleteRequest((Integer) item.get(Inform.ID),
						new PermittedProgress());
				reload();
				activity.friendListReload();
			}

			@Override
			public void cancel() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_CANCEL, new PermittedProgress());
				reload();
			}
		}
	}

	public void reload() {
		localUser.clearInformData();
		localUser.getSendInformList(new SendInformProgress());
	}

	public void updateDisplay() {
		informAdapter.notifyDataSetChanged();
	}
}
