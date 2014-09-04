package group.acm.bookshare.function;

import group.acm.bookshare.R;
import group.acm.bookshare.function.NetAccess.UrlConnectProcess;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.app.Application;
import android.os.Handler;

public class ImageProcess {
	protected Application application;

	public ImageProcess(Application application) {
		this.application = application;
	}

	public void createAvatar(String aimName, String path, Handler handler) {
		String url = application.getString(R.string.url_host);
		url += application.getString(R.string.url_avatar_create);
		url += aimName;

		NetAccess net = NetAccess.getInstance();
		net.createUrlPost(url, new ImageUrlProcess(path), handler);
	}

	public class ImageUrlProcess implements UrlConnectProcess {
		private String path;

		public ImageUrlProcess(String path) {
			this.path = path;
		}

		public void writeOutputStream(HttpURLConnection con) throws Exception {
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			FileInputStream fStream = new FileInputStream(path);
			int bufferSize = 8192;
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			while ((length = fStream.read(buffer)) != -1) {
				ds.write(buffer, 0, length);
			}
			fStream.close();
			ds.close();
		}

		public void getInputStream(InputStream is) throws Exception {
			int ch;
			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
		}
	}
}
