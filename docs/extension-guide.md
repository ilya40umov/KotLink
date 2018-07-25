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

Please, make sure to replace `YOUR_SERVER_ADDRESS` and `YOUR_SECRET` in the aforementioned URLs 
with your actual data.

### Safari

Safari is currently only partially supported via 3rd-party 
[Safari Omnikey](http://marioestrada.github.io/safari-omnikey/) extension.

In order to make it work, first install the extension, then click on the extension icon (looks like a magnifying glass), 
click *Add Site* and use prefix `go` with the following URL `http://YOUR_SERVER_ADDRESS/api/link/redirect?link={search}`

Please, make sure to replace `YOUR_SERVER_ADDRESS` in the URL with your actual server address. 
Also, it's worth to mention that unlike regular KotLink extension for Chrome / FireFox, 
*Safari Omnikey* does not support autocomplete.

### KotLink Extension Options

KotLink extension (for both Chrome and FireFox) requires some configuration before it can be used.

![Extension Options](https://raw.githubusercontent.com/ilya40umov/KotLink/master/docs/images/extension-options.png)

Namely, there are two options that need to be set:

* *KotLink Server URL* should be pointing to your KotLink server (e.g. `http://localhost:8080` 
in case if you are running KotLink server locally)
* *Extension Secret* should be set to your personal extension secret, which can be found in UI under *Extension Secret* menu item.
