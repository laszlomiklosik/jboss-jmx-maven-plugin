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

/**
 * @goal restart
 */
public class JBossRestartMojo extends AbstractMojo {

	private static final String SUCCESS = "success";

	private static final String HOST_PLACEHOLDER = "{host}";

	private static final String PORT_PLACEHOLDER = "{port}";

	private static final String SERVER_RESTART_URL = "http://"
			+ HOST_PLACEHOLDER
			+ ":"
			+ PORT_PLACEHOLDER
			+ "/jmx-console/HtmlAdaptor?action=invokeOp&name=jboss.system:type=Server&methodIndex=6&arg0=10";

	private static final String SERVER_INSPECT_URL = "http://"
			+ HOST_PLACEHOLDER
			+ ":"
			+ PORT_PLACEHOLDER
			+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.system:type=Server";

	private static final int RESTART_CHECK_INTERVAL_SECONDS = 10;

	private static final int PROGRESS_DIVISION_INTERVAL_SECONDS = 1;

	private static final int MAX_CHECKS_FOR_RESTART = 6;

	/**
	 * @parameter expression="${restart.host}" default-value="localhost"
	 */
	private String serverHost;

	/**
	 * @parameter expression="${restart.port}" default-value="8080"
	 */
	private String serverPort;

	/**
	 * @parameter expression="${restart.jmxConsoleUser}" default-value=null
	 */
	private String jmxConsoleUser;

	/**
	 * @parameter expression="${restart.jmxConsolePassword}" default-value=null
	 */
	private String jmxConsolePassword;

	public void execute() throws MojoExecutionException, MojoFailureException {

		URL url;
		try {
			url = new URL(SERVER_RESTART_URL.replace(HOST_PLACEHOLDER,
					serverHost).replace(PORT_PLACEHOLDER, serverPort));
			URLConnection conn = url.openConnection();
			authorizeConnection(conn);

			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));

			String response;
			boolean restartInitiated = false;

			while (!restartInitiated && (response = in.readLine()) != null) {
				if (response.toLowerCase().contains(SUCCESS)) {
					restartInitiated = true;
					getLog().info("JBoss restart initiated succesfully.");
					getLog().info("Waiting for restart to complete.");
				}
			}

			int attemptCounter = 0;
			boolean restartComplete = false;
			while (attemptCounter < MAX_CHECKS_FOR_RESTART) {
				attemptCounter++;
				getLog()
						.info(
								"Waiting "
										+ RESTART_CHECK_INTERVAL_SECONDS
										+ " seconds before checking whether JBoss finished restarting.");
				waitBetweenChecks();
				restartComplete = isRestartCompleted();
				if (restartComplete) {
					getLog().info("Server has been successfully restarted.");
					break;
				}
			}

			if (!restartComplete) {
				getLog().info(
						"Could not restart JBoss in "
								+ RESTART_CHECK_INTERVAL_SECONDS
								* MAX_CHECKS_FOR_RESTART + " seconds");
			}

			in.close();

		} catch (IOException e) {
			getLog().error(e);
			throw new MojoFailureException(
					"JBoss JMX console not available. Cannot restart JBoss.");
		} catch (Exception e) {
			throw new MojoExecutionException(
					"An unknown error occured while trying to restart JBoss.");
		}

	}

	private void authorizeConnection(URLConnection conn) {
		if (jmxConsoleUser != null && jmxConsolePassword != null) {
			String authorizationString = jmxConsoleUser + ":"
					+ jmxConsolePassword;
			String encoding = new BASE64Encoder().encode(authorizationString
					.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + encoding);
		}
	}

	private void waitBetweenChecks() {
		int divisionsCount = RESTART_CHECK_INTERVAL_SECONDS
				/ PROGRESS_DIVISION_INTERVAL_SECONDS;
		for (int i = 0; i < divisionsCount; i++) {
			try {
				Thread.sleep(PROGRESS_DIVISION_INTERVAL_SECONDS * 1000);
			} catch (InterruptedException e) {
				getLog().error("Error occured while waiting between checks.");
			}
			getLog().info(".");
		}

	}

	/**
	 * Restart is complete after the JMX console is available again.
	 * 
	 * @return true if the the restart finished false otherwise.
	 */
	private boolean isRestartCompleted() {
		boolean restartSucessful = false;
		try {
			URL url = new URL(SERVER_INSPECT_URL.replace(HOST_PLACEHOLDER,
					serverHost).replace(PORT_PLACEHOLDER, serverPort));
			URLConnection conn = url.openConnection();
			authorizeConnection(conn);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			
			in.close();
			
			/*String response = null;
			while((response = in.readLine()) != null) {
				// eventually check for the word "Started"
				System.out.println(response);
			}*/

			
			restartSucessful = true;
		} catch (MalformedURLException e) {
			restartSucessful = false;
			getLog().error(
					"JMX console URL" + SERVER_INSPECT_URL
							+ " is not correctly formed.");
		} catch (IOException e) {
			restartSucessful = false;
		}
		return restartSucessful;
	}
}
