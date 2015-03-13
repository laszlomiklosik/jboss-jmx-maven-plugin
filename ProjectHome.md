The main purpose of this project is to make possible restarting a remote JBoss server as part of a Maven 2 build.

Looking at the available Maven 2 plugins (maven cargo plugin, maven jboss plugin) this feature was not implemented yet, however it would help a lot implementing automatic remote deployments to JBoss.

The experiments show that repeated hot re-deployments of an application (war or ear) to JBoss via the JMX console leads to memory leaks and after 5-10 to an out of memory error.

A JBoss restart will solve this: lets restart JBoss before each deployment in the automatic remote deployment implementation of the Continuous Integration solution.

In future additional JMX console features could be added to this plugin.

To get started using it read: http://code.google.com/p/jboss-jmx-maven-plugin/wiki/Usage