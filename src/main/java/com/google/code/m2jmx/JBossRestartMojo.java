package com.google.code.m2jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import sun.misc.BASE64Encoder;

// GIT TEST 1

/**
 * JBoss restart mojo - use to restart a JBoss server via its JMX console.
 * 
 * @goal restart
 */
public class JBossRestartMojo extends AbstractMojo {

	/**
	 * Magic string searched up in the response coming from the JMX console.<br>
	 * Use to determine whether the restart via JMX was successfully initiated
	 * or not.
	 */
	private static final String SUCCESS = "success";

	/**
	 * JBoss restart via JMX URL.
	 */
	private static final String SERVER_RESTART_URL = "/jmx-console/HtmlAdaptor?action=invokeOpByName&"
			+ "name=jboss.system:type=Server&methodName=exit&argType=int&arg0=10";

	/**
	 * JBoss server state inspection URL. If this URL is alive the JBoss restart
	 * is considered complete.
	 */
	private static final String SERVER_INSPECT_URL = "/jmx-console/HtmlAdaptor?action=inspectMBean&"
			+ "name=jboss.system:type=Server";

	/**
	 * The time interval between 2 consecutive server state checks.
	 */
	private static final int RESTART_CHECK_INTERVAL_SECONDS = 5;

	/**
	 * Hostname (or IP) of the server on which JBoss is running.
	 * 
	 * @parameter expression="${restart.host}" default-value="localhost"
	 */
	private String serverHost;

	/**
	 * Port on which the JBoss server is listening.
	 * 
	 * @parameter expression="${restart.port}" default-value="8080"
	 */
	private String serverPort;

	/**
	 * The user name used to access the JMX console.
	 * 
	 * @parameter expression="${restart.jmxConsoleUser}" default-value="admin"
	 */
	private String jmxConsoleUser;

	/**
	 * The password used to access the JMX console.
	 * 
	 * @parameter expression="${restart.jmxConsolePassword}" default-value=""
	 */
	private String jmxConsolePassword;

	/**
	 * Restart mojo timeout. If more time is elapsed then this value, <br>
	 * then the mojo is considered failed.
	 * 
	 * @parameter expression="${restart.restartTimeout}" default-value=120
	 */
	private int restartTimeout;

	/**
	 * The moment when this Mojo is started.
	 */
	private long mojoStartTime;

	/**
	 * Implementation of inherited abstract mojo method.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		mojoStartTime = System.currentTimeMillis();

		try {
			initiateRestart();
			waitForRestartToComplete();
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoFailureException(
					"JBoss JMX console not available or the supplied user/password is not correct. "
							+ "Cannot restart JBoss.");
		}

		logMojoDuration();
	}

	/**
	 * Logs the duration of this mojo to the Maven console.
	 */
	private void logMojoDuration() {
		final long mojoDurationSeconds = (System.currentTimeMillis() - mojoStartTime) / 1000;
		if (mojoDurationSeconds < 60) {
			getLog().info(
					"JBossRestartMojo took " + mojoDurationSeconds
							+ " seconds.");
		} else {
			String minutes = mojoDurationSeconds / 60 + "";
			String seconds = mojoDurationSeconds % 60 + "";
			if (minutes.length() < 2) {
				minutes = "0" + minutes;
			}
			if (seconds.length() < 2) {
				seconds = "0" + seconds;
			}
			getLog().info(
					"JBossRestartMojo took (MM:SS) " + minutes + ":" + seconds);
		}
	}

	/**
	 * Adapts the URL to use the effective settings.
	 */
	private String processURL(String url) {
		return "http://" + serverHost + ":" + serverPort + url;
	}

	/**
	 * Connects to an url by using the authorization settings from the mojo
	 * configuration.
	 */
	private URLConnection connect(String urlString) throws IOException,
			MojoExecutionException {
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Unparsable URL: " + urlString);
		}
		URLConnection conn = url.openConnection();
		String authorizationString = jmxConsoleUser + ":" + jmxConsolePassword;
		String encoding = new BASE64Encoder().encode(authorizationString
				.getBytes());
		conn.setRequestProperty("Authorization", "Basic " + encoding);
		return conn;

	}

	/**
	 * Initiates a restart via the JMX console.
	 */
	private void initiateRestart() throws IOException, MojoExecutionException {
		String restartURL = processURL(SERVER_RESTART_URL);
		URLConnection conn = connect(restartURL);
		BufferedReader in = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		String response = null;
		boolean restartInitiated = false;

		while (!restartInitiated && (response = in.readLine()) != null) {
			if (response.toLowerCase().contains(SUCCESS)) {
				restartInitiated = true;
				getLog().info("Restart was succesfully initiated.");
				getLog().info("Waiting for restart to complete.");
			}
		}
	}

	/**
	 * Waits for the restart to complete.
	 */
	private void waitForRestartToComplete() throws MojoExecutionException {

		int stateCheckCounter = 0;
		int maxStateChecks = restartTimeout / RESTART_CHECK_INTERVAL_SECONDS;
		boolean restartCompleted = false;

		while (stateCheckCounter < maxStateChecks) {
			stateCheckCounter++;
			waitBetweenChecks();
			getLog().info("Checking server state.");
			restartCompleted = isRestartComplete();
			if (restartCompleted) {
				getLog().info("Server was successfully restarted.");
				break;
			}
		}

		if (!restartCompleted) {
			throw new MojoExecutionException("Could not restart JBoss in "
					+ restartTimeout + " seconds. You should increase the "
					+ "restartTimeout configuration setting in your pom.xml.");
		}
	}

	/**
	 * Waits between 2 consecutive checks of server state and display time
	 * progress.
	 */
	private void waitBetweenChecks() {
		for (int i = 0; i < RESTART_CHECK_INTERVAL_SECONDS; i++) {
			try {
				Thread.sleep(1000);
				// display progress every second.
				getLog().info(".");
			} catch (InterruptedException e) {
				getLog().error("Error occured while waiting between checks.");
			}
		}
	}

	/**
	 * Checks whether the restart finished or still in progress. <br>
	 * Restart is considered complete after the JMX console's server inspect
	 * page can be loaded again.
	 * 
	 * @return true if the the restart finished false otherwise.
	 */
	private boolean isRestartComplete() throws MojoExecutionException {
		boolean restartComplete = false;
		try {
			String inspectURL = processURL(SERVER_INSPECT_URL);
			URLConnection conn = connect(inspectURL);

			// if the stream cannot be created (IOException), then the restart
			// did not finish yet.
			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));

			in.close();
			restartComplete = true;
		} catch (IOException e) {
			restartComplete = false;
			getLog().info("Server restart is in progress.");
		}
		return restartComplete;
	}
}