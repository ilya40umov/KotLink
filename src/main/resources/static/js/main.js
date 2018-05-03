window.ktReady(function () {
    if (!mdc) {
        console.error("mdc is still not available!");
        return;
    }
    mdc.autoInit();
});