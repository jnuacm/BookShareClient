package group.acm.bookshare.function;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class PageListAdapter extends BaseAdapter implements
		OnScrollListener {
	public static final int DEFAULT_PAGE_SIZE = 10;

	protected int curViewSize = DEFAULT_PAGE_SIZE;
	private boolean isLastRow;
	
	/**
	 * �б��ҳ��ʾ�����У������ˢ�º���Ҫ�������ҳ����������ô˺�������notify
	 */
	public void initViewItemSize(){
		initViewItemSize(DEFAULT_PAGE_SIZE);
	}

	public void initViewItemSize(int Size) {
		if (Size < 0)
			curViewSize = DEFAULT_PAGE_SIZE;
		else
			curViewSize = Size;
	}
 
	public abstract void loadData();
                                         
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isLastRow && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			loadData();
			curViewSize += DEFAULT_PAGE_SIZE;
			isLastRow = false;
			notifyDataSetChanged();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount > 0) {
			isLastRow = true;
		} else
			isLastRow = false;
	}
	
	public void updateAdapter(){
		initViewItemSize();
		this.notifyDataSetChanged();
	}
}
