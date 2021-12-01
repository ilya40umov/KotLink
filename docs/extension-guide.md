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
  - *Nickname* is `kk`
  - *URL* is `http://YOUR_SERVER_ADDRESS/api/link/redirect?link=%s`
  - *Suggest URL* is `http://YOUR_SERVER_ADDRESS/api/link/suggest?link=%s&mode=opensearch&secret=YOUR_SECRET`

Please, make sure to replace `YOUR_SERVER_ADDRESS` and `YOUR_SECRET` in the aforementioned URLs 
with your actual data.

### Safari

It used to be possible to get partial support via [Safari Omnikey](http://marioestrada.github.io/safari-omnikey/),
but since Safari 13 it does not seem to function anymore.

### KotLink Extension Options

KotLink extension (for both Chrome and FireFox) requires some configuration before it can be used.
Namely, there are two options that need to be set:

* *KotLink Server URL* should be pointing to your KotLink server (e.g. `http://localhost:8080` 
in case if you are running KotLink server locally)
* *Extension Secret* should be set to your personal extension secret, which can be found in UI under *Extension Secret* menu item.

![Extension Options](https://raw.githubusercontent.com/ilya40umov/KotLink/master/docs/images/extension-options.png)

To access the extension options (as shown above):
* In Chrome, click on the icon of the newly installed extension and select *Options* from the menu.
* In Firefox, open `about:addons` and click on *Preferences* button that is displayed next to "KotLink Browser Extension".