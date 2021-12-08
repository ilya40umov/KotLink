/* global kotlink, mdc */

if (typeof (kotlink) === "undefined") {
    window.kotlink = {};
}

/**
 * Adds the given event handler to the queue. It will be executed asynchronously after all external JS and CSS resources
 * have finished loading (as determined by continuous long-polling with a timeout). If this function is called after all
 * resources have finished loading, the given handler function will be invoked synchronously (in the same call stack).
 * Handlers are invoked in FIFO order.
 *
 * XXX Borrowed from https://material-components-web.appspot.com/ready.js
 */
kotlink.onReady = (() => {
    const POLL_INTERVAL_MS = 100;
    const POLL_MAX_WAIT_MS = 60 * 1000;

    let isReadyCached = false;
    let isDomLoaded = false;
    let isMdcInitialized = false;
    let handlers = [];
    let testDom = null;
    let startTimeMs = null;
    let pollTimer = null;

    function ensureDetectionDom() {
        if (testDom) {
            return;
        }
        testDom = document.createElement("div");
        testDom.classList.add("kotlink-ready-detect");
        document.body.appendChild(testDom);
    }

    function isReady() {
        if (isReadyCached) {
            return true;
        }
        if (document.body == null) {
            return false;
        }
        ensureDetectionDom();
        const isDomWithCss = getComputedStyle(testDom).position === "relative";
        isReadyCached = isDomWithCss && isDomLoaded && isMdcInitialized;
        return isReadyCached;
    }

    function removeDetectionDom() {
        if (!testDom) {
            return;
        }
        document.body.removeChild(testDom);
        testDom = null;
    }

    function invokeHandlers() {
        handlers.forEach(function (handler) {
            handler();
        });
        handlers.length = 0;
    }

    function tick() {
        if (isReady()) {
            clearInterval(pollTimer);
            removeDetectionDom();
            invokeHandlers();
            return;
        }

        const elapsedTimeMs = Date.now() - startTimeMs;
        if (elapsedTimeMs > POLL_MAX_WAIT_MS) {
            clearInterval(pollTimer);
            removeDetectionDom();
        }
    }

    function startTimer() {
        if (pollTimer) {
            return;
        }
        startTimeMs = Date.now();
        pollTimer = setInterval(tick, POLL_INTERVAL_MS);
        window.addEventListener("load", function () {
            tick();
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        isDomLoaded = true;
        document.addEventListener("MDCAutoInit:End", function () {
            isMdcInitialized = true;
        });
        mdc.autoInit();
    });

    return function addHandler(handler) {
        if (isReady()) {
            handler();
            return;
        }
        handlers.push(handler);
        startTimer();
    };
})();

kotlink.onReady(() => {
    // enable the user menu (right-top corner)
    const userMenu = document.getElementById("user-menu").MDCMenu;
    userMenu.setAnchorCorner(mdc.menu.Corner.BOTTOM_START);
    const menuIconEl = document.querySelector("#user-menu-icon");
    menuIconEl.addEventListener("click", function () {
        this.blur();
        userMenu.open = !userMenu.open;
    });
});