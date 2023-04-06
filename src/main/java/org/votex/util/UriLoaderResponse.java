package org.votex.util;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class UriLoaderResponse {
    private int code;
    private String data;
    private byte[] binaryData;
    private Map<String, String> headers;

    public UriLoaderResponse(int code) {
        this.code = code;
        this.data = null;
        this.binaryData = null;
    }

    public UriLoaderResponse(int code, String data) {
        this.code = code;
        this.data = data;
    }

    public UriLoaderResponse(int code, byte[] binaryData) {
        this.code = code;
        this.binaryData = binaryData;
    }
}
