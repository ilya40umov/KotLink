# KotLink
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://github.com/ilya40umov/KotLink/actions/workflows/ci.yml/badge.svg)](https://github.com/ilya40umov/KotLink/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/ilya40umov/KotLink/branch/master/graph/badge.svg)](https://codecov.io/gh/ilya40umov/KotLink)
[![Docker Hub](https://img.shields.io/docker/pulls/ilya40umov/kotlink.svg)](https://hub.docker.com/r/ilya40umov/kotlink/)

KotLink is a solution for creating and sharing memorable URL aliases, 
which takes its inspiration from Google's internal Go-Links system.

### Why use KotLink?

* Get rid of bookmarks for URLs frequently used within your team
* Speak the same language as your colleges (e.g. Matt, can you check out what is up with *staging␣grafana*?)
* Search in your "intranet" quickly for a resource that you don't know the exact URL for

### Overview

KotLink works by letting people install a tiny browser extension 
that activates when the person first types *kk* in the address bar and then presses *space*. 
While activated, the extension is providing autocomplete based on the database of aliases,
and after the user has hit *enter*, 
it will redirect the user to the actual URL that matches the provided alias 
(or to the search page, if such an alias does not exist).

![Suggestions In Address Bar](https://raw.githubusercontent.com/ilya40umov/KotLink/master/docs/images/suggest.png)

For example, if someone has already created an alias for `vim shortcuts` 
that maps to `https://vim.rtorr.com`,
by typing *kk␣vim␣shortcuts↵*, the user will be redirected to the actual link.

Please beware that to make use of the browser extension, 
you will first need to set up a dedicated KotLink server,
as it's going to store all of the links / namespaces for your team.

### Supported Browsers

* Chrome / Chromium via [Browser Extension](https://chrome.google.com/webstore/detail/kotlink-browser-extension/cdkflkfieefihicjaidafmggjdnkakod)
* Firefox via [Browser Extension](https://addons.mozilla.org/en-US/firefox/addon/kotlink-browser-extension)

Chrome and Firefox extensions require some
[configuration](http://kotlink.org/docs/extension-guide.html#kotlink-extension-options)
before they can be used.

### Other Known Ways Of Using KotLink

With Vivaldi via [manually registering KotLink as a search engine](docs/extension-guide.md#vivaldi)

MacOS and [Alfred](https://www.alfredapp.com) users can install [this workflow](https://github.com/augustocdias/alfred-kotlink) (requires [Node.js](https://nodejs.org) 8+ and the Alfred [Powerpack](https://www.alfredapp.com/powerpack/)):

    npm install --global alfred-kotlink

### KotLink Server

KotLink server requires an instance of PostgreSQL as the backend store, 
and encapsulates all the logic around storing / resolving URL aliases, 
as well as UI for creating / editing them.

For evaluation purposes, you can run KotLink server on your machine with the following commands, 
given that you have [Docker](https://en.wikipedia.org/wiki/Docker_(software)) installed
(you may have to add `sudo` before all calls to `docker` depending on your system): 
```
docker network create kotlink-network

docker run --name kotlink-postgres \
  --network kotlink-network \
  -e POSTGRES_USER=kotlinkuser \
  -e POSTGRES_PASSWORD=kotlinkpass \
  -e POSTGRES_DB=kotlink \
  -d postgres:13.3

docker pull ilya40umov/kotlink

docker run --rm --name kotlink-server \
  --network kotlink-network -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://kotlink-postgres:5432/kotlink \
  ilya40umov/kotlink
```

Now if you open `http://localhost:8080/` you will be redirected to KotLink UI, 
which, after you have signed in with your Google account, will allow you to add namespaces and aliases.

![List Aliases in UI](https://raw.githubusercontent.com/ilya40umov/KotLink/master/docs/images/list-aliases.png)

At this point, you can finally install the Kotlink browser extension, 
open its *Options* (*Preferences* in Firefox), and configure it to access your local KotLink server:
* set *KotLink Server URL* to `http://localhost:8080` 
* set *Extension Secret* to your personal extension secret, 
which you will find at `http://localhost:8080/ui/extension_secret`).

![Extension Options](https://raw.githubusercontent.com/ilya40umov/KotLink/master/docs/images/extension-options.png)

Please, note that to allow accessing KotLink UI under your custom domain name / IP address,
you will need to obtain OAuth 2.0 client credentials from [Google API Console](https://console.developers.google.com)
and provide them to your KotLink server via environment variables 
(see more on this in [Deployment Guide](docs/deployment-guide.md)). 
Through environment variables, you will also be able to restrict who can access your KotLink server.

When you are done evaluating, you can run the following command to clean up containers from your machine:

```docker rm -f kotlink-postgres && docker network rm kotlink-network```

For the detailed instructions on how to permanently set up your own KotLink server, 
take a look at the [Deployment Guide](docs/deployment-guide.md).

### Engineering Guide
If you would like to contribute to the project, take a look at the [Engineering Guide](docs/engineering-guide.md).
