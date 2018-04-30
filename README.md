# KotLink
[![Build Status](https://travis-ci.org/ilya40umov/go-link.png?branch=master)](https://travis-ci.org/ilya40umov/go-link)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

KotLink is a service for creating and sharing memorable URL aliases.

The service will consist of a backend that will perform link resolution 
and UI that will allow users to add/edit/delete aliases/namespaces.
Then, there will be plugins available at least for Chrome and FireFox 
that will resolve aliases entered by the user in Omnibox after first typing `kt`+whitespace.

### Project Status
Project is still in a very early stage and COMPLETELY UNUSABLE. :)

### TODOs
* Multi module project
* Code coverage plugin
* Detekt plugin (static code analysis)
* CRUD endpoint for aliases
* env (Docker, Postgres, compose)
* Repositories with https://github.com/JetBrains/Exposed https://github.com/bastman/spring-kotlin-exposed/blob/master/rest-api/src/main/kotlin/com/example/api/bookz/db/bookzRepo.kt
* vivaldi instructions
* chrome plugin
* firefox plugin
* frontend with material and angular ???
* security (oath)
