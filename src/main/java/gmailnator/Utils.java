package gmailnator;

import java.util.List;

public class Utils {


    public static String getCsrfGmailnatorCookie(List<String> cookies) {
        for (String cookie: cookies) {
            if (cookie.contains(CookieHeaders.CSRF_GMAILNATOR_COOKIE)) {
                return cookie.substring(cookie.indexOf(CookieHeaders.CSRF_GMAILNATOR_COOKIE) + CookieHeaders.CSRF_GMAILNATOR_COOKIE.length() + 1, cookie.indexOf(CookieHeaders.EXPIRES) - 2);
            }
        }
        return "";
    }
}
