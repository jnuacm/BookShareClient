package group.acm.bookshare.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utils {
	// /////////push utils//////////////
	public static final String TAG = "PushDemoActivity";
	public static final String RESPONSE_METHOD = "method";
	public static final String RESPONSE_CONTENT = "content";
	public static final String RESPONSE_ERRCODE = "errcode";
	protected static final String ACTION_LOGIN = "com.baidu.pushdemo.action.LOGIN";
	public static final String ACTION_MESSAGE = "com.baiud.pushdemo.action.MESSAGE";
	public static final String ACTION_RESPONSE = "bccsclient.action.RESPONSE";
	public static final String ACTION_SHOW_MESSAGE = "bccsclient.action.SHOW_MESSAGE";
	protected static final String EXTRA_ACCESS_TOKEN = "access_token";
	public static final String EXTRA_MESSAGE = "message";

	public static String logStringCache = "";

	// //////////////////Other utils////////////////////
	public static final int ACTIVITY_REQUEST_ADDBOOK = 1;
	public static final int REQUEST_SCANBOOK_UPDATESTATUS = 2;
	public static final int ACTIVITY_REQUEST_SHOWCODE = 3;

	private static boolean first = true;
	private static long lastTime;

	private static Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	public static final String JPG = "jpg";// JPG格式
	public static final String JPEG = "jpeg";// JPEG格式
	public static final String GIF = "gif";// GIF格式
	public static final String PNG = "png";// PNG格式
	public static final String BMP = "bmp";// BMP格式

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

	public static String getCurSystemTime() {
		return Long.toString(System.currentTimeMillis());
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

	public static Bitmap fileToAvatarBitmap(String path) {
		if (!isAvatarImg(path))
			return null;
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = getAvatarScaleSize(options);
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (bitmap == null)
			Log.i(Utils.getLineInfo(), "bitmap is null");
		return bitmap;
	}

	public static boolean isAvatarImg(String path) {
		File file = new File(path);
		if (!file.isFile())
			return false;
		file.getName();
		String fileName = file.getName();
		String postFix = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		postFix = postFix.toLowerCase();
		Log.i(Utils.getLineInfo(), "aim: " + postFix);
		if (postFix.equals(JPG) || postFix.equals(JPEG) || postFix.equals(PNG)
				|| postFix.equals(BMP) || postFix.equals(GIF))
			return true;
		else
			return false;
	}

	public static int getAvatarScaleSize(Options options) {
		int reqHeight = 200;
		int reqWidth = 200;

		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

	public static boolean isAvatarFileSize(File file) {
		if (file.length() <= 128 * 1024)
			return true;
		else
			return false;
	}

	// /////////////////push method////////////////////////
	// record userid/chanelid
	public static void setPushInfo(Context context, String userid) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("userid", userid);
		editor.commit();
	}

	public static String getPushInfo(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("userid", "empty");
	}

	// 获取ApiKey
	public static String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
		String apiKey = null;
		if (context == null || metaKey == null) {
			return null;
		}
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			if (null != ai) {
				metaData = ai.metaData;
			}
			if (null != metaData) {
				apiKey = metaData.getString(metaKey);
			}
		} catch (NameNotFoundException e) {

		}
		return apiKey;
	}

	// 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
	public static boolean hasBind(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String flag = sp.getString("bind_flag", "");
		if ("ok".equalsIgnoreCase(flag)) {
			return true;
		}
		return false;
	}

	public static void setBind(Context context, boolean flag) {
		String flagStr = "not";
		if (flag) {
			flagStr = "ok";
		}
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("bind_flag", flagStr);
		editor.commit();
	}

	public static List<String> getTagsList(String originalText) {
		if (originalText == null || originalText.equals("")) {
			return null;
		}
		List<String> tags = new ArrayList<String>();
		int indexOfComma = originalText.indexOf(',');
		String tag;
		while (indexOfComma != -1) {
			tag = originalText.substring(0, indexOfComma);
			tags.add(tag);

			originalText = originalText.substring(indexOfComma + 1);
			indexOfComma = originalText.indexOf(',');
		}

		tags.add(originalText);
		return tags;
	}

	public static String getLogText(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("log_text", "");
	}

	public static void setLogText(Context context, String text) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("log_text", text);
		editor.commit();
	}

	// 获取代码文件名及行号
	public static String getLineInfo() {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return ste.getFileName() + ": Line " + ste.getLineNumber();
	}

	// 设置是否有更新
	public static void setHasUpdate(Context appContext, boolean flag) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(appContext);
		Editor editor = sp.edit();
		editor.putBoolean("has_update", flag);
		editor.commit();
	}

	// 获取是否有更新
	public synchronized static boolean hasUpdate(Context appContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(appContext);
		return sp.getBoolean("has_update", false);
	}
}
