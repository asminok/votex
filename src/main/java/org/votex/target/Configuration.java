package org.votex.target;

public interface Configuration extends ParserOptions, URLS {
    String[] getCookies();
    String getPayload();
    Integer getNumberOfReqs();
    Integer getDelayNext();
    Integer getRandomDelay();
    Integer getDelayProxyRetry();
    Boolean getDirectMode();
    Boolean autoCookie();
    Boolean getNoScoreCheck();
    Boolean getDebug();
}
