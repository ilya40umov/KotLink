document.addEventListener("DOMContentLoaded", function (event) {
    var tfs = document.querySelectorAll(
        '.mdc-text-field:not([data-demo-no-auto-js])'
    );
    for (var i = 0, tf; tf = tfs[i]; i++) {
        mdc.textField.MDCTextField.attachTo(tf);
    }
});