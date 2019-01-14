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

### Configuration

All of the settings that will be mentioned below (e.g. `SPRING_DATASOURCE_URL`) 
should be provided to KotLink app as environment variables. 
For example with Docker this can be achieved with running the container with multiple `-e` flags.

#### Connect To Database

* `SPRING_DATASOURCE_URL` - JDBC connection URL, 
 e.g. `jdbc:postgresql://localhost:5432/kotlink`
* `SPRING_DATASOURCE_USERNAME` - username that is used to access Postgres, 
 e.g. `kotlinkuser`
* `SPRING_DATASOURCE_PASSWORD` - password that is used to access Postgres, 
 e.g. `kotlinkpass`

#### Obtain OAuth2 Client ID

1. Go to *Credentials* in your [Google API Console](https://console.developers.google.com)
1. Click on *Create credentials* button, choose *OAuth client ID*, and then select *Web application*.
1. On the next page, enter any name you see fit, and add the following URL `http://${YOUR_KOTLINK_SERVER_ADDRESS}/login` 
under *Authorized redirect URLs*, where `${YOUR_KOTLINK_SERVER_ADDRESS}` 
should be replaced with the domain name / the external ip address of your KotLink server.
E.g. The OAuth2 Client ID for local development has `http://localhost:8080/login` added to *Authorized redirect URLs*.
1. Save the generated client ID and client secret for the next step.
 
#### Set Up OAuth2
 
* `SECURITY_OAUTH2_CLIENT_CLIENT_ID` - OAuth2 client ID obtained from [Google API Console](https://console.developers.google.com),
 e.g. `115327279391-cqrf3suvt416skdkr8lqvdntgfa90epg.apps.googleusercontent.com`
* `SECURITY_OAUTH2_CLIENT_CLIENT_SECRET` - OAuth2 client secret obtained from Google API Console,
 e.g. `SZDICodbaLAkNXjbFKfOFZCO`

#### Limit Who Can Access Your Server

* `KOTLINK_SECURITY_ADMIN_EMAIL` - email of the user who has admin privileges and owns the default namespace,
* `KOTLINK_SECURITY_RESTRICT_EDITS_TO_IP_REGEX` - optional regex that limits IPs from which edits can be performed,
* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` - an array of user emails that can access the server, 
 e.g. `user1@gmail.com,user2@gmail.com`; set to empty array by default.
* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` - a [Java regular expression](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) 
 of user emails that can access the server, e.g. `.*` for everybody, `^$` for nobody,
 and `.*@gmail\.com$` for everything that ends with *gmail.com*; set to `.*` by default.

Please, note that `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` and `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` checks 
are combined using **OR**, and thus if you want to allow only a set of specific users to access the server,
you should set the regex to `^$` (which matches nothing) and emails array to whatever your users' addresses look like.

#### Tune Tomcat If Behind ELB / Reverse Proxy

Most likely you will be running KotLink behind ELB or some other reverse proxy, 
and in this case you will want to set the following properties 
to make sure Tomcat is handing *X-Forwarded-* headers correctly: 

* `SERVER_USE_FORWARD_HEADERS` - must be set to `true`
* `SERVER_TOMCAT_REMOTE_IP_HEADER` - must be set to the remote IP header used by your proxy/LB, most likely `x-forwarded-for`
* `SERVER_TOMCAT_PROTOCOL_HEADER` - must be set to the protocol header used by your proxy/LB, most likely `x-forwarded-proto`
* `SERVER_TOMCAT_INTERNAL_PROXIES` - may need to be set to a regex matching IPs of your proxy/LB nodes,
e.g. `10\.\d{1,3}\.\d{1,3}\.\d{1,3}|192\.168\.\d{1,3}\.\d{1,3}`; 
see [RemoveIpValve](https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/valves/RemoteIpValve.html)
and [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-customize-tomcat-behind-a-proxy-server)
for more details.

#### Use Redis As Cache And Session Storage

To enable using Redis as storage for caches (by default lives in memory) 
and session information (by default is stored in Postgres), 
which in turn will help improving performance and availability 
(e.g. without a stand-alone cache you can only have a single-node deployment, 
as local caches on each node will quickly become stale and cause issues),
you can tweak the following properties:

* `SPRING_REDIS_URL` - must be set to `redis://username:password@your-redis-host:6379` 
(where `username:password` pair is optional and `username` is always ignored)
* `SPRING_CACHE_TYPE` - must be set to `redis`
* `SPRING_SESSION_STORE_TYPE` - must be set to `redis`

#### Monitoring

KotLink server also exposes some endpoints that help with monitoring how it's doing:

* `/actuator/health` - (unprotected) reports health of the application
* `/actuator/metrics` and `/actuator/metrics/{metric}` - 
(behind Basic Auth) allow checking the list of metric names and their concrete values
* `/actuator/prometheus` - (behind Basic Auth) allows fetching all metric values in Prometheus format

To access protected endpoint, KotLink has a special user with name `kotlinkactuator` password `kotlinkpass`.
This user can't access anything other than `/actuator/*` 
and can be changed to have a different name / password via the following two properties:

* `SPRING_SECURITY_USER_NAME` - defaults to `kotlinkactuator`
* `SPRING_SECURITY_USER_PASSWORD` - defaults to `kotlinkpass`