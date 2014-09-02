package group.acm.bookshare.function;

import group.acm.bookshare.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

//此类负责访问网络，利用Singleton模式保证HttpClient只会产生一个实例对象
public class NetAccess {
	public static final int STATUS_SUCCESS = 200;
	public static final int STATUS_ERROR = 403;
	private static final int STATUS_DEFAULT = STATUS_ERROR;

	public static final int NETMSG_BEFORE = 10;
	public static final int NETMSG_PROCESS = 20;
	public static final int NETMSG_AFTER = 30;
	public static final int NETMSG_ERROR = 40;
	
	public static final String STATUS = "status";
	public static final String RESPONSE = "response";
	public static final String ERROR = "error";

	private HttpClient httpClient;

	private static NetAccess internetaccess = new NetAccess();

	private NetAccess() {
		KeyStore trustStore;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

	         SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
	         sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  //允许所有主机的验证

	         HttpParams params = new BasicHttpParams();

	         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	         HttpProtocolParams.setContentCharset(params,
	                 HTTP.DEFAULT_CONTENT_CHARSET);
	         HttpProtocolParams.setUseExpectContinue(params, true);

	         // 设置连接管理器的超时
	         ConnManagerParams.setTimeout(params, 10000);
	         // 设置连接超时
	         HttpConnectionParams.setConnectionTimeout(params, 10000);
	         // 设置socket超时
	         HttpConnectionParams.setSoTimeout(params, 10000);

	         // 设置http https支持
	         SchemeRegistry schReg = new SchemeRegistry();
	         schReg.register(new Scheme("http", PlainSocketFactory
	                 .getSocketFactory(), 80));
	         schReg.register(new Scheme("https", sf, 443));

	         ClientConnectionManager conManager = new ThreadSafeClientConnManager(params, schReg);

	        this.httpClient = new DefaultHttpClient(conManager, params);
			
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);*/
	}
	
	class SSLSocketFactoryEx extends SSLSocketFactory {
		 
	    SSLContext sslContext = SSLContext.getInstance("TLS");
	 
	    public SSLSocketFactoryEx(KeyStore truststore)
	            throws NoSuchAlgorithmException, KeyManagementException,
	            KeyStoreException, UnrecoverableKeyException {
	        super(truststore);
	 
	        TrustManager tm = new X509TrustManager() {
	 
	            @Override
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	 
	            @Override
	            public void checkClientTrusted(
	                    java.security.cert.X509Certificate[] chain, String authType)
	                    throws java.security.cert.CertificateException {
	 
	            }
	 
	            @Override
	            public void checkServerTrusted(
	                    java.security.cert.X509Certificate[] chain, String authType)
	                    throws java.security.cert.CertificateException {
	 
	            }
	        };
	 
	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }
	    @Override  
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {  
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);  
	    }  
	  
	    @Override  
	    public Socket createSocket() throws IOException {  
	        return sslContext.getSocketFactory().createSocket();  
	    }  
	}
	

	// 获取实例对象的唯一方法
	public static NetAccess getInstance() {
		return internetaccess;
	}

	// 单独线程从豆瓣获取图书信息(直接用httpClient会出现500错误)
	public class DoubanThread extends Thread {
		String url;
		Handler handler;

		public DoubanThread(String url, Handler handler) {
			this.url = url;
			this.handler = handler;
		}

		public void run() {
			handler.sendEmptyMessage(NetAccess.NETMSG_BEFORE);
			int status = STATUS_DEFAULT;
			String response = "";

			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(this.url)
						.openConnection();
				synchronized (conn) {
					conn.setConnectTimeout(5000);
					conn.setRequestMethod("GET");
					GZIPInputStream gis = (GZIPInputStream) conn.getContent();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(gis));
					Log.i("douban response", conn.getContentType());
					String tmp;
					while ((tmp = br.readLine()) != null) {
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
				Message msg = Message.obtain();
				msg.what = NetAccess.NETMSG_ERROR;
				Bundle data = new Bundle();
				data.putString("error", e.toString());
				msg.setData(data);
				handler.sendMessage(msg);
			}

			Bundle data = new Bundle();
			data.putInt("status", status);
			data.putString("response", response);

			Message msg = Message.obtain();
			msg.what = NetAccess.NETMSG_AFTER;
			msg.setData(data);
			handler.sendMessage(msg);
		}
	}

	public class BitmapThread extends Thread {
		String url;
		ImageView view;

		public BitmapThread(String url, ImageView view) {
			this.url = url;
			this.view = view;
		}

		public void run() {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(this.url)
						.openConnection();
				synchronized (conn) {
					conn.setConnectTimeout(3000);
					conn.setRequestMethod("GET");
					conn.connect();
					InputStream is = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					view.setImageBitmap(bitmap);
					is.close();
					conn.disconnect();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createBitmapThread(String url, Handler handler) {

	}

	public void createDoubanThread(String url, Handler handler) {
		new DoubanThread(url, handler).start();
	}

	public void createPostThread(String url, List<NameValuePair> nvps,
			Handler handler) {
		PostThread thread = new PostThread(url, nvps, handler);
		thread.start();
	}

	public void createGetThread(String url, Handler handler) {
		GetThread thread = new GetThread(url, handler);
		thread.start();
	}

	public void createPutThread(String url, List<NameValuePair> nvps,
			Handler handler) {
		PutThread thread = new PutThread(url, nvps, handler);
		thread.start();
	}

	public void createDeleteThread(String url, Handler handler) {
		DeleteThread thread = new DeleteThread(url, handler);
		thread.start();
	}

	// /////////////////网络访问线程///////////////////////////
	public abstract class NetThread extends Thread {
		protected String url;
		protected List<NameValuePair> nvps;
		protected Handler handler;

		private NetThread(String url, List<NameValuePair> nvps, Handler handler) {
			if (nvps != null) {
				for (NameValuePair i : nvps) {
					Log.i("nvps:", i.getName() + ":" + i.getValue());
				}
			}
			this.url = url;
			this.nvps = nvps;
			this.handler = handler;
		}

		protected abstract HttpUriRequest getRequest() throws Exception;

		public void run() {
			handler.sendEmptyMessage(NetAccess.NETMSG_BEFORE);
			String response = "";
			int status = STATUS_DEFAULT;
			try {
				HttpUriRequest request = getRequest();
				HttpResponse httpResponse;
				synchronized (httpClient) {
					httpResponse = httpClient.execute(request);
				}
				status = httpResponse.getStatusLine().getStatusCode();
				HttpEntity entity = httpResponse.getEntity();
				response = (entity != null ? EntityUtils.toString(entity) : "");
			} catch (Exception e) {
				Message msg = Message.obtain();
				msg.what = NetAccess.NETMSG_ERROR;
				Bundle data = new Bundle();
				data.putString(NetAccess.ERROR, e.toString());
				msg.setData(data);
				handler.sendMessage(msg);
			}

			Bundle data = new Bundle();
			data.putInt(NetAccess.STATUS, status);
			data.putString(NetAccess.RESPONSE, response);

			Log.i("NetAccess:url", url);
			Log.i("NetAccess:status", Integer.toString(status));
			Log.i("NetAccess:response", Utils.decode(response));

			Message msg = Message.obtain();
			msg.what = NetAccess.NETMSG_AFTER;
			msg.setData(data);
			handler.sendMessage(msg);
		}
	}

	// /////////////////////四种访问方法分别对应四种线程////////////////////////////////
	public class PostThread extends NetThread {
		private PostThread(String url, List<NameValuePair> nvps, Handler handler) {
			super(url, nvps, handler);
		}

		@Override
		protected HttpUriRequest getRequest() throws Exception {
			// TODO Auto-generated method stub
			HttpPost post = new HttpPost(url);
			HttpEntity entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
			post.setEntity(entity);
			return post;
		}
	}

	public class GetThread extends NetThread {
		private GetThread(String url, Handler handler) {
			super(url, null, handler);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpGet get = new HttpGet(url);
			return get;
		}
	}

	public class PutThread extends NetThread {
		private PutThread(String url, List<NameValuePair> nvps, Handler handler) {
			super(url, nvps, handler);
		}

		@Override
		protected HttpUriRequest getRequest() throws Exception {
			// TODO Auto-generated method stub
			HttpPut put = new HttpPut(url);
			put.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			return put;
		}
	}

	public class DeleteThread extends NetThread {
		private DeleteThread(String url, Handler handler) {
			super(url, null, handler);
		}

		@Override
		protected HttpUriRequest getRequest() {
			// TODO Auto-generated method stub
			HttpDelete delete = new HttpDelete(url);
			return delete;
		}

	}

}
