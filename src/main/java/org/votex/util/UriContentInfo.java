package org.votex.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UriContentInfo {
    private Integer code;
    private Integer size;
    private String mimeType;
}
