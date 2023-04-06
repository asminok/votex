package org.votex.target;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Headers {
    private static final Random rnd = new Random();
    public static Map ofGet() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authority", "www.kp.ru");
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("accept-language", "en-US,en;q=0.9");
        headers.put("sec-ch-ua", "\"Microsoft Edge\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "document");
        headers.put("sec-fetch-mode", "navigate");
        headers.put("sec-fetch-site", "none");
        headers.put("sec-fetch-user", "?1");
        headers.put("upgrade-insecure-requests", "1");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.54");
        return headers;
    }

    public static Map ofPost() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authority", "www.kp.ru");
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("accept-language", "en-RU,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6");
        headers.put("cache-control", "max-age=0");
        headers.put("content-type", "application/x-www-form-urlencoded");
        //headers.put("cookie", "uua=89f6d7a64c4f604e4f579fd7e6133177; _gid=GA1.2.983848565.1680770145; _ym_uid=1680770145221038397; _ym_d=1680770145; _ym_isad=1; _ga=GA1.2.1420619942.1680770145; _gat_UA-23870775-5=1; _ga_8MQ0FGXD1P=GS1.1.1680779789.7.0.1680779789.0.0.0; _ga_Q5YRVQY3FS=GS1.1.1680779789.7.0.1680779789.0.0.0; _ga_R7DD899R0W=GS1.1.1680779451.7.1.1680779794.0.0.0");
        headers.put("origin", "https://www.kp.ru");
        headers.put("referer", "https://www.kp.ru/best/msk/oprosy/tula_klinikagoda2023");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "document");
        headers.put("sec-fetch-mode", "navigate");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("sec-fetch-user", "?1");
        headers.put("upgrade-insecure-requests", "1");
        headers.put("user-agent", mkRandomAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/", "(KHTML, like Gecko) Chrome/111.0.0.0 Safari/"));
        return headers;
    }

    private static String mkRandomAgent(String head, String tail) {
        final StringBuilder sb = new StringBuilder(head);
        final String V = "" + rnd.nextInt(32768) + "." + rnd.nextInt(128);
        return sb.append(V).append(tail).append(V).toString();
    }
}
