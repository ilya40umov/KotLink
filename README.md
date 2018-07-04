# KotLink
![Project Logo](https://raw.githubusercontent.com/ilya40umov/KotLink/master/browser-extension/icons/icon-128.png)
[![Build Status](https://travis-ci.org/ilya40umov/KotLink.png?branch=master)](https://travis-ci.org/ilya40umov/KotLink)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1a55315857b44bb78aab3a87da4f61ec)](https://www.codacy.com/app/ilya40umov/KotLink?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ilya40umov/KotLink&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/ilya40umov/KotLink/branch/master/graph/badge.svg)](https://codecov.io/gh/ilya40umov/KotLink)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

KotLink is a solution for creating and sharing memorable URL aliases, 
which takes its inspiration from Google's internal Go-Links system.

It works by letting people install a tiny browser extension 
(which is not even necessary in case of Vivaldi browser)
that activates when the person first types `go` in the status bar and then presses *Whitespace*. 
While activated, the extension is providing autocomplete based on the database of aliases,
and after the user has hit *Enter*, it will redirect the user to the actual URL that matches the provided alias.
For example, if someone has already created an alias for `staging newrelic` 
that maps to `https://rpm.newrelic.com/accounts/YYY/applications/ZZZ`,
by typing `go` *Whitespace* `staging newrelic` *Enter*, the user will be redirected to the aforementioned link.

### Deploy KotLink Server

KotLink server is the backend for storing and resolving aliases, 
as well as the frontend for creating/editing them.

*The installation instructions are coming soon*

### Get KotLink Working In Your Browser

[FireFox Extension](https://addons.mozilla.org/en-US/firefox/addon/kotlink-browser-extension)

*Chrome Extension is coming soon*

*Vivaldi set up instructions are coming soon*

### Engineering Guide

#### Recommended Software
* jdk 8+
* direnv
* docker
* docker-compose
* Intellij IDEA (recommended)

#### How To Develop Locally
* Start dependencies with `kt_env_up`
* Run application in Terminal with `./gradlew bootRun` (or in Intellij IDEA)
* Go to `http://localhost:8080` in your browser and create your namespaces / links
* Stop dependencies with `kt_env_down` (this command will remove all the data from the database)
* Run the CI pipeline with `kt_env_ci` (can be run in parallel with application)

### TODOs
* implement generating per-user secrets for `/api/link/suggest` and `/api/link/redirect`
* improve DB-related error handling
* document instructions on how to set up Vivaldi
* add validation logic to namespace/alias service (e.g. default namespace should always be present)
* develop chrome plugin
* develop firefox plugin
* implement caching for the `/api/link/suggest` endpoint

* (Tech Debt) write tests for service layer
* (Tech Debt) address Codacy issues

* add notion of owners for namespaces / links to restrict who can modify / delete what
* implement REST API to allow development of alternative UIs