package org.votex.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UriLoaderParams {

    int connectTimeout; // milliseconds
    int readTimeout;  // milliseconds
    int maxContentSize; // bytes
    boolean binaryMode; // treat response as binary stream

    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    public static final int DEFAULT_MAX_CONTENT_SIZE = 10 * 1024 * 1024 ;
    public static final boolean DEFAULT_BINARY_MODE = false;

    public static final UriLoaderParams DEFAULT_PARAMS = new UriLoaderParams(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_MAX_CONTENT_SIZE, DEFAULT_BINARY_MODE);
}


