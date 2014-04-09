package com.example.bookshare.util;

import java.util.List;

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
	private String ret;
	private HttpClient httpLogin;
	private HttpResponse loginResponse;
	private HttpPost post;

	private NetAccess() {
		httpLogin = new DefaultHttpClient();
	}

	private static NetAccess internetaccess;

	// 获取实例对象的唯一方法
	public static NetAccess getInstance() {
		if (internetaccess == null) {
			internetaccess = new NetAccess();
		}
		return internetaccess;
	}

	// 供访问网络时调用的方法
	public String getResponse(String url, List<NameValuePair> nvps) {
		if (internetaccess == null) {
			internetaccess = new NetAccess();
		}
		accessNetwork(url, nvps);
		return ret;
	}

	// url为访问地址，nvps为键值对，确定访问的内容，返回String值保存在类成员ret
	private void accessNetwork(String url, List<NameValuePair> nvps) {
		post = new HttpPost(url);
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
			synchronized (httpLogin) {
				Log.i("run","1");
				loginResponse = httpLogin.execute(post);
			}
			Log.i("run","2");
			ret = EntityUtils.toString(loginResponse.getEntity());
			Log.i("httpreturn", ret);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
