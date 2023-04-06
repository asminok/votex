package org.votex.target;

public interface Configuration extends URLS {
    String[] getCookies();
    String getPayload();
    Integer getNumberOfReqs();
    Integer getDelayNext();
    Integer getDelayPost();
    Integer getDelayProxyRetry();
    Boolean getDirectMode();
}
