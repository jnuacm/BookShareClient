package group.acm.bookshare.function;

import group.acm.bookshare.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 图片管理类 头像文件 保存格式：avatarusername_version.jpg 保存路径:data/data/package/avatar/
 */
public class ImageManage {
    private static ImageManage manageinstance = new ImageManage();

    public static final int AVATAR_VERSION_NONE = 0;

    private static final String CACHE_RELATIVE_PATH_AVATAR = "avatars";
    private static final String CACHE_RELATIVE_PATH_BOOKS = "books";

    private static Context appContext;
    private Map<String, Bitmap> avatarMap = Collections
            .synchronizedMap(new HashMap<String, Bitmap>()); // 进程中对头像bitmap保存<username,avatar>
    private Map<String, Bitmap> bookImgMap = Collections
            .synchronizedMap(new HashMap<String, Bitmap>()); // 进程中对头像bitmap保存<isbn,book>

    private ImageManage() {

    }

    public static ImageManage getInstance(Context context) {
        appContext = context;
        return manageinstance;
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

    public Map<String, Bitmap> getBookImgs() {
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
        bookImgMap.put(isbn, bitmap);
        Log.i(Utils.getLineInfo(), "load form local: " + isbn);
        return true;
    }

    public boolean loadAvatarFromCache(String name, int curVersion) {
        File avatarsDir = getAvatarsDir();
        File avatarFiles[] = avatarsDir.listFiles(new AvatarFilter(name));
        Log.i(Utils.getLineInfo(),
                name + ":" + Integer.toString(avatarFiles.length));
        for (File cacheAvatar : avatarFiles) {
            if (curVersion == getAvatarStrVersion(name, cacheAvatar.getName())) {
                Bitmap bitmap = BitmapFactory.decodeFile(cacheAvatar
                        .getAbsolutePath());
                avatarMap.put(name, bitmap);
                Log.i(Utils.getLineInfo(), "load form local: " + name);
                return true;
            } else {
                cacheAvatar.delete();
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
        Log.i(Utils.getLineInfo(), "filename: " + filename);
		/*
		 * Pattern pat = Pattern.compile("^avatar" + username +
		 * "_(\\d+)\\.jpg$"); Matcher mat = pat.matcher(filename);
		 */
        String tmp = filename.substring(("avatar" + username + "_").length(),
                filename.length() - 4);// mat.group(1);
        Log.i(Utils.getLineInfo(), "version num: " + tmp);
        return Integer.parseInt(tmp);
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

    /**
     * 清理过多的缓存的文件，超过OVER_SIZE则清除一半
     */
    public void clearOverCacheFile() {
        int OVER_SIZE = 10000;
        File avatarsDir = getAvatarsDir();
        File avatarFiles[] = avatarsDir.listFiles();
        if (avatarFiles.length > OVER_SIZE) {
            for (int i = 0; i < OVER_SIZE; i += 2)
                avatarFiles[i].delete();
        }

        File booksDir = getBooksDir();
        File bookFiles[] = booksDir.listFiles();
        if (bookFiles.length > OVER_SIZE) {
            for (int i = 0; i < OVER_SIZE; i += 2)
                bookFiles[i].delete();
        }
    }

    public void clearBitmap() {
        clearAvatarBitmap();
        clearBookBitmap();
    }

    public void clearAvatarBitmap() {
        avatarMap.clear();
    }

    public void clearBookBitmap() {
        bookImgMap.clear();
    }
}