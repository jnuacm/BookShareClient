package group.acm.bookshare.function;

import group.acm.bookshare.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/*
 * 头像文件
 * 保存格式：avatarusername_version.jpg
 * 保存路径:data/data/package/avatar/
 */

// 图片管理类
public class ImageManage {
	public static final int AVATAR_VERSION_NONE = 0;

	private static final String CACHE_RELATIVE_PATH_AVATAR = "avatars";
	private static final String CACHE_RELATIVE_PATH_BOOKS = "books";

	private Context appContext;
	private Map<String, Bitmap> avatarMap; // 进程中对头像bitmap保存<username,avatar>
	private Map<String, Bitmap> bookImgMap; // 进程中对头像bitmap保存<isbn,book>

	public ImageManage(Context appContext) {
		this.appContext = appContext;
		avatarMap = new HashMap<String, Bitmap>();
		bookImgMap = new HashMap<String, Bitmap>();
	}

	public void saveBookImg(String isbn, Bitmap bookImg) {
		bookImgMap.put(isbn, bookImg);
		setBookImgToCache(isbn, bookImg);
	}

	public void saveAvatar(String username, Bitmap avatar, int curVersion) {
		avatarMap.put(username, avatar);
		setAvatarToCache(username, avatar, curVersion);
	}

	private void setBookImgToCache(String isbn, Bitmap bookImg) {
		deleteBook(isbn);
		File booksDir = getBooksDir();
		String filename = isbn + ".jpg";
		File bookFile = new File(booksDir, filename);
		try {
			FileOutputStream fo = new FileOutputStream(bookFile);
			bookImg.compress(Bitmap.CompressFormat.JPEG, 100, fo);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setAvatarToCache(String username, Bitmap avatar, int curVersion) {
		deleteAvatars(username);
		File avatarsDir = getAvatarsDir();
		String filename = "avatar" + username + "_"
				+ Integer.toString(curVersion) + ".jpg";
		File avatarFile = new File(avatarsDir, filename);
		try {
			FileOutputStream fo = new FileOutputStream(avatarFile);
			avatar.compress(Bitmap.CompressFormat.JPEG, 100, fo);
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap getAvatarBitmap(String aimName) {
		if (avatarMap.containsKey(aimName))
			return avatarMap.get(aimName);
		else
			return null;
	}
	
	public Map<String, Bitmap> getBookImgs(){
		return bookImgMap;
	}

	public Map<String, Bitmap> getAvatars() {
		return avatarMap;
	}

	public File getAvatarsDir() {
		return appContext.getDir(CACHE_RELATIVE_PATH_AVATAR,
				Context.MODE_PRIVATE);
	}

	public File getBooksDir() {
		return appContext.getDir(CACHE_RELATIVE_PATH_BOOKS,
				Context.MODE_PRIVATE);
	}

	public boolean loadBookImgFromCache(String isbn) {
		File booksDir = getBooksDir();
		File cacheBook = new File(booksDir, isbn + ".jpg");
		if (!cacheBook.isFile())
			return false;
		Bitmap bitmap = BitmapFactory.decodeFile(cacheBook.getAbsolutePath());
		avatarMap.put(isbn, bitmap);
		return true;
	}

	public boolean loadAvatarFromCache(String name, int curVersion) {
		File avatarsDir = getAvatarsDir();
		File avatarFiles[] = avatarsDir.listFiles(new AvatarFilter(name));
		Log.i(Utils.getLineInfo(),
				name + ":" + Integer.toString(avatarFiles.length));
		if (avatarFiles.length > 0) {
			File cacheAvatar = avatarFiles[0];
			if (curVersion == getAvatarStrVersion(name, cacheAvatar.getName())) {
				Bitmap bitmap = BitmapFactory.decodeFile(cacheAvatar
						.getAbsolutePath());
				avatarMap.put(name, bitmap);
				return true;
			}
		}
		return false;
	}

	public void deleteBook(String isbn) {
		File booksDir = getBooksDir();
		File bookFile = new File(booksDir, isbn + ".jpg");
		if (bookFile.isFile())
			bookFile.delete();
	}

	public void deleteAvatars(String name) {
		File avatarsDir = getAvatarsDir();
		File avatarFiles[] = avatarsDir.listFiles(new AvatarFilter(name));
		for (File f : avatarFiles) {
			f.delete();
		}
	}

	// 获取文件名的版本号
	public int getAvatarStrVersion(String username, String filename) {
		Pattern pat = Pattern.compile("^avatar" + username
				+ "_([1-9]\\d*|0)\\.jpg$");
		Matcher mat = pat.matcher(filename);
		return Integer.parseInt(mat.group(1));
	}

	// 根据用户名username获取头像File
	public File getAvatarFile(String username) {
		File avatarsDir = getAvatarsDir();
		File avatarFiles[] = avatarsDir.listFiles(new AvatarFilter(username));
		if (avatarFiles.length > 0) {
			Log.i(Utils.getLineInfo(), "path: " + avatarFiles[0].getPath()
					+ "  abpath:" + avatarFiles[0].getAbsolutePath());
			return avatarFiles[0];
		}
		return new File(avatarsDir, "empty");
	}

	// 过滤出用户名为username的头像图片
	private class AvatarFilter implements FilenameFilter {
		private String name;

		public AvatarFilter(String name) {
			this.name = name;
		}

		@Override
		public boolean accept(File dir, String filename) {
			Log.i(Utils.getLineInfo(), "file name:" + filename);
			Pattern pat = Pattern.compile("^avatar" + name
					+ "_([1-9]\\d*|0)\\.jpg$");
			Matcher mat = pat.matcher(filename);
			boolean flag = mat.matches();
			Log.i(Utils.getLineInfo(), Boolean.toString(flag));
			return flag;
		}

	}
}