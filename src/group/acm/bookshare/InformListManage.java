package group.acm.bookshare;

import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.Inform;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.NetAccess;
import group.acm.bookshare.function.NetProgress;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
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

		informlistview.addHeaderView(getHeadView());

		setListener();

		informlistview.setAdapter(informAdapter);
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
			localUser.deleteInformById(id);
			localUser.getBookList(new HttpProcessBase() {
				@Override
				public void statusError(String response) {
					String content = "失败:" + response;
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				}

				@Override
				public void statusSuccess(String response) {
					localUser.clearBookData();
					localUser.addBookDataToList(response);
					updateInformData();
					String content = "成功";
					Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	public void updateInformData() {
		informAdapter.notifyDataSetChanged();
	}

	private void setListener() {
		informlistview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position != 0) {
					new AlertDialog.Builder(activity)
							.setTitle("Message")
							.setMessage(
									localUser.getInformString(localUser
											.getInformListData().get(
													position - 1)))
							.setPositiveButton("确认", null).show();
				}
			}
		});
	}

	private View getHeadView() {
		View head = LayoutInflater.from(activity).inflate(
				R.layout.inform_listview_top, null);
		Button refresh = (Button) head.findViewById(R.id.button_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reload();
			}
		});

		Button button_scan = (Button) head.findViewById(R.id.button_scan);
		button_scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(activity,
						CaptureActivity.class);
				activity.startActivityForResult(openCameraIntent,
						Utils.REQUEST_SCANBOOK_UPDATESTATUS);

			}
		});

		Button button3 = (Button) head.findViewById(R.id.button3);
		button3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity,
						GenerateQRCodeActivity.class);
				intent.putExtra("key", "key");
				activity.startActivity(intent);
			}
		});

		return head;
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
			localUser.addInformDataToList(response);
			informAdapter.notifyDataSetChanged();
			Utils.setHasUpdate(activity, false);
		}

		@Override
		public void statusSuccess(String response) {
		}
	}

	public class InformListAdapter extends BaseAdapter {
		List<Map<String, Object>> informs;
		Context context;

		public InformListAdapter(Context context, List<Map<String, Object>> data) {
			this.context = context;
			informs = data;
		}

		@Override
		public int getCount() {
			return informs.size();
		}

		@Override
		public Object getItem(int position) {
			return informs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public class RequestProgress extends HttpProcessBase {
			private int position;

			public RequestProgress(int position) {
				this.position = position;
			}

			public void error(String content) {
				Toast.makeText(activity, content, Toast.LENGTH_LONG).show();
			}

			@Override
			public void statusError(String response) {
			}

			@Override
			public void statusSuccess(String response) {
				String tmp = "完成";
				Toast.makeText(activity, tmp, Toast.LENGTH_LONG).show();
				localUser.getBookList(new HttpProcessBase() {
					public void error(String content) {
						Toast.makeText(activity, content, Toast.LENGTH_LONG)
								.show();
					}

					@Override
					public void statusError(String response) {
					}

					@Override
					public void statusSuccess(String response) {
						localUser.clearBookData();
						localUser.addBookDataToList(response);
						reload();
					}
				});
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
						R.layout.inform_listview_item, null);
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

			int id = (Integer) item.get(Inform.ID);

			Inform inform = null;
			try {
				inform = new Inform(item, localUser, new InformAction(item,
						position));
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
				inform.clickCancel();
			}
		}

		private class InformAction implements Inform.ButtonsAction {
			private Map<String, Object> item;
			private int position;

			public InformAction(Map<String, Object> item, int position) {
				this.item = item;
				this.position = position;
			}

			@Override
			public void permitted() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_PERMITTED, new RequestProgress(
								position));
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
			public void readConfirm() {
				localUser.updateRead((Integer) item.get(Inform.ID), true,
						new RequestProgress(position));
			}

			@Override
			public void refused() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_REFUSED, new RequestProgress(
								position));
			}

			@Override
			public void cancel() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_CANCEL, new RequestProgress(
								position));
			}

			@Override
			public void confirm() {
				localUser.updateRequest((Integer) item.get(Inform.ID),
						Inform.REQUEST_STATUS_CONFIRM, new RequestProgress(
								position));
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
