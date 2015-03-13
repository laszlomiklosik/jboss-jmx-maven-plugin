# Introduction #

In order for this plugin to work you first need to:

1. make your server visible from other machines (by default it is visible only on localhost). For this you only need to start JBoss (run.sh or run.bat) using the

```
-b 0.0.0.0
```

argument.

_Note: If you do not follow this suggestion you will be able to restart a local JBoss server only._

2. think about security

Now that your JBoss server is visible to everybody from your network, you should secure

- the jmx-console (http://localhost:8080/jmx-console)

and

- the web-console (http://localhost:8080/web-console)

using a user/password.

See details on page:

http://code.google.com/p/jboss-jmx-maven-plugin/wiki/Security

_Note: this plugin works without these security settings, however securing your server is encouraged._

# Usage #

**Step 1: Make the jboss-jmx-plugin visible to your (group or local) repository**

For this do any of the following:

a. Add

```
  <repository>
    <id>jboss-jmx-plugin-repository</id>
    <url>http://jboss-jmx-maven-plugin.googlecode.com/svn/maven/repository</url>
  </repository>
```

to your pom.xml or settings.xml.

b. The second option is to add http://jboss-jmx-maven-plugin.googlecode.com/svn/maven/repository as a proxy repository in your group repository (e.g. using Nexus).

c. The third option is to download files:

http://jboss-jmx-maven-plugin.googlecode.com/files/jboss-jmx-plugin-0.1.jar

http://jboss-jmx-maven-plugin.googlecode.com/files/pom.xml

and install them:

-to your group repository (e.g. using Nexus)

or

-to your local repository by executing the below command from the command line:
```
mvn install:install-file -Dfile=/location/where/you/downloaded/jboss-jmx-plugin-0.1.jar -DpomFile=/location/where/you/downloaded/pom.xml
```

d. The fourth option is to build the library yourself (using Maven):

  * svn checkout http://jboss-jmx-maven-plugin.googlecode.com/svn/trunk/ jboss-jmx-maven-plugin-read-only
  * ` mvn install `

**Step 2: Refer the plugin in your pom.xml file and configure the necessary settings**

In your project's pom.xml add the following:
```
<build>
	<plugins>
		<plugin>
			<groupId>com.google.code</groupId>
			<artifactId>jboss-jmx-plugin</artifactId>
			<version>0.1</version>
			<configuration>
				<serverHost>hostname</serverHost>
				<serverPort>8080</serverPort>
				<jmxConsoleUser>adminuser</jmxConsoleUser>
				<jmxConsolePassword>adminpassword</jmxConsolePassword>
				<restartTimeout>90</restartTimeout>
			</configuration>
		</plugin>
	</plugins>
</build>
```

All configuration settings are optional, they use default values unless configured explicitly:

_serverHost_ = the host on which JBoss is running (its default value is _localhost_)

_serverPort_ = the port on which JBoss is listening (its default value is _8080_)

_jmxConsoleUser_ = the name of a user which has permission to restart JBoss via the JMX console (its default value is _admin_)

_jmxConsolePassword_ = the password of the previously mentioned user (its default value is the empty string)

_restartTimeout_ = the maximum time interval in seconds till the plugin waits for JBoss to complete the restart (its default value is _120_). If the restart takes more then this, then the restart attempt is considered failed.


**Step 3: Use it**

execute the `jboss-jmx:restart` maven goal

(e.g. execute ` mvn jboss-jmx:restart ` from the command line in your project's root directory)