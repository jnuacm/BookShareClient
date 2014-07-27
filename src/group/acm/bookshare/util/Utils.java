package group.acm.bookshare.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
}
