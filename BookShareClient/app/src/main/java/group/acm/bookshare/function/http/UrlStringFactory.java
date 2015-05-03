package group.acm.bookshare.function.http;

public class UrlStringFactory {
//    public static String URL_HOST = "http://104.148.70.98/";

    public static String URL_HOST = "http://192.168.1.105/";
    public static final String DOUBAN_URL = "https://api.douban.com/v2/book/isbn/";
    public static final String DOUBAN_URL_SEARCH = "https://api.douban.com/v2/book/search?";
    public static final String DOUBAN_FORM = "\\?alt=json";
    public static final String DOUBAN_SEARCH_TAG = "tag=";

    public static final String PATH_LOGIN = "BookShareYii/index.php/api/login/";
    public static final String PATH_REGISTER = "BookShareYii/index.php/api/register/";
    public static final String PATH_USER = "BookShareYii/index.php/api/user/";
    public static final String PATH_BOOK = "BookShareYii/index.php/api/book/";
    public static final String PATH_BOOK_ALL = "/all/";
    public static final String PATH_BOOK_OWNBOOK = "/own/";
    public static final String PATH_BOOK_BORROWED = "/borrowed/";
    public static final String PATH_BOOK_SEARCH = "search";
    public static final String PATH_FRIEND = "BookShareYii/index.php/api/friend/";
    public static final String PATH_INFORM = "BookShareYii/index.php/api/request/";
    public static final String PATH_INFORM_FROM = "from/";
    public static final String PATH_INFORM_TO = "to/";
    public static final String PATH_AVATAR = "BookShareYii/index.php/api/avatar/";
    public static final String PATH_COMMENT = "BookShareYii/index.php/api/comment/";
    public static final String PATH_COMMENT_ISBN = "isbn/";

    public String getLoginUrl() {
        String url = URL_HOST;
        url += PATH_LOGIN;
        return url;
    }

    public String getRegisterUrl() {
        String url = URL_HOST;
        url += PATH_REGISTER;
        return url;
    }

    public String getAimUserUrl(String username) {
        String url = URL_HOST;
        url += PATH_USER;
        url += username;
        return url;
    }

    public String getCreateBookUrl() {
        String url = URL_HOST;
        url += PATH_BOOK;
        return url;
    }

    public String getAimBookUrl(int id) {
        String url = URL_HOST;
        url += PATH_BOOK;
        url += Integer.toString(id);
        return url;
    }

    public String getBookListUrl(String aimName) {
        String url = URL_HOST;
        url += PATH_BOOK;
        url += aimName;
        url += PATH_BOOK_ALL;
        return url;
    }

    public String getFriendListUrl() {
        String url = URL_HOST;
        url += PATH_FRIEND;
        return url;
    }

    public String getAimFriendUrl(String aimName) {
        String url = URL_HOST;
        url += PATH_FRIEND;
        url += aimName;
        return url;
    }

    public String getInformCreateUrl() {
        String url = URL_HOST;
        url += PATH_INFORM;
        return url;
    }

    public String getInformListFromUrl(String aimName) {
        String url = URL_HOST;
        url += PATH_INFORM;
        url += PATH_INFORM_FROM;
        url += aimName;
        return url;
    }

    public String getInformListToUrl(String aimName) {
        String url = URL_HOST;
        url += PATH_INFORM;
        url += PATH_INFORM_TO;
        url += aimName;
        return url;
    }

    public String getAimInformUrl(int id) {
        String url = URL_HOST;
        url += PATH_INFORM;
        url += Integer.toString(id);
        return url;
    }

    public String getAimAvatarUrl(String aimName) {
        String url = URL_HOST;
        url += PATH_AVATAR;
        url += aimName;
        return url;
    }

    public String getBookSearchUrl(String isbn, String name, String author,
                                   String publisher) {
        String url = URL_HOST;
        url += PATH_BOOK;
        url += PATH_BOOK_SEARCH;
        url += ("?isbn=" + isbn);
        url += ("&name=" + name);
        url += ("&author=" + author);
        url += ("&publisher=" + publisher);
        return url;
    }

    public String getDoubanBookUrl(String isbn) {
        String url = DOUBAN_URL;
        url += isbn;
        return url;
    }

    public String getDoubanSearchUrl(String tag) {
        String url = DOUBAN_URL_SEARCH;
        url += DOUBAN_SEARCH_TAG;
        url += tag;
        return url;
    }

    public String getCommentUrl(String isbn) {
        String url = URL_HOST;
        url += PATH_COMMENT;
        url += PATH_COMMENT_ISBN;
        url += isbn;
        return url;
    }

    public String getCommentUrl(int id) {
        String url = URL_HOST;
        url += PATH_COMMENT;
        url += id;
        return url;
    }
}
