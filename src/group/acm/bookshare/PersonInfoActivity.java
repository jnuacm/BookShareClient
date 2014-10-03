package group.acm.bookshare;

import group.acm.bookshare.function.HttpProcessBase;
import group.acm.bookshare.function.LocalApp;
import group.acm.bookshare.function.User;
import group.acm.bookshare.util.Utils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PersonInfoActivity extends Activity {
	public static final int REQUEST_FILE_SELECT = 1;

	private User localUser;

	private ImageView avatarView;
	private TextView nameView;
	private TextView areaView;
	private TextView emailView;
	private TextView bookNumView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_info);
		localUser = ((LocalApp) getApplication()).getUser();

		avatarView = (ImageView) findViewById(R.id.personal_avatar_view);
		nameView = (TextView) findViewById(R.id.personal_textview_name_show);
		areaView = (TextView) findViewById(R.id.personal_textview_area_show);
		emailView = (TextView) findViewById(R.id.personal_textview_email_show);
		bookNumView = (TextView) findViewById(R.id.personal_textview_booknum_show);

		setAction();
		setInformation();
	}

	private void setAction() {
		avatarView.setOnClickListener(new OnClickListener() {

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
		Bitmap bitmap = localUser.getAvatarBitmap();
		if (bitmap != null)
			avatarView.setImageBitmap(bitmap);
		nameView.setText(localUser.getUsername());
		areaView.setText(localUser.getArea());
		emailView.setText(localUser.getEmail());
		bookNumView.setText(Integer.toString(localUser.getPersonBookNum()));
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
				Log.i(Utils.getLineInfo(), "username" + localUser.getUsername());
				localUser.createAvatar(path, new AvatarUpdateProcess());
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class AvatarUpdateProcess extends HttpProcessBase {
		public void error(String content) {
			Toast.makeText(PersonInfoActivity.this, content, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void statusError(String response) {
			Toast.makeText(PersonInfoActivity.this, response, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void statusSuccess(String response) {
			Toast.makeText(PersonInfoActivity.this, "成功", Toast.LENGTH_LONG)
					.show();
			avatarView.setImageBitmap(localUser.getAvatarBitmap());
		}
	}
}
