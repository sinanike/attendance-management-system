(function () {
    const storageKey = 'sutech-color-theme';
    const root = document.documentElement;

    function preferredTheme() {
        const saved = localStorage.getItem(storageKey);

        if (saved === 'dark' || saved === 'light') {
            return saved;
        }

        return window.matchMedia(
            '(prefers-color-scheme: dark)'
        ).matches ? 'dark' : 'light';
    }

    function apply(theme) {
        root.dataset.theme = theme;

        const button = document.querySelector(
            '.theme-toggle-button'
        );

        if (button) {
            const dark = theme === 'dark';

            button.setAttribute(
                'aria-label',
                dark
                    ? 'فعال‌کردن حالت روشن'
                    : 'فعال‌کردن حالت تاریک'
            );

            button.setAttribute(
                'title',
                dark
                    ? 'حالت روشن'
                    : 'حالت تاریک'
            );

            button.innerHTML = dark
                ? '<span aria-hidden="true">☀</span>'
                : '<span aria-hidden="true">☾</span>';
        }
    }

    apply(preferredTheme());

    document.addEventListener(
        'DOMContentLoaded',
        function () {
            const button =
                document.createElement('button');

            button.type = 'button';
            button.className =
                'theme-toggle-button';

            document.body.appendChild(button);

            apply(
                root.dataset.theme
                || preferredTheme()
            );

            button.addEventListener(
                'click',
                function () {
                    const next =
                        root.dataset.theme === 'dark'
                            ? 'light'
                            : 'dark';

                    localStorage.setItem(
                        storageKey,
                        next
                    );

                    apply(next);
                }
            );
        }
    );
})();