package group.acm.bookshare.function.http;

import group.acm.bookshare.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
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

import android.util.Log;

//此类负责访问网络，利用Singleton模式保证HttpClient只会产生一个实例对象
public class NetAccess {
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_ERROR = 403;
    private static final int STATUS_DEFAULT = STATUS_ERROR;

    public static final int NETMSG_BEFORE = 10;
    public static final int NETMSG_PROCESS = 20;
    public static final int NETMSG_AFTER = 30;
    public static final int NETMSG_ERROR = 40;

    public static final int MULTI_POOL_MAX_SIZE = 10;

    public static final String STATUS = "status";
    public static final String RESPONSE = "response";
    public static final String ERROR = "error";

    private HttpClient httpClient;
    private HttpClient httpsClient;

    private ExecutorService pool;
    private ExecutorService multiPool;

    private static NetAccess internetaccess = new NetAccess();

    private NetAccess() {
        pool = Executors.newSingleThreadExecutor();
        multiPool = Executors.newFixedThreadPool(MULTI_POOL_MAX_SIZE);
        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // 允许所有主机的验证

            HttpParams params = new BasicHttpParams();

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params,
                    HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);

            // 设置连接管理器的超时
            ConnManagerParams.setTimeout(params, 3000);
            // 设置连接超时
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            // 设置socket超时
            HttpConnectionParams.setSoTimeout(params, 3000);

            // 设置http https支持
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schReg.register(new Scheme("https", sf, 443));

            ClientConnectionManager conManager = new ThreadSafeClientConnManager(
                    params, schReg);

            httpsClient = new DefaultHttpClient(conManager, params);

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient = new DefaultHttpClient();
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

    // 获取实例对象的唯一方法
    public static NetAccess getInstance() {
        return internetaccess;
    }

    // /////////////////网络访问方法///////////////////////////
    /**
     * UrlConnection线程获取方法
     */
    public UrlConnectionThread createUrlConntectionGetThread(String url, NetProgress progress,
                                                             StreamProcess process) {
        UrlConnectionThread thread = new UrlConnectionThread(url, progress, process);
        multiPool.execute(thread);
        return thread;
    }

    // 通用四种访问
    public NetThread createPostThread(String url, HttpEntity entity,
                                      NetProgress progress) {
        NetThread thread = new PostThread(url, entity, progress,
                new SimpleProcessImpl());
        pool.execute(thread);
        return thread;
    }

    public NetThread createGetThread(String url, NetProgress progress) {
        NetThread thread = new GetThread(url, progress, new SimpleProcessImpl());
        pool.execute(thread);
        return thread;
    }

    public NetThread createPutThread(String url, HttpEntity entity,
                                     NetProgress progress) {
        NetThread thread = new PutThread(url, entity, progress,
                new SimpleProcessImpl());
        pool.execute(thread);
        return thread;
    }

    public NetThread createDeleteThread(String url, NetProgress progress) {
        NetThread thread = new DeleteThread(url, progress, new SimpleProcessImpl());
        pool.execute(thread);
        return thread;
    }

    // 包含文件的网络访问
    public NetThread createFileGetThread(String url, NetProgress progress,
                                         EntityProcess eprocess) {
        NetThread thread = new GetThread(url, progress, eprocess);
        pool.execute(thread);
        return thread;
    }

    // 单独线程类从豆瓣获取图书信息(直接用httpClient会出现500错误)
    public class UrlConnectionThread extends Thread {
        String url;
        NetProgress progress;
        StreamProcess process;
        private boolean bIsCancel;

        public UrlConnectionThread(String url, NetProgress progress,
                                   StreamProcess process) {
            this.url = url;
            this.progress = progress;
            this.process = process;
            this.bIsCancel = false;
        }

        public boolean isCanceled() {
            return bIsCancel;
        }

        public void cancelRequest() {
            bIsCancel = true;
        }

        public void run() {
            if (bIsCancel)
                return;
            progress.setBefore();
            if (bIsCancel)
                return;
            int status = STATUS_DEFAULT;
            String response = "";

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(this.url)
                        .openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                status = conn.getResponseCode();
                response = process.getResponse(status, is);
                is.close();
                conn.disconnect();
                Log.i("NetAccess:url", url);
                Log.i("NetAccess:status", Integer.toString(status));
                Log.i("NetAccess:response", response);
                if (bIsCancel)
                    return;
                progress.setAfter(status, response);

            } catch (Exception e) {
                if (bIsCancel)
                    return;
                progress.setError(e.toString());
            }
            bIsCancel = true;
        }
    }

    /**
     * UrlConnection的Stream处理方法
     */
    public interface StreamProcess {
        public String getResponse(int status, InputStream responseStream);
    }

    // //////////////通用网络线程类/////////////////
    // 抽象线程类
    public abstract class NetThread extends Thread {
        protected String url; // 目标url
        protected NetProgress progress; // 进度接口
        protected EntityProcess entityProcess; // 返回的HttpEntity的处理接口
        private boolean bIsCancel;

        private NetThread(String url, NetProgress progress,
                          EntityProcess entityProcess) {
            this.url = url;
            this.progress = progress;
            this.entityProcess = entityProcess;
            this.bIsCancel = false;
        }

        public boolean isCanceled() {
            return bIsCancel;
        }

        public void cancelRequest() {
            bIsCancel = true;
        }

        public void run() {
            if (bIsCancel)
                return;
            progress.setBefore();
            int status = STATUS_DEFAULT;
            try {
                HttpUriRequest request = getRequest();
                HttpResponse httpResponse;
                if (bIsCancel)
                    return;
                if (url.startsWith("https")) {
                    synchronized (httpsClient) {
                        httpResponse = httpsClient.execute(request);
                    }
                } else {
                    synchronized (httpClient) {
                        httpResponse = httpClient.execute(request);
                    }
                }
                status = httpResponse.getStatusLine().getStatusCode();
                HttpEntity responseEntity = httpResponse.getEntity();
                Log.i("NetAccess:url", url);
                Log.i("NetAccess:status", Integer.toString(status));
                String response = entityProcess.getResponse(status,
                        responseEntity);
                if (bIsCancel)
                    return;
                progress.setAfter(status, response);
            } catch (Exception e) {
                if (bIsCancel)
                    return;
                progress.setError(e.toString());
            }
            bIsCancel = true;
        }

        // 获取请求类型
        protected abstract HttpUriRequest getRequest() throws Exception;
    }

    /**
     * HttpEntity的处理方法
     */
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

    // /////////////////////四种访问方法分别对应四种线程实现///////////////////////////////
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

    public void poolsClose() {
        pool.shutdown();
        multiPool.shutdown();
    }
}
