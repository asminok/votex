package org.votex.target;

import lombok.extern.slf4j.Slf4j;
import org.votex.proxy.ProxyDef;
import org.votex.proxy.ProxyPool;
import org.votex.proxy.ProxyPoolImpl;
import org.votex.util.UriLoaderParams;
import org.votex.util.UriProxyLoader;
import org.votex.util.UriProxyLoaderResponse;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

@Slf4j
public class RequestRunner {
    private final Configuration configuration;
    private final ProxyPool proxyPool;

    private int currentCookie;
    public RequestRunner(Configuration configuration) {
        this.configuration = configuration;
        currentCookie = configuration.getCookies().length - 1;
        proxyPool = configuration.getDirectMode()? null : new ProxyPoolImpl(configuration);
    }

    public int run() {
        if (!configuration.getDirectMode() && (proxyPool == null || !proxyPool.isReady())) {
            log.error("Pool not ready - exiting");
            return 1;
        }
        log.info("Ready. Press Ctrl+C to abort the loop...");
        return runLoop();
    }

    private int runLoop() {
        boolean exiting = false;
        ProxyDef next = null;
        int maxLoop = 100;
        int more = configuration.getNumberOfReqs();
        int numSuccessful = 0;

        do {
            try {
                boolean success;
                Proxy proxy = null;
                if (!configuration.getDirectMode()) {
                    next = proxyPool.getNext();
                    if (next == null) {
                        log.error("No valid proxies in the pool - exiting (try -d)");
                        break;
                    }
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(next.getHost(), next.getPort()));
                }
                Map headers = Headers.ofPost();
                applyCookie(headers);

                UriProxyLoaderResponse res = UriProxyLoader.sendRequest(configuration.getSourceForGET(), UriProxyLoader.METHOD.POST, headers, configuration.getPayload(), UriLoaderParams.DEFAULT_PARAMS, proxy);
                log.info("res: ({}) {}", res.getCode(), res.getData());

                if (res != null && (res.getCode() == 200 || res.getCode() == 201)) {
                    numSuccessful++;
                    log.info("SUCCESS: {} of {} done ({} left)", numSuccessful, configuration.getNumberOfReqs(), more);
                    parseResponseCounters(res);
                    success = true;
                } else {
                    if (next != null) {
                        log.warn("Marking proxy {} as down", next);
                        next.setMarkedDown(true);
                    }
                    Thread.sleep(configuration.getDelayProxyRetry() * 1000);
                    success = false;
                }

                if (success) {
                    if (--more > 0) {
                        Thread.sleep(configuration.getDelayNext() * 1000);
                    }
                }
            } catch (Exception e) {
                if (next != null) {
                    next.setMarkedDown(true);
                    log.warn("Marking proxy {} as down", next);
                }
                log.error("I/O error: {}", e);
                return 2;
            }
        }
        while (!exiting && more > 0 && maxLoop-- > 0);

        log.info("Done: {} request(s) sent", configuration.getNumberOfReqs()-more);
        return 0;
    }

    private void parseResponseCounters(UriProxyLoaderResponse res) {
    }

    private void applyCookie(Map headers) {
        headers.put("cookie", getCookie());
    }

    private String getCookie() {
        if (configuration.getCookies() == null || configuration.getCookies().length == 0) {
            return null;
        } else if (configuration.getCookies().length == 1) {
            return configuration.getCookies()[0];
        }
        if (--currentCookie < 0) {
            currentCookie = configuration.getCookies().length - 1;
        }
        return configuration.getCookies()[currentCookie];
    }
}
