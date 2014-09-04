package group.acm.bookshare.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class Utils {
	public static final int ACTIVITY_REQUEST_ADDBOOK = 1;
	public static final int REQUEST_SCANBOOK_UPDATESTATUS = 2;
	public static final int ACTIVITY_REQUEST_SHOWCODE = 3;

	private static boolean first = true;
	private static long lastTime;

	private static Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	public static String decode(String s) {
		Matcher m = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find()) {
			m.appendReplacement(sb,
					Character.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static String encode(String s) {
		StringBuilder sb = new StringBuilder(s.length() * 3);
		for (char c : s.toCharArray()) {
			if (c < 256) {
				sb.append(c);
			} else {
				sb.append("\\u");
				sb.append(Character.forDigit((c >>> 12) & 0xf, 16));
				sb.append(Character.forDigit((c >>> 8) & 0xf, 16));
				sb.append(Character.forDigit((c >>> 4) & 0xf, 16));
				sb.append(Character.forDigit((c) & 0xf, 16));
			}
		}
		return sb.toString();
	}

	public static boolean isQuickClick() {
		if (first) {
			first = false;
			lastTime = System.currentTimeMillis();
			return false;
		}
		long time = System.currentTimeMillis();
		if (time - lastTime < 800) {
			lastTime = time;
			return true;
		} else {
			lastTime = time;
			return false;
		}
	}

	public static String getPath(Context context, Uri uri) {

		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		}

		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static void getBitmap(String url, Handler handler) {

	}
}
