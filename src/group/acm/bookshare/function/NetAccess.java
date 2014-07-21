package group.acm.bookshare.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/*
 * 此类负责访问网络，利用Singleton模式保证HttpClient只会产生一个实例对象
 */

public class NetAccess {
	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_ERROR = 403;

	private Map<String, Object> ret = new HashMap<String, Object>();
	private HttpClient httpLogin;
	private HttpResponse loginResponse;
	private HttpPost post;

	private NetAccess() {
		httpLogin = new DefaultHttpClient();
	}

	private static NetAccess internetaccess = new NetAccess();

	// 获取实例对象的唯一方法
	public static NetAccess getInstance() {
		return internetaccess;
	}

	// 供访问网络时调用的方法
	public Map<String, Object> getResponse(String url, List<NameValuePair> nvps) {
		accessNetwork(url, nvps);
		return ret;
	}

	// url为访问地址，nvps为键值对，确定访问的内容，返回String值保存在类成员ret
	private void accessNetwork(String url, List<NameValuePair> nvps) {
		Log.i("url", url);
		post = new HttpPost(url);
		String response = null;
		int status = STATUS_ERROR;
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
			synchronized (httpLogin) {
				loginResponse = httpLogin.execute(post);
			}
			status = loginResponse.getStatusLine().getStatusCode();
			response = EntityUtils.toString(loginResponse.getEntity());

			if (response == null)
				Log.i("yes", "is null");
			else
				Log.i("noway", response);

		} catch (Exception e) {
			e.printStackTrace();
		}
		ret.put("status", status);
		ret.put("response", response);
	}
}
