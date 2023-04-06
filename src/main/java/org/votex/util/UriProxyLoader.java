package org.votex.util;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

@Slf4j
public class UriProxyLoader extends UriLoader {
    public enum METHOD {
        GET, HEAD, PUT, OPTIONS, POST, DELETE
    }
    public static UriLoaderResponse sendRequest(String url, METHOD method, Map headers, String data, UriLoaderParams params, Proxy proxy) {
        HttpsURLConnection conn = null;
        UriLoaderResponse res = new UriLoaderResponse();

        log.info("Connecting to {}...", url);
        try {
            URL imageUrl = new URL(url);

            if (proxy != null) {
                log.info("Using proxy: {}", proxy);
                conn = (HttpsURLConnection)imageUrl.openConnection(proxy);
            } else
                conn = (HttpsURLConnection)imageUrl.openConnection();

            conn.setConnectTimeout(params.connectTimeout);
            conn.setReadTimeout(params.readTimeout);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod(method.name());
            conn.setHostnameVerifier((hostname, session) -> true);

            setHeaders(conn, headers);
            doInterchangeLoop(conn, res, data, params);
        } catch (Exception ex) {
            res.setData(ex.getMessage());
            res.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            log.error("I/O error: {}", ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return res;
    }

    private static void setHeaders(HttpURLConnection conn, Map<String, String> headers) {
        log.info("Request headers:");
        headers.forEach((k, v) -> {
            conn.setRequestProperty(k, v);
            log.info("> {}: {}", k, v);
        });
    }
}
