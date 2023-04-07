package org.votex;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.votex.chrome.ChromeCookieProvider;
import org.votex.target.Configuration;
import org.votex.target.RequestRunner;
import org.votex.target.ResponseParserImpl;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "votex", mixinStandardHelpOptions = true, version = "votex 0.0.1", description = "\n")
@Slf4j
public class VotexApplication implements Callable<Integer>, Configuration {

	@ArgGroup(exclusive = true, multiplicity = "1")
	CookieOptions cookieOptions;

	static class CookieOptions {
		@Option(names = {"-a", "--auto"}, description = "Auto cookie mode - launch a Browser", required = true)
		private Boolean autoCookie = false;
		@Option(names = {"-c", "--cookie"}, description = "Browser cookie", required = true)
		private String[] cookies = new String[] {};
	}

	public Boolean autoCookie() { return cookieOptions.autoCookie; }
	public String[] getCookies() { return cookieOptions.cookies; }

	@Getter
	@Option(names = {"-d", "--data"}, description = "Payload data ((default - inp-12-0=0&vote=1)")
	private String payload = "inp-1-0=0&inp-2-0=2&vote=1";

	@Getter
	@Option(names = {"--delay"}, description = "Delay after successful attempt, seconds (default - 5)")
	private Integer delayNext = 5;

	@Getter
	@Option(names = {"--delay-post"}, description = "Delay after GET/before POST, seconds (default - 5)")
	private Integer delayPost = 5;

	@Getter
	@Option(names = {"-r", "--delay-retry"}, description = "Delay before trying next proxy, seconds (default - 1)")
	private Integer delayProxyRetry = 1;

	@Getter
	@Option(names = {"-u", "--proxy-list-url"}, description = "List of proxies (default is https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt)")
	private String proxyListUrl = "https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt";

	@Getter
	@Option(names = {"-G", "--get"}, description = "URL for making GET request")
	private String sourceForGET = "https://eoaowsdhxrrl8on.m.pipedream.net";

	@Getter
	@Option(names = {"-P", "--post"}, description = "URL for making GET request")
	private String targetForPOST = "https://eoaowsdhxrrl8on.m.pipedream.net";

	@Getter
	@Option(names = {"-n", "--attempts"}, description = "Total Number of POSTs to do (default - 1)")
	private Integer numberOfReqs = 1;

	@Getter
	@Option(names = {"--no-proxy"}, description = "Direct mode - do not use proxies (default - off)")
	private Boolean directMode = false;

	// Parser options
	@Getter
	@Option(names = {"-q", "--question"}, description = "Question (i.e. \"9. «Лучшая клиника инновационных методов»\")", required = true)
	String question = null;
	@Getter
	@Option(names = {"-p", "--participant"}, description = "Participant (i.e. \"OАО «Клиника микрохирургии глаза»\")", required = true)
	String participant = null;
	@Getter
	@Option(names = {"--header"}, description = "Participant head marker (default - <span class=\"noimg\">)")
	String header = "<span class=\"noimg\">";
	@Getter
	@Option(names = {"--footer"}, description = "Question tail marker (default - </span>)")
	String footer = "</span>";
	@Getter
	@Option(names = {"--result"}, description = "Resulting score head marker (default - <span class=\"unicredit_poll_results_count\">)")
	String result = "<span class=\"unicredit_poll_results_count\">";
	@Getter
	@Option(names = {"--no-score"}, description = "Do not check the resulting score (default - false)")
	Boolean checkScore = true;

	public static void main(String[] args) {
		new VotexApplication().run(args);
	}

	public void run(String... args) {
		new CommandLine(this).execute(args);
	}

	@Override
	public Integer call() throws Exception {
		return new RequestRunner(this, new ChromeCookieProvider(), new ResponseParserImpl()).run();
	}
}
