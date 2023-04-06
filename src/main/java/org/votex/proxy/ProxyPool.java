package org.votex.proxy;

public interface ProxyPool {
    boolean isReady();
    ProxyDef getNext();
}
