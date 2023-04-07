package org.votex.target;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
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
            log.error("Failed to parse scores: {}", ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private Integer parseResult(String line, String header) {
        return Integer.parseInt(
                line.substring(line.indexOf(header) + header.length())
                .trim().split(SPACER.pattern())[0]
        );
    }
}
