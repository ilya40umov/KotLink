# KotLink
[![Build Status](https://travis-ci.org/ilya40umov/KotLink.png?branch=master)](https://travis-ci.org/ilya40umov/KotLink)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1a55315857b44bb78aab3a87da4f61ec)](https://www.codacy.com/app/ilya40umov/KotLink?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ilya40umov/KotLink&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/ilya40umov/KotLink/branch/master/graph/badge.svg)](https://codecov.io/gh/ilya40umov/KotLink)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

KotLink is a service for creating and sharing memorable URL aliases.

The service will consist of a backend that will perform link resolution 
and UI that will allow users to add/edit/delete aliases/namespaces.
Then, there will be plugins available at least for Chrome and FireFox 
that will resolve aliases entered by the user in Omnibox after first typing `kt`+whitespace.

### Project Status
Project is still in a very early stage and COMPLETELY UNUSABLE. :)

### TODOs
* Detekt plugin (static code analysis)
* CRUD endpoint for aliases
* vivaldi instructions
* chrome plugin
* firefox plugin
* frontend with material
* security (oath)

### Engineering Guide

#### Recommended Software
* jdk 8+
* direnv
* docker
* docker-compose
* Intellij IDEA

#### Some useful commands
* Start dependencies `kt_env_up`
* Build application (also runs tests) `./gradlew build`
* Run application in Terminal `./gradlew bootRun`
* Stop dependencies `kt_env_down`