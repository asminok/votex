package org.votex.util;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UriProxyLoader extends UriLoader {
    public enum METHOD {
        GET, HEAD, PUT, OPTIONS, POST, DELETE
    }
    public static UriProxyLoaderResponse sendRequest(String url, METHOD method, Map headers, String data, UriLoaderParams params, Proxy proxy) {
        HttpsURLConnection conn = null;
        UriProxyLoaderResponse res = new UriProxyLoaderResponse();

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

            exchange(conn, res, data, params);
        } catch (Exception ex) {
            res.setData(ex.getMessage());
            res.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
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

    private static UriLoaderResponse exchange(HttpURLConnection conn, UriProxyLoaderResponse res, String data, UriLoaderParams params) {
        InputStream in = null;
        OutputStream os = null;

        try {
            if (data != null) {
                byte[] encodedData = data.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length));
                conn.setDoOutput(true);

                os = conn.getOutputStream();
                os.write(encodedData);
            }
            res.setCode(conn.getResponseCode());

            if (res.getCode() >= HttpStatus.BAD_REQUEST.value()) {
                in = new BufferedInputStream(conn.getErrorStream());
            } else {
                in = new BufferedInputStream(conn.getInputStream());
            }

            byte[] buffer = new byte[10240];
            int length;
            int totalLength = 0;

            if (params.binaryMode) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while (totalLength < params.maxContentSize && (length = in.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                    totalLength += length;
                }
                in.close();
                byte[] binRes = output.toByteArray();
                output.close();
                res.setBinaryData(binRes);
            } else {
                StringBuilder tres = new StringBuilder();
                while (totalLength < params.maxContentSize && (length = in.read(buffer)) > 0) {
                    tres.append(new String(buffer, 0, length));
                    totalLength += length;
                }
                in.close();
                res.setData(tres.toString());
            }
            // Headers
            Map<String, String> headers = new HashMap<>();
            log.info("Response headers:");
            for (Map.Entry<String, List<String>> entries : conn.getHeaderFields().entrySet()) {
                StringBuilder values = new StringBuilder();
                for (String value : entries.getValue()) {
                    if (values.length() > 0) {
                        values.append(", ");
                    }
                    values.append(value);
                }
                headers.put(entries.getKey(), values.toString());
                log.info("< {} : {}", entries.getKey(), values.toString());
            }
            res.setHeaders(headers);
        } catch (Exception ex) {
            if (res == null) {
                res = new UriProxyLoaderResponse();
            }
            res.setData(ex.getMessage());
            res.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error(ex.getMessage(), ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return res;
    }
}
