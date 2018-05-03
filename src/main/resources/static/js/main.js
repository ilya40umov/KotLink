window.ktReady(function () {
    if (!Boolean(window.mdc)) {
        console.error("mdc is still not available!");
        return;
    }
    mdc.autoInit();
});