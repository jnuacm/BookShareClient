package group.acm.bookshare.function;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/*
 * 此类负责访问网络，利用Singleton模式保证HttpClient只会产生一个实例对象
 */

public class NetAccess {
	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_ERROR = 403;
	private static final int STATUS_DEFAULT = 403;

	private HttpClient httpClient;

	private NetAccess() {
		httpClient = new DefaultHttpClient();
	}

	private static NetAccess internetaccess = new NetAccess();

	// 获取实例对象的唯一方法
	public static NetAccess getInstance() {
		return internetaccess;
	}

	public Thread getDoubanThread(String url, List<Update> updates) {
		return new DoubanThread(url, updates);
	}

	public class DoubanThread extends Thread {
		String url;
		List<Update> updates;

		public DoubanThread(String url, List<Update> updates) {
			this.url = url;
			this.updates = updates;
		}

		public void run() {
			for (int i = 0; i < updates.size(); i++)
				updates.get(i).before();
			int status = STATUS_DEFAULT;
			String response = "";
			Map<String, Object> map = new HashMap<String, Object>();
			try {

				HttpURLConnection conn = (HttpURLConnection) new URL(this.url)
						.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				GZIPInputStream gis = (GZIPInputStream) conn.getContent();
				int count;
				byte data[] = new byte[1024];
				while ((count = gis.read(data, 0, 1024)) != -1) {
					String tmp = new String(data, 0, count);
					response += tmp;
				}
				status = conn.getResponseCode();
				Log.i("NetAccess:url", url);
				Log.i("NetAccess:status", Integer.toString(status));
				Log.i("NetAccess:response", response);

			} catch (Exception e) {
				e.printStackTrace();
			}

			map.put("status", status);
			map.put("response", response);

			for (int i = 0; i < updates.size(); i++)
				updates.get(i).after(map);
		}
	}

	public Thread getPostThread(String url, List<NameValuePair> nvps,
			List<Update> updates) {
		PostThread thread = new PostThread(url, nvps);
		thread.updates = updates;
		return thread;
	}

	public Thread getGetThread(String url, List<Update> updates) {
		GetThread thread = new GetThread(url);
		thread.updates = updates;
		return thread;
	}

	public Thread getPutThread(String url, List<NameValuePair> nvps,
			List<Update> updates) {
		PutThread thread = new PutThread(url, nvps);
		thread.updates = updates;
		return thread;
	}

	public Thread getDeleteThread(String url, List<Update> updates) {
		DeleteThread thread = new DeleteThread(url);
		thread.updates = updates;
		return thread;
	}

	// /////////////////网络访问线程///////////////////////////
	public abstract class NetThread extends Thread {
		protected String url;
		protected List<NameValuePair> nvps;
		protected List<Update> updates;

		private NetThread(String url, List<NameValuePair> nvps) {
			this.url = url;
			this.nvps = nvps;
		}

		protected abstract HttpUriRequest getRequest();

		public void run() {
			for (int i = 0; i < updates.size(); i++)
				updates.get(i).before();

			String response = null;
			int status = STATUS_DEFAULT;
			try {
				HttpUriRequest request = getRequest();
				HttpResponse httpResponse;
				synchronized (httpClient) {
					httpResponse = httpClient.execute(request);
				}
				status = httpResponse.getStatusLine().getStatusCode();
				response = EntityUtils.toString(httpResponse.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("status", status);
			map.put("response", response);

			Log.i("NetAccess:url", url);
			Log.i("NetAccess:status", Integer.toString(status));
			Log.i("NetAccess:response", response);

			for (int i = 0; i < updates.size(); i++)
				updates.get(i).after(map);
		}
	}

	// /////////////////////四种访问方法分别对应四种线程////////////////////////////////
	public class PostThread extends NetThread {
		private PostThread(String url, List<NameValuePair> nvps) {
			super(url, nvps);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpPost post = new HttpPost(url);
			try {
				post.setEntity(new UrlEncodedFormEntity(nvps));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return post;
		}
	}

	public class GetThread extends NetThread {
		private GetThread(String url) {
			super(url, null);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpGet get = new HttpGet(url);
			return get;
		}
	}

	public class PutThread extends NetThread {
		private PutThread(String url, List<NameValuePair> nvps) {
			super(url, nvps);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpPut put = new HttpPut(url);
			try {
				put.setEntity(new UrlEncodedFormEntity(nvps));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return put;
		}
	}

	public class DeleteThread extends NetThread {
		private DeleteThread(String url) {
			super(url, null);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpDelete delete = new HttpDelete(url);
			return delete;
		}

	}

}
