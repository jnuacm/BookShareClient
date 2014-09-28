package group.acm.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BookInformationActivity extends Activity {
	String bookName;
	String bookIsbn;
	String bookDescription;
	int bookImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_information);

		TextView bookNameView = (TextView) findViewById(R.id.book_name);
		TextView bookDescriptionView = (TextView) findViewById(R.id.book_description);
		ImageView bookImageView = (ImageView) findViewById(R.id.book_image);

		Intent intent = getIntent();// ��ȡ email
		Bundle bundle = intent.getBundleExtra("key");// �� email
		bookName = bundle.getString("bookName");
		bookIsbn = bundle.getString("bookIsbn");
		bookDescription = bundle.getString("bookDescription");
		bookImage = bundle.getInt("bookImage");

		bookNameView.setText(bookName);
		bookNameView.setTextSize(30);
		bookDescriptionView.setText("        " + bookDescription);
		bookDescriptionView.setTextSize(15);
		bookImageView.setImageResource(bookImage);
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// ��ȡListView��Ӧ��Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()�������������Ŀ
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); // ��������View �Ŀ��
			totalHeight += listItem.getMeasuredHeight(); // ͳ������������ܸ߶�
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));

		listView.setLayoutParams(params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.book_information, menu);
		return true;
	}
}
