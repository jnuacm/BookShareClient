package group.acm.bookshare.function;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * 此类负责访问网络，利用Singleton模式保证HttpClient只会产生一个实例对象
 */
public class NetAccess {
	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_ERROR = 403;
	public static final int NETMSG_BEFORE = 10;
	public static final int NETMSG_PROCESS = 20;
	public static final int NETMSG_AFTER = 30;
	public static final int NETMSG_ERROR = 40;
	private static final int STATUS_DEFAULT = 403;

	private HttpClient httpClient;

	private static NetAccess internetaccess = new NetAccess();

	private NetAccess() {
		httpClient = new DefaultHttpClient();
	}

	// 获取实例对象的唯一方法
	public static NetAccess getInstance() {
		return internetaccess;
	}

	public void createDoubanThread(String url, List<Handler> handlers) {
		new DoubanThread(url, handlers).start();
	}

	public class DoubanThread extends Thread {
		String url;
		List<Handler> handlers;

		public DoubanThread(String url, List<Handler> handlers) {
			this.url = url;
			this.handlers = handlers;
		}

		public void run() {
			for (int i = 0; i < handlers.size(); i++)
				handlers.get(i).sendEmptyMessage(NetAccess.NETMSG_BEFORE);
			int status = STATUS_DEFAULT;
			String response = "";

			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(this.url)
						.openConnection();
				synchronized (conn) {
					conn.setConnectTimeout(5000);
					conn.setRequestMethod("GET");
					conn.setRequestProperty("charset", HTTP.UTF_8);
					GZIPInputStream gis = (GZIPInputStream) conn.getContent();
					int count;
					byte data[] = new byte[1024];
					while ((count = gis.read(data, 0, 1024)) != -1) {
						String tmp = new String(data, 0, count, HTTP.UTF_8);
						response += tmp;
					}
					gis.close();
					status = conn.getResponseCode();
					conn.disconnect();
				}
				Log.i("NetAccess:url", url);
				Log.i("NetAccess:status", Integer.toString(status));
				Log.i("NetAccess:response", response);

			} catch (Exception e) {
				e.printStackTrace();
			}

			Bundle data = new Bundle();
			data.putInt("status", status);
			data.putString("response", response);

			for (int i = 0; i < handlers.size(); i++) {
				Message msg = Message.obtain();
				msg.what = NetAccess.NETMSG_AFTER;
				msg.setData(data);
				handlers.get(i).sendMessage(msg);
			}
		}
	}

	public void createPostThread(String url, List<NameValuePair> nvps,
			List<Handler> handlers) {
		PostThread thread = new PostThread(url, nvps, handlers);
		thread.start();
	}

	public void createGetThread(String url, List<Handler> handlers) {
		GetThread thread = new GetThread(url, handlers);
		thread.start();
	}

	public void createPutThread(String url, List<NameValuePair> nvps,
			List<Handler> handlers) {
		PutThread thread = new PutThread(url, nvps, handlers);
		thread.start();
	}

	public void createDeleteThread(String url, List<Handler> handlers) {
		DeleteThread thread = new DeleteThread(url, handlers);
		thread.start();
	}

	// /////////////////网络访问线程///////////////////////////
	public abstract class NetThread extends Thread {
		protected String url;
		protected List<NameValuePair> nvps;
		protected List<Handler> handlers;

		private NetThread(String url, List<NameValuePair> nvps,
				List<Handler> handlers) {
			this.url = url;
			this.nvps = nvps;
			this.handlers = handlers;
		}

		protected abstract HttpUriRequest getRequest();

		public void run() {
			for (int i = 0; i < handlers.size(); i++) {
				handlers.get(i).sendEmptyMessage(NetAccess.NETMSG_BEFORE);
			}

			String response = "";
			int status = STATUS_DEFAULT;
			try {
				HttpUriRequest request = getRequest();
				/*request.setHeader(
						new String("Content-Type".getBytes(), "UTF-8"),
						new String("text/html; charset=utf-8".getBytes(),
								"UTF-8"));*/
				HttpResponse httpResponse;
				synchronized (httpClient) {
					Header[] heads = request.getAllHeaders();
					for (int i = 0; i < heads.length; i++)
						Log.i("request header:", heads[i].toString());
					httpResponse = httpClient.execute(request);
					heads = httpResponse.getAllHeaders();
					for (int i = 0; i < heads.length; i++)
						Log.i("response header:", heads[i].toString());
					status = httpResponse.getStatusLine().getStatusCode();
					HttpEntity entity = httpResponse.getEntity();
					response = (entity != null ? EntityUtils.toString(entity) : "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bundle data = new Bundle();
			data.putInt("status", status);
			data.putString("response", response);

			Log.i("NetAccess:url", url);
			Log.i("NetAccess:status", Integer.toString(status));
			Log.i("NetAccess:response", "rp:"+response);

			for (int i = 0; i < handlers.size(); i++) {
				Message msg = Message.obtain();
				msg.what = NetAccess.NETMSG_AFTER;
				msg.setData(data);
				handlers.get(i).sendMessage(msg);
			}
		}
	}

	// /////////////////////四种访问方法分别对应四种线程////////////////////////////////
	public class PostThread extends NetThread {
		private PostThread(String url, List<NameValuePair> nvps,
				List<Handler> handlers) {
			super(url, nvps, handlers);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpPost post = new HttpPost(url);
			try {
				HttpEntity entity = new UrlEncodedFormEntity(nvps);
				post.setEntity(entity);
				// Log.i("before encode:",entity.getContentEncoding().toString());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return post;
		}
	}

	public class GetThread extends NetThread {
		private GetThread(String url, List<Handler> handlers) {
			super(url, null, handlers);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpGet get = new HttpGet(url);
			return get;
		}
	}

	public class PutThread extends NetThread {
		private PutThread(String url, List<NameValuePair> nvps,
				List<Handler> handlers) {
			super(url, nvps, handlers);
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
		private DeleteThread(String url, List<Handler> handlers) {
			super(url, null, handlers);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpDelete delete = new HttpDelete(url);
			return delete;
		}

	}

}
