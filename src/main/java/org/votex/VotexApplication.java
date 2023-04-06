package org.votex;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.votex.target.Configuration;
import org.votex.target.RequestRunner;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "votex", mixinStandardHelpOptions = true, version = "votex 0.0.1", description = "app")
@Slf4j
public class VotexApplication implements Callable<Integer>, Configuration {

	@Getter
	@Option(names = {"-c", "--cookie"}, description = "Browser cookie", required = true)
	private String[] cookies = new String[] {};

	@Getter
	@Option(names = {"-D", "--data"}, description = "Payload data ((default - inp-12-0=0&vote=1)")
	private String payload = "inp-1-0=0&inp-2-0=2&vote=1";

	@Getter
	@Option(names = {"-d", "--delay"}, description = "Delay after successful attempt, seconds (default - 5)")
	private Integer delayNext = 5;

	@Getter
	@Option(names = {"-p", "--delay-post"}, description = "Delay after GET/before POST, seconds (default - 5)")
	private Integer delayPost = 5;

	@Getter
	@Option(names = {"-r", "--delay-retry"}, description = "Delay before trying next proxy, seconds (default - 1)")
	private Integer delayProxyRetry = 1;

	@Getter
	@Option(names = {"-u", "--proxy-list-url"}, description = "List of proxies (default is https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt)")
	private String proxyListUrl = "https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt";

	@Getter
	@Option(names = {"-G", "--get"}, description = "URL for making GET request")
	private String sourceForGET = "https://www.kp.ru/best/msk/oprosy/tula_klinikagoda2023";

	@Getter
	@Option(names = {"-P", "--post"}, description = "URL for making GET request")
	private String targetForPOST = "https://www.kp.ru/best/msk/oprosy/tula_klinikagoda2023";

	@Getter
	@Option(names = {"-n", "--attempts"}, description = "Total Number of POSTs to do (default - 1)")
	private Integer numberOfReqs = 1;

	@Getter
	@Option(names = {"--no-proxy"}, description = "Direct mode - do not use proxies (default - off)")
	private Boolean directMode = false;

	@Option(names = {"-a", "--auto"}, description = "Auto cookie mode - launch Browser in background")
	private Boolean autoCookie = false;
	public Boolean autoCookie() { return autoCookie; }


	public static void main(String[] args) {
		new VotexApplication().run(args);
	}

	public void run(String... args) {
		new CommandLine(this).execute(args);
	}

	@Override
	public Integer call() throws Exception {
		return new RequestRunner(this).run();
	}
}
