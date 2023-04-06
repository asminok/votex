package org.votex.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Morpheus {
    protected void yeld(Integer seconds) {
        yeldMs(seconds * 1000);
    }
    protected void yeldMs(Integer milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            log.error("Cannot make a(n) {} ms break: {}", milliSeconds, ex.getMessage(), ex);
        }
    }
}
