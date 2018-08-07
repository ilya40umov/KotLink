# KotLink - Deployment Guide

### Prerequisites

* In order to deploy KotLink server, one needs to have an instance of PostgreSQL already running in their network.
This can be achieved by installing Postgres on one of your servers 
[directly](https://www.postgresql.org/docs/current/static/tutorial-install.html),
or by using [the official docker image](https://www.postgresql.org/docs/current/static/tutorial-install.html),
or by provisioning an instance of Postgres from your cloud provider 
(e.g. [Postgres on RDS](https://aws.amazon.com/rds/postgresql/)).
* The second requirement is [G Suite](https://gsuite.google.com/) 
or if one's team is really small they can let everybody use their personal *gmail* accounts to authenticate.

#### Configure database

* `SPRING_DATASOURCE_URL` - JDBC connection URL, 
 e.g. `jdbc:postgresql://localhost:5432/kotlink`
* `SPRING_DATASOURCE_USERNAME` - username that is used to access Postgres, 
 e.g. `kotlinkuser`
* `SPRING_DATASOURCE_PASSWORD` - password that is used to access Postgres, 
 e.g. `kotlinkpass`

#### Obtain OAuth2 Client ID

1. Go to *Credentials* in your [Google API Console](https://console.developers.google.com)
1. Click on *Create credentials* button, choose *OAuth client ID*, and then select *Web application*.
1. On the next page, enter any name you see fit, and add the following URL `http://YOUR_SERVER_ADDRESS/login` 
under *Authorized redirect URLs*, where `YOUR_SERVER_ADDRESS` 
should be replaced with the domain name / the external ip address of your KotLink server.
E.g. The OAuth2 Client ID for local development has `http://localhost:8080/login` added to *Authorized redirect URLs*.
 
#### Set Up OAuth2
 
* `SECURITY_OAUTH2_CLIENT_CLIENT_ID` - OAuth2 client ID obtained from [Google API Console](https://console.developers.google.com),
 e.g. `115327279391-cqrf3suvt416skdkr8lqvdntgfa90epg.apps.googleusercontent.com`
* `SECURITY_OAUTH2_CLIENT_CLIENT_SECRET` - OAuth2 client secret obtained from Google API Console,
 e.g. `SZDICodbaLAkNXjbFKfOFZCO`

#### Limit Who Can Access Your Server

* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` - an array of user emails that can access the server, 
 e.g. `user1@gmail.com,user2@gmail.com`; set to empty array by default.
* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` - a [Java regular expression](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) 
 of user emails that can access the server, e.g. `.*` for everybody, `^$` for nobody,
 and `.*@gmail\.com$` for everything that ends with *gmail.com*; set to `.*` by default.

Please, note that `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` and `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` checks 
are combined using **OR**, and thus if you want to allow only a set of specific users to access the server,
you should set the regex to `^$` (which matches nothing) and emails array to whatever your users' addresses look like.

#### KotLink Behind ELB / Reverse Proxy

If you will be running KotLink behind ELB or some other reverse proxy, 
you will most likely want to set the following properties: 

* `SERVER_USE_FORWARD_HEADERS` - must be set to `true`
* `SERVER_TOMCAT_REMOTE_IP_HEADER` - must be set to the remote IP header used by your proxy/LB, most likely `x-forwarded-for`
* `SERVER_TOMCAT_PROTOCOL_HEADER` - must be set to the protocol header used by your proxy/LB, most likely `x-forwarded-proto`
* `SERVER_TOMCAT_INTERNAL_PROXIES` - may need to be set to a regex matching IPs of your proxy/LB nodes,
e.g. `10\.\d{1,3}\.\d{1,3}\.\d{1,3}|192\.168\.\d{1,3}\.\d{1,3}`; 
see [RemoveIpValve](https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/valves/RemoteIpValve.html)
and [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-customize-tomcat-behind-a-proxy-server)
for more details.