/* global browser, chrome */

function loadExtensionStorage(handleResult) {
    if (typeof browser !== "undefined") {
        browser.storage.local.get(["kotlinkServerUrl", "kotlinkExtensionSecret"])
            .then(handleResult);
    } else {
        chrome.storage.sync.get({
            kotlinkServerUrl: '',
            kotlinkExtensionSecret: ''
        }, handleResult)
    }
}

function saveExtensionStorage(extensionStorage, handleResult) {
    if (typeof browser !== "undefined") {
        browser.storage.local.set(extensionStorage).then(handleResult);
    } else {
        chrome.storage.sync.set(extensionStorage, handleResult)
    }
}

function saveOptions(e) {
    e.preventDefault();
    saveExtensionStorage({
        kotlinkServerUrl: document.querySelector("#server-url").value,
        kotlinkExtensionSecret: document.querySelector("#extension-secret").value
    }, () => {
        document.querySelector("#message").innerHTML = "Preferences have been saved.";
        setTimeout(() => {
            document.querySelector("#message").innerHTML = "";
        }, 750);
    });
}

function restoreOptions() {
    loadExtensionStorage((result) => {
        document.querySelector("#server-url").value =
            result.kotlinkServerUrl || "";
        document.querySelector("#extension-secret").value =
            result.kotlinkExtensionSecret || "";
    });
}

document.addEventListener("DOMContentLoaded", restoreOptions);
document.querySelector("form").addEventListener("submit", saveOptions);