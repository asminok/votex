package org.votex.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class UriLoader {
    public static final int HTTP_CONNECT_TIMEOUT = 10000;
    public static final int HTTP_READ_TIMEOUT = 10000;
    public static final int MAX_CONTENT_SIZE = 100 * 1024 * 1024;

    private static final UriLoaderParams defaultParams = new UriLoaderParams(HTTP_CONNECT_TIMEOUT, HTTP_READ_TIMEOUT, MAX_CONTENT_SIZE, false);

    public static UriLoaderResponse getLinkContentEx(String url, String method, String auth, String data) {
        return getLinkContentEx(url, method, auth, data, "application/json");
    }

    public static UriLoaderResponse getLinkContentEx(String url, String method, String auth,
                                                     String data, String contentType) {
        return getLinkContentEx(url, method, auth, data, contentType, defaultParams);
    }

    public static UriLoaderResponse getLinkContentEx(String url, String method, String auth,
                                                     String data, String contentType, UriLoaderParams params) {
        HttpURLConnection conn = null;
        InputStream in = null;
        OutputStream os = null;
        UriLoaderResponse res = new UriLoaderResponse();

        try {
            URL imageUrl = new URL(url);

            conn = (HttpURLConnection) imageUrl.openConnection();
            if (StringUtils.isNotBlank(auth)) {
                conn.setRequestProperty("Authorization", auth);
            }
            conn.setConnectTimeout(params.connectTimeout);
            conn.setReadTimeout(params.readTimeout);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod(method);
            if (StringUtils.isNotBlank(contentType)) {
                conn.setRequestProperty("Content-Type", contentType);
            }

            setBasicHeaders(conn, params);
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            doInterchangeLoop(conn, res, data, params);
        } catch (Exception ex) {
            if (res == null) {
                res = new UriLoaderResponse();
            }
            res.setData(ex.getMessage());
            res.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            log.error(ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return res;
    }

    public static UriContentInfo getLinkContentInfo(String contentUrl) {
        return getLinkContentInfo(contentUrl, defaultParams);
    }

    private static void setBasicHeaders(HttpURLConnection conn, UriLoaderParams params) {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setRequestProperty("Cache-Control", "no-cache");
    }

    public static UriContentInfo getLinkContentInfo(String contentUrl, UriLoaderParams params) {
        try {
            URL url = new URL(contentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(params.connectTimeout);
            conn.setReadTimeout(params.readTimeout);
            conn.setRequestMethod("HEAD");
            setBasicHeaders(conn, params);
            int status = conn.getResponseCode();
            Integer length = Integer.parseInt(conn.getHeaderField("Content-Length"));
            String type = conn.getHeaderField("Content-Type");
            return new UriContentInfo(status, length, type);
        } catch (Exception e) {
            log.error("getLinkContentInfo: {}", e);
            return null;
        }
    }

    protected static UriLoaderResponse doInterchangeLoop(HttpURLConnection conn, UriLoaderResponse res, String data, UriLoaderParams params) {
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

            if (res.getCode() >=  HttpURLConnection.HTTP_BAD_REQUEST) {
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
                res = new UriLoaderResponse();
            }
            res.setData(ex.getMessage());
            res.setCode( HttpURLConnection.HTTP_INTERNAL_ERROR);
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
