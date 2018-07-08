kotlink.onReady(() => {
    // enable the user menu (right-top corner)
    const userMenu = document.getElementById("user-menu").MDCMenu;
    userMenu.setAnchorCorner(mdc.menu.MDCMenuFoundation.Corner.BOTTOM_START);
    const menuButtonEl = document.querySelector('#user-menu-button');
    menuButtonEl.addEventListener('click', function () {
        this.blur();
        userMenu.open = !userMenu.open;
    });
});