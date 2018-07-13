/* global kotlink, mdc */
/*
 * Borrowed from https://material-components-web.appspot.com/ready.js
 */
/**
 * Adds the given event handler to the queue. It will be executed asynchronously after all external JS and CSS resources
 * have finished loading (as determined by continuous long-polling with a timeout). If this function is called after all
 * resources have finished loading, the given handler function will be invoked synchronously (in the same call stack).
 * Handlers are invoked in FIFO order.
 */
if (typeof(kotlink) === "undefined") {
    window.kotlink = {};
}

kotlink.onReady = (() => {
    const POLL_INTERVAL_MS = 100;
    const POLL_MAX_WAIT_MS = 60 * 1000;

    let isReadyCached = false;
    let isDomLoaded = false;
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
        const isMdcJsLoaded = Boolean(window.mdc) ||
            (isDomLoaded && !document.querySelector("script[src$='material-components-web.js']"));
        isReadyCached = isDomWithCss && isMdcJsLoaded;
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

// make sure auto init is called because anything else
kotlink.onReady(() => {
    mdc.autoInit();
});