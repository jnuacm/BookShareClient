package group.acm.bookshare;

import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class PersonInfoActivity extends Activity {
	public static final int REQUEST_FILE_SELECT = 1;

	private User localUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_info);
		LocalApp app = (LocalApp) getApplication();
		localUser = app.getUser();
		setAction();
		setInformation();
	}

	private void setAction() {
		ImageView avatar = (ImageView) findViewById(R.id.personal_avatar_view);
		avatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);

				try {
					startActivityForResult(Intent.createChooser(intent,
							"Select a File to Upload"), REQUEST_FILE_SELECT);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(PersonInfoActivity.this, "请安装文件浏览器",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	private void setInformation() {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.person_info, menu);
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FILE_SELECT:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				String path = Utils.getPath(this, uri);
				localUser.createAvatar(path,
						HttpProcessBase.createShowProgress(this, "成功", "失败"));
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
