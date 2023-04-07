package org.votex.target;

public interface Configuration extends ParserOptions, URLS {
    String[] getCookies();
    String getPayload();
    Integer getNumberOfReqs();
    Integer getDelayNext();
    Integer getDelayProxyRetry();
    Boolean getDirectMode();
    Boolean autoCookie();
}
