# KotLink
[![Build Status](https://travis-ci.org/ilya40umov/KotLink.png?branch=master)](https://travis-ci.org/ilya40umov/KotLink)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1a55315857b44bb78aab3a87da4f61ec)](https://www.codacy.com/app/ilya40umov/KotLink?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ilya40umov/KotLink&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/ilya40umov/KotLink/branch/master/graph/badge.svg)](https://codecov.io/gh/ilya40umov/KotLink)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

KotLink is a solution for creating and sharing memorable URL aliases, 
which takes its inspiration from Google's internal Go-Links system.

It works by letting people install a tiny browser extension 
(which is not even necessary in case of Vivaldi browser)
that activates when the person first types `go` in the status bar and then presses whitespace. 
While activated, the extension is providing autocomplete based on the database of aliases,
and after the user has hit *Enter*, it will redirect the user to an actual URL that matches the provided alias.
For example, if someone has already created an alias for `staging newrelic` 
that maps to `https://rpm.newrelic.com/accounts/YYY/applications/ZZZ`,
by typing `go` *Whitespace* `staging newrelic` *Enter*, the user will be redirected to the aforementioned link.

### Project Scope

* Backend for storing and resolving aliases (In-Progress)
* Frontend for managing aliases (In-Progress)
* Chrome Plugin (Not started yet)
* FireFox Plugin (Not started yet)

### TODOs
* Security (oath for all endpoints / secrets for autocomplete)
* add validation logic to namespace service (e.g. default namespace should always be present)
* write tests for service layer
* implement caching of aliases
* vivaldi instructions
* chrome plugin
* firefox plugin

### Deployment Instructions
TBA

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
* Stop dependencies with `kt_env_down`
* Run the CI pipeline with `kt_env_ci`