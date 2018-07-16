# KotLink
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/ilya40umov/KotLink.png?branch=master)](https://travis-ci.org/ilya40umov/KotLink)
[![codecov](https://codecov.io/gh/ilya40umov/KotLink/branch/master/graph/badge.svg)](https://codecov.io/gh/ilya40umov/KotLink)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1a55315857b44bb78aab3a87da4f61ec)](https://www.codacy.com/app/ilya40umov/KotLink?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ilya40umov/KotLink&amp;utm_campaign=Badge_Grade)
[![Docker Hub](https://img.shields.io/docker/pulls/ilya40umov/kotlink.svg)](https://hub.docker.com/r/ilya40umov/kotlink/)

KotLink is a solution for creating and sharing memorable URL aliases, 
which takes its inspiration from Google's internal Go-Links system.

### Why use KotLink?

* Get rid of bookmarks for URLs frequently used within your team
* Speak the same language as your colleges (e.g. Hey, can you check out what is happening with *staging␣grafana*?)
* Search your "intranet" quickly for a resource that you don't know the URL for

### Overview

KotLink works by letting people install a tiny browser extension 
that activates when the person first types *go* in the address bar and then presses *space*. 
While activated, the extension is providing autocomplete based on the database of aliases,
and after the user has hit *enter*, 
it will redirect the user to the actual URL that matches the provided alias 
(or to the search page, if such an alias does not exist).

![Suggestions In Address Bar](https://raw.githubusercontent.com/ilya40umov/KotLink/master/images/suggest.png)

For example, if someone has already created an alias for `vim shortcuts` 
that maps to `https://vim.rtorr.com`,
by typing *go␣vim␣shortcuts↵*, the user will be redirected to the actual link.

Please beware that to make use of the browser extension, 
you will first need to set up a dedicated KotLink server,
as it's going to store all of the links / namespaces for your team.

### Supported Browsers

* Chrome / Chromium via [Browser Extension](https://chrome.google.com/webstore/detail/kotlink-browser-extension/cdkflkfieefihicjaidafmggjdnkakod)
* Firefox via [Browser Extension](https://addons.mozilla.org/en-US/firefox/addon/kotlink-browser-extension)
* Vivaldi via [manually registering KotLink as a search engine](extension-guide.md#vivaldi)

### KotLink Server

KotLink server requires PostgreSQL and contains all the logic around storing / resolving URL aliases, 
as well as UI for creating / editing them.

For evaluation purposes, you can run KotLink server on your machine with the following commands 
(you may have to add `sudo` before all calls to `docker` depending on your system): 
```
docker network create kotlink-network

docker run --name kotlink-postgres \
  --network kotlink-network \
  -e POSTGRES_USER=kotlinkuser \
  -e POSTGRES_PASSWORD=kotlinkpass \
  -e POSTGRES_DB=kotlink \
  -d postgres:10.4

docker run --rm --name kotlink-server \
  --network kotlink-network -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://kotlink-postgres:5432/kotlink \
  ilya40umov/kotlink
```

Now if you open `http://localhost:8080/` you will be redirected to KotLink UI, 
which, after you have signed in with your Google account, will allow you to add namespaces and aliases.

![List Aliases in UI](https://raw.githubusercontent.com/ilya40umov/KotLink/master/images/list-aliases.png)

At this point, you can finally install the browser extension, 
open its *Options* (*Preferences* in Firefox), and configure it to access your local KotLink server:
* set *KotLink Server URL* to `http://localhost:8080` 
* set *Extension Secret* to your personal extension secret, 
which can be found at `http://localhost:8080/ui/extension_secret`).

![Extension Options](https://raw.githubusercontent.com/ilya40umov/KotLink/master/images/extension-options.png)

Please, note that to allow accessing KotLink UI under your custom domain name / IP address,
you will need to obtain OAuth 2.0 client credentials from [Google API Console](https://console.developers.google.com)
and provide them to your KotLink server via environment variables 
(see more on this in [Deployment Guide](deployment-guide.md)). 
Through environment variables, you will also be able to restrict who can access your KotLink server.

When you are done evaluating, you can run the following command to clean up containers from your machine:

```docker rm -f kotlink-postgres && docker network rm kotlink-network```

For the detailed instructions on how to permanently set up your own KotLink server, 
take a look at the [Deployment Guide](deployment-guide.md).

### Engineering Guide
If you would like to contribute to the project, take a look at the [Engineering Guide](engineering-guide.md).