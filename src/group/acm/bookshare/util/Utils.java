package group.acm.bookshare.util;

public class Utils {
	private static boolean first = true;
	private static long lastTime;

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
}
