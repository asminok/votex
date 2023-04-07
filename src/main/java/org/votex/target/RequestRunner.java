package org.votex.target;

import lombok.extern.slf4j.Slf4j;
import org.votex.chrome.CookieProvider;
import org.votex.proxy.ProxyDef;
import org.votex.proxy.ProxyPool;
import org.votex.proxy.ProxyPoolImpl;
import org.votex.util.Morpheus;
import org.votex.util.UriLoaderParams;
import org.votex.util.UriLoaderResponse;
import org.votex.util.UriProxyLoader;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

@Slf4j
public class RequestRunner extends Morpheus {
    private final Configuration configuration;
    private final ProxyPool proxyPool;
    private final CookieProvider cookieProvider;
    private final ResponseParser responseParser;

    private String extCookie = null;
    private Integer lastScore = null;

    private int currentCookie;
    public RequestRunner(Configuration configuration, CookieProvider cookieProvider, ResponseParser responseParser) {
        this.configuration = configuration;
        this.cookieProvider = cookieProvider;
        this.responseParser = responseParser;
        currentCookie = Math.max(0, configuration.getCookies().length - 1);
        proxyPool = configuration.getDirectMode()? null : new ProxyPoolImpl(configuration);
    }

    public int run() {
        if (!configuration.getDirectMode() && (proxyPool == null || !proxyPool.isReady())) {
            log.error("Pool not ready - exiting");
            return 1;
        }

        // Cookie set with --cookie
        if (configuration.getCookies().length > 0) {
            log.info("Using cookies from command line: {}", configuration.getCookies().length);
        } else {
            if (configuration.autoCookie()) {
                log.info("Going to launch the Browser");
                extCookie = cookieProvider.acquireSiteCookies(configuration.getSourceForGET());
                log.info("Cookie from Browser: {}", extCookie);

                if (extCookie == null || extCookie.length() < 10) {
                    log.error("Erroneous value - exiting");
                    return 2;
                }
            }
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

                // Proxy
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

                // POST
                UriLoaderResponse res = UriProxyLoader.sendRequest(configuration.getTargetForPOST(), UriProxyLoader.METHOD.POST, headers, configuration.getPayload(), UriLoaderParams.DEFAULT_PARAMS, proxy);
                log.info("res: ({}) {}", res.getCode(), res.getData());

                if (res != null && (res.getCode() == 200 || res.getCode() == 201)) {
                    numSuccessful++;
                    log.info("SUCCESS: {} of {} done ({} left)", numSuccessful, configuration.getNumberOfReqs(), more-1);
                    if (!parseResponseCounters(res)) {
                        return 3;
                    }
                    success = true;
                } else {
                    if (next != null) {
                        log.warn("Marking proxy {} as down", next);
                        next.setMarkedDown(true);
                    }
                    yeld(configuration.getDelayProxyRetry());
                    success = false;
                }

                if (success) {
                    if (--more > 0) {
                        yeld(configuration.getDelayNext());
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

    private boolean parseResponseCounters(UriLoaderResponse res) {
        if (configuration.getNoScoreCheck()) {
            log.warn("Not checking the resulting score");
            return true;
        }

        Integer score = responseParser.parseInteger(res.getData(), configuration);
        log.info("Got participant score: {} (was {}) {}", score, lastScore, (score == null? " - check your markers setup (--help)" : ""));

        if (lastScore != null && score != null && score <= lastScore) {
            log.error("Score check failed - {} became {} - NOT INCREASED, exiting...", lastScore, score);
            return false;
        }

        lastScore = score;
        return true;
    }

    private void applyCookie(Map headers) {
        headers.put("cookie", getCookie());
    }

    private String getCookie() {
        if (extCookie != null) {
            return extCookie;
        }
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
