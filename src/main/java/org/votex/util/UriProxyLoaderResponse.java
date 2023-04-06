package org.votex.util;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class UriProxyLoaderResponse extends UriLoaderResponse {
    private Map<String, String> headers;
}
