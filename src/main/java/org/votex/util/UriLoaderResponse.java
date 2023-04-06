package org.votex.util;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UriLoaderResponse {
    private int code;
    private String data;

    private byte[] binaryData;

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
