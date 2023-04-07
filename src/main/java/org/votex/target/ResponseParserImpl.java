package org.votex.target;

import lombok.extern.slf4j.Slf4j;
import org.votex.VotexApplication;
import org.votex.proxy.ProxyDef;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

@Slf4j
public class ResponseParserImpl implements ResponseParser {
    private static final Pattern SPACER = Pattern.compile(" ");
    @Override
    public Integer parseInteger(String source, ParserOptions opts) {
        try (BufferedReader input = new BufferedReader(new StringReader(source))) {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains(opts.getHeader()) && line.contains(opts.getQuestion())) {
                    while ((line = input.readLine()) != null) {
                        if (line.contains(opts.getParticipant()) && line.contains(opts.getFooter())) {
                            while ((line = input.readLine()) != null) {
                                if (line.contains(opts.getResult())) {
                                    return parseResult(line, opts.getResult());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to load proxy list: {}", ex);
        }
        return null;
    }

    private Integer parseResult(String line, String header) {
        return Integer.parseInt(
                line.substring(line.indexOf(header) + header.length())
                .trim().split(SPACER.pattern())[0]
        );
    }

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        ResponseParserImpl i = new ResponseParserImpl();
        String data = null;
        try {
            data = new String(new FileInputStream("D:\\projects\\smartech\\votex\\test\\my\\data.j").readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Integer res = i.parseInteger(data, new ParserOptions() {
            @Override
            public String getQuestion() {
                return "2. «Лучшая клиника инновационных методов лечения в офтальмологии»";
            }

            @Override
            public String getParticipant() {
                return "АНО «Клиника микрохирургии глаза ВЗГЛЯД®»";
            }

            @Override
            public String getHeader() {
                return "<span class=\"noimg\">";
            }

            @Override
            public String getFooter() {
                return "</span>";
            }

            @Override
            public String getResult() {
                return "<span class=\"unicredit_poll_results_count\">";
            }
        });
        System.out.println("res: " + res);
    }
}
