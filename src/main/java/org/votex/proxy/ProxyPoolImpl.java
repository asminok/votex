package org.votex.proxy;

import lombok.extern.slf4j.Slf4j;
import org.votex.target.URLS;
import org.votex.util.UriLoader;
import org.votex.util.UriLoaderParams;
import org.votex.util.UriLoaderResponse;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Slf4j
public class ProxyPoolImpl implements ProxyPool {

    private final List<ProxyDef> proxies = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger currentProxyIndex = new AtomicInteger(0);
    private boolean isReady = false;
    private final URLS urls;

    public ProxyPoolImpl(URLS urls) {
        this.urls = urls;
        load();
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public ProxyDef getNext() {
        if (!isReady) {
            return null;
        }
        ProxyDef next;
        while ((currentProxyIndex.get()) < proxies.size() && (next = proxies.get(currentProxyIndex.getAndIncrement())) != null) {
            if (!next.isMarkedDown()) {
                return next;
            }
        }
        return null;
    }

    private void shuffle() {
        Collections.shuffle(proxies);
    }

    private void load() {
        load(urls.getProxyListUrl());

        if (isReady = proxies.size() > 0) {
            log.info("Number of hosts in cache: {}", proxies.size());
        }

        log.info("Shuffling...");
        shuffle();
    }
    private void load(String url) {
        log.info("Loading from {} ...", url);
        try {
            UriLoaderParams params = UriLoaderParams.DEFAULT_PARAMS;
            UriLoaderResponse res = UriLoader.getLinkContentEx(url, "GET", null,
                    null, null, params);
            if (res == null || (res.getCode() != 200 && res.getCode() != 201)) {
                throw new Exception();
            }
            loadList(res.getData());
        } catch (Exception ex) {
            log.error("Cannot acquire proxy list from source: {} {}", url, ex);
        }
    }

    private void loadList(String data) {
        final Pattern sep = Pattern.compile(":");
        try (BufferedReader inList = new BufferedReader(new StringReader(data))) {
            String singleProxy;
            while ((singleProxy = inList.readLine()) != null) {
                String[] hosport = singleProxy.split(sep.pattern());
                try {
                    if (hosport != null && hosport.length == 2) {
                        proxies.add(new ProxyDef(hosport[0], Integer.parseInt(hosport[1])));
                    } else {
                        throw new Exception();
                    }
                }
                catch (Exception ex) {
                    log.warn("Erroneous proxy definition: {}", ex, hosport);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to load proxy list: {}", ex);
        }
    }
}