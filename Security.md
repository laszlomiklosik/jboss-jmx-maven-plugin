# Introduction #

If you expose your JBoss server over the network (by starting the server using option -b 0.0.0.0 or by changing the first occurrence of variable jboss.bind.address to 0.0.0.0 in .../jboss/server/default/deploy/web-deployer/server.xml) you automatically expose also the JBoss web-console (http://localhost:8080/web-console) and jmx-console (http://localhost:8080/jmx-console) administration tools without any user/password.

Using these applications anybody from the network can e.g. stop your server, undeploy an application, deploy a different application etc. It is a good idea to secure it via a password (especially when talking about a production or UAT app server).

_Note: in the below setting examples I assumed that the "default" JBoss server configuration is used._

# Securing the jmx-console #

1) in .../jboss/server/default/deploy/jmx-console.war/WEB-INF/jboss-web.xml uncomment:
```
 <security-domain>java:/jaas/jmx-console</security-domain>
```
2) in .../jboss/server/default/deploy/jmx-console.war/WEB-INF/web.xml  uncomment:

```
   <security-constraint>
     <web-resource-collection>
       <web-resource-name>HtmlAdaptor</web-resource-name>
       <description>An example security config that only allows users with the
         role JBossAdmin to access the HTML JMX console web application
       </description>
       <url-pattern>/*</url-pattern>
       <http-method>GET</http-method>
       <http-method>POST</http-method>
     </web-resource-collection>
     <auth-constraint>
       <role-name>JBossAdmin</role-name>
     </auth-constraint>
   </security-constraint>
```
3) in .../jboss/server/default/conf/props/jmx-console-users.properties add/edit line:
admin=a\_password\_you\_like

  * To assign a different user to the JBossAdmin group add "username=JBossAdmin" to the jmx-console-roles.properties.properties file (from the same directory).

  * The existing jmx-console-users.properties file has an admin user with the password admin. For security, either remove the user or change the password to  a stronger one.

# Securing the web-console #

1) in .../jboss/server/default/deploy/management/console-mgr.sar/web-console.war/WEB-INF/jboss-web.xml uncomment:
```
<security-domain>java:/jaas/web-console</security-domain>
```
2) in .../jboss/server/default/deploy/management/console-mgr.sar/web-console.war/WEB-INF/web.xml uncomment:
```
   <security-constraint>
   <web-resource-collection>
   <web-resource-name>HtmlAdaptor</web-resource-name>
   <description>An example security config that only allows users with the
   role JBossAdmin to access the HTML JMX console web application
   </description>
   <url-pattern>/*</url-pattern>
   <http-method>GET</http-method>
   <http-method>POST</http-method>
   </web-resource-collection>
   <auth-constraint>
   <role-name>JBossAdmin</role-name>
   </auth-constraint>
   </security-constraint>
```
3) in .../jboss/server/default/deploy/management/console-mgr.sar/web-console.war/WEB-INF/classes/web-console-users.properties add/edit line:

admin=a\_password\_you\_like

  * To assign a different user to the JBossAdmin group add "username=JBossAdmin" to the web-console-roles.properties.properties file (from the same directory).

  * The existing web-console-users.properties file has an admin user with the password admin. For security, either remove the user or change the password to  a stronger one.

Remarks:

- the Maven 2 jboss-jmx-plugin uses the JMX console (same way as maven-cargo-plugin does)

- the web console is not needed, thus it should be disabled (by removing/commenting out the user/password line from web-console-users.properties)