package org.votex.chrome;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.votex.util.Morpheus;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ChromeCookieProvider extends Morpheus implements CookieProvider {

    static ChromeOptions options = new ChromeOptions();

    static {
        options.addArguments("--remote-allow-origins=*");
    }

    public ChromeCookieProvider() {
    }

    @Override
    public String acquireSiteCookies(String url) {
        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            yeldMs(500);
            driver.get(url);
            return driver.manage().getCookies().stream()
                    .map(e -> e.getName() + "=" + e.getValue())
                    .collect(Collectors.joining("; "));
        }
        catch (Exception ex) {
            log.error("Error getting cookies from {}: {}", url, ex.getLocalizedMessage(), ex);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return null;
    }
}
