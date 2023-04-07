package org.votex.chrome;

public interface CookieProvider {
    String acquireSiteCookies(String url);
}
