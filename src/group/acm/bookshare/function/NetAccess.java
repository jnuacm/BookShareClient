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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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

import android.graphics.BitmapFactory;
import android.util.Log;

//���ฺ��������磬����Singletonģʽ��֤HttpClientֻ�����һ��ʵ������
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

	private ExecutorService pool;

	private static NetAccess internetaccess = new NetAccess();

	private NetAccess() {
		pool = Executors.newSingleThreadExecutor();
		KeyStore trustStore;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // ����������������֤

			HttpParams params = new BasicHttpParams();

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params,
					HTTP.DEFAULT_CONTENT_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);

			// �������ӹ������ĳ�ʱ
			ConnManagerParams.setTimeout(params, 10000);
			// �������ӳ�ʱ
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			// ����socket��ʱ
			HttpConnectionParams.setSoTimeout(params, 10000);

			// ����http https֧��
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schReg.register(new Scheme("https", sf, 443));

			ClientConnectionManager conManager = new ThreadSafeClientConnManager(
					params, schReg);

			httpClient = new DefaultHttpClient(conManager, params);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class SSLSocketFactoryEx extends SSLSocketFactory {

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
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	// ��ȡʵ�������Ψһ����
	public static NetAccess getInstance() {
		return internetaccess;
	}

	// /////////////////������ʷ���///////////////////////////
	// �������
	public void createDoubanThread(String url, NetProgress progress) {
		pool.execute(new DoubanThread(url, progress));
	}

	// ͨ�����ַ���
	public void createPostThread(String url, HttpEntity entity,
			NetProgress progress) {
		pool.execute(new PostThread(url, entity, progress,
				new SimpleProcessImpl()));
	}

	public void createGetThread(String url, NetProgress progress) {
		pool.execute(new GetThread(url, progress, new SimpleProcessImpl()));
	}

	public void createPutThread(String url, HttpEntity entity,
			NetProgress progress) {
		pool.execute(new PutThread(url, entity, progress,
				new SimpleProcessImpl()));
	}

	public void createDeleteThread(String url, NetProgress progress) {
		pool.execute(new DeleteThread(url, progress, new SimpleProcessImpl()));
	}

	// �����ļ����������
	public void createFileGetThread(String url, NetProgress progress,
			EntityProcess eprocess) {
		pool.execute(new GetThread(url, progress, eprocess));
	}

	// �����߳���Ӷ����ȡͼ����Ϣ(ֱ����httpClient�����500����)
	public class DoubanThread extends Thread {
		String url;
		NetProgress progress;

		public DoubanThread(String url, NetProgress progress) {
			this.url = url;
			this.progress = progress;
		}

		public void run() {
			progress.setBefore();
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

				progress.setAfter(status, response);

			} catch (Exception e) {
				progress.setError(e.toString());
			}
		}
	}

	// //////////////ͨ�������߳���/////////////////
	// �����߳���
	public abstract class NetThread extends Thread {
		protected String url; // Ŀ��url
		protected NetProgress progress; // ���Ƚӿ�
		protected EntityProcess entityProcess; // ���ص�HttpEntity�Ĵ���ӿ�

		private NetThread(String url, NetProgress progress,
				EntityProcess entityProcess) {
			this.url = url;
			this.progress = progress;
			this.entityProcess = entityProcess;
		}

		public void run() {
			progress.setBefore();
			int status = STATUS_DEFAULT;
			try {
				HttpUriRequest request = getRequest();
				HttpResponse httpResponse;
				synchronized (httpClient) {
					httpResponse = httpClient.execute(request);
				}
				status = httpResponse.getStatusLine().getStatusCode();
				HttpEntity responseEntity = httpResponse.getEntity();
				Log.i("NetAccess:url", url);
				Log.i("NetAccess:status", Integer.toString(status));
				progress.setAfter(status,
						entityProcess.getResponse(status, responseEntity));
			} catch (Exception e) {
				progress.setError(e.toString());
			}
		}

		// ��ȡ��������
		protected abstract HttpUriRequest getRequest() throws Exception;
	}

	// HttpEntity�Ĵ�����
	public interface EntityProcess {
		public String getResponse(int status, HttpEntity responseEntity);
	}

	private class SimpleProcessImpl implements EntityProcess {

		@Override
		public String getResponse(int status, HttpEntity responseEntity) {
			try {
				String response = (responseEntity != null ? EntityUtils
						.toString(responseEntity) : "");
				Log.i("NetAccess:response", Utils.decode(response));
				return response;

			} catch (Exception e) {
				return e.toString();
			}
		}

	}

	// /////////////////////���ַ��ʷ����ֱ��Ӧ�����߳�ʵ��///////////////////////////////
	public class PostThread extends NetThread {
		private HttpEntity entity;

		private PostThread(String url, HttpEntity entity, NetProgress progress,
				EntityProcess entityProcess) {
			super(url, progress, entityProcess);
			this.entity = entity;
		}

		@Override
		protected HttpUriRequest getRequest() throws Exception {
			HttpPost post = new HttpPost(url);
			post.setEntity(entity);
			return post;
		}
	}

	public class GetThread extends NetThread {
		private GetThread(String url, NetProgress progress,
				EntityProcess entityProcess) {
			super(url, progress, entityProcess);
		}

		@Override
		protected HttpUriRequest getRequest() {
			HttpGet get = new HttpGet(url);
			return get;
		}
	}

	public class PutThread extends NetThread {
		private HttpEntity entity;

		private PutThread(String url, HttpEntity entity, NetProgress progress,
				EntityProcess entityProcess) {
			super(url, progress, entityProcess);
			this.entity = entity;
		}

		@Override
		protected HttpUriRequest getRequest() throws Exception {
			HttpPut put = new HttpPut(url);
			put.setEntity(entity);
			return put;
		}
	}

	public class DeleteThread extends NetThread {
		private DeleteThread(String url, NetProgress progress,
				EntityProcess entityProcess) {
			super(url, progress, entityProcess);
		}

		@Override
		protected HttpUriRequest getRequest() {
			HttpDelete delete = new HttpDelete(url);
			return delete;
		}
	}
}
