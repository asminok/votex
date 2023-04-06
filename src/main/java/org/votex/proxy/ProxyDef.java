package org.votex.proxy;

import lombok.Data;

@Data
public class ProxyDef {
    private String host;
    private int port;
    private boolean markedDown;

    public ProxyDef(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
