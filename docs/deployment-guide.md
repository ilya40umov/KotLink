# KotLink - Deployment Guide

### Supported Environment Variables

* `SPRING_DATASOURCE_URL` - JDBC connection URL, 
 e.g. `jdbc:postgresql://localhost:5432/kotlink`
* `SPRING_DATASOURCE_USERNAME` - username that is used to access Postgres, 
 e.g. `kotlinkuser`
* `SPRING_DATASOURCE_PASSWORD` - password that is used to access Postgres, 
 e.g. `kotlinkpass`
* `SECURITY_OAUTH2_CLIENT_CLIENT_ID` - OAuth2 client ID obtained from [Google API Console](https://console.developers.google.com),
 e.g. `115327279391-cqrf3suvt416skdkr8lqvdntgfa90epg.apps.googleusercontent.com`
* `SECURITY_OAUTH2_CLIENT_CLIENT_SECRET` - OAuth2 client secret obtained from Google API Console,
 e.g. `SZDICodbaLAkNXjbFKfOFZCO`
* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` - (empty by default) - an array of user emails that can access the server, 
 e.g. `user1@gmail.com,user2@gmail.com`
* `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` - (`.*` by default) - a [Java regular expression](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) 
 of user emails that can access the server, e.g. `.*` for everybody, `^$` for nobody,
 and `.*@gmail\.com$` for everything that ends with *gmail.com*

Please, note that `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAILS` and `KOTLINK_SECURITY_OAUTH_ALLOWED_EMAIL_REGEX` 
are combined using **OR**, and thus if you want to allow only a set of specific users to access the server,
you should set the regex to `^$` and emails array to whatever your users' addresses look like.

### Linux: Deployment Instructions

Coming soon 

### AWS: Deployment Instructions

Coming soon