function loadExtensionStorage(handleResult) {
    if (typeof browser !== "undefined") {
        browser.storage.local.get(["kotlinkServerUrl", "kotlinkExtensionSecret"])
            .then(handleResult);
    } else {
        chrome.storage.sync.get({
            kotlinkServerUrl: "",
            kotlinkExtensionSecret: ""
        }, handleResult)
    }
}

let omnibox = (typeof browser !== "undefined") ? browser.omnibox : chrome.omnibox;
let tabs = (typeof browser !== "undefined") ? browser.tabs : chrome.tabs;

function getServerUrl(extensionStorage) {
    let url = extensionStorage.kotlinkServerUrl;
    if (!url.endsWith("/")) {
        url += "/";
    }
    return url;
}

function createSuggestionsFromResponse(response) {
    return new Promise((resolve) => {
        response.json().then(jsonArray => {
            if (!jsonArray) {
                return resolve([]);
            }
            let suggestions = [];
            for (let i in jsonArray) {
                if (jsonArray.hasOwnProperty(i)) {
                    let suggestion = `${jsonArray[i]} `;
                    suggestions.push({content: suggestion, description: suggestion});
                }
            }
            return resolve(suggestions);
        });
    });
}

omnibox.onInputChanged.addListener((userInput, addSuggestions) => {
    if (userInput && userInput.length !== 0 && userInput.trim()) {
        loadExtensionStorage((extensionStorage) => {
            if (extensionStorage.kotlinkServerUrl && extensionStorage.kotlinkExtensionSecret) {
                let headers = new Headers({
                    "Accept": "application/json",
                    "Authorization": `Bearer ${extensionStorage.kotlinkExtensionSecret}`
                });
                let init = {method: 'GET', headers};
                let url = getServerUrl(extensionStorage) + "api/link/suggest?mode=simple&link=" + userInput;
                let request = new Request(url, init);
                fetch(request)
                    .then(createSuggestionsFromResponse)
                    .then(addSuggestions);
            }
        });
    }
});

omnibox.onInputEntered.addListener(function (userInput, disposition) {
    loadExtensionStorage((extensionStorage) => {
        if (extensionStorage.kotlinkServerUrl) {
            let url = `${getServerUrl(extensionStorage)}api/link/redirect?link=${userInput}`;
            switch (disposition) {
                case "currentTab":
                    tabs.update({url});
                    break;
                case "newForegroundTab":
                    tabs.create({url});
                    break;
                case "newBackgroundTab":
                    tabs.create({url, active: false});
                    break;
            }
        }
    });
});