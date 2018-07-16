# KotLink - Browser Extension

### Chrome / Chromium

Supported via [Browser Extension](https://chrome.google.com/webstore/detail/kotlink-browser-extension/cdkflkfieefihicjaidafmggjdnkakod)

### Firefox

Supported via [Browser Extension](https://addons.mozilla.org/en-US/firefox/addon/kotlink-browser-extension)

### Vivaldi

To use KotLink with Vivaldi perform the following steps:
1. Go to Settings / Privacy and enable "Search Suggestions in Address Field".
1. Go to Settings / Search and make sure "Allow Search Suggestions" is checked for "In Address Field".
1. Also in Settings / Search, add a new search engine where:
  - *Name* is `KotLink` 
  - *Nickname* is `go`
  - *URL* is `http://YOUR_SERVER_ADDRESS/api/link/redirect?link=%s`
  - *Suggest URL* is `http://YOUR_SERVER_ADDRESS/api/link/suggest?link=%s&mode=opensearch&secret=YOUR_SECRET`

### Extension Options

Both browser extensions as well as Vivaldi require some configuration before they can be used.

![Extension Options](https://raw.githubusercontent.com/ilya40umov/KotLink/master/images/extension-options.png)

Namely, there are two options that need to be set:

* *KotLink Server URL* should be pointing to your KotLink server (e.g. `http://localhost:8080` 
in case if you are running KotLink server locally)
* *Extension Secret* should be set to your personal extension secret,
which can be found in UI under *Extension Secret* menu item.