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
 * ���ฺ��������磬����Singletonģʽ��֤HttpClientֻ�����һ��ʵ������
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

	// ��ȡʵ�������Ψһ����
	public static NetAccess getInstance() {
		if (internetaccess == null) {
			internetaccess = new NetAccess();
		}
		return internetaccess;
	}

	// ����������ʱ���õķ���
	public String getResponse(String url, List<NameValuePair> nvps) {
		if (internetaccess == null) {
			internetaccess = new NetAccess();
		}
		accessNetwork(url, nvps);
		return ret;
	}

	// urlΪ���ʵ�ַ��nvpsΪ��ֵ�ԣ�ȷ�����ʵ����ݣ�����Stringֵ���������Աret
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
