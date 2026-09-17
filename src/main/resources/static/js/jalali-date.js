(() => {
    'use strict';

    const MONTH_NAMES = [
        'فروردین', 'اردیبهشت', 'خرداد',
        'تیر', 'مرداد', 'شهریور',
        'مهر', 'آبان', 'آذر',
        'دی', 'بهمن', 'اسفند'
    ];

    const SKIP_TAGS = new Set([
        'SCRIPT', 'STYLE', 'TEXTAREA', 'CODE', 'PRE', 'NOSCRIPT'
    ]);

    function normalizeDigits(value) {
        if (value == null) return '';

        const persian = '۰۱۲۳۴۵۶۷۸۹';
        const arabic = '٠١٢٣٤٥٦٧٨٩';

        return String(value)
            .replace(/[۰-۹]/g, char => String(persian.indexOf(char)))
            .replace(/[٠-٩]/g, char => String(arabic.indexOf(char)));
    }

    function pad(value) {
        return String(value).padStart(2, '0');
    }

    function isGregorianLeapYear(year) {
        return (year % 4 === 0 && year % 100 !== 0)
            || year % 400 === 0;
    }

    function gregorianToJalali(year, month, day) {
        const gregorianDays = [
            0, 31, 59, 90, 120, 151,
            181, 212, 243, 273, 304, 334
        ];

        let jalaliYear;

        if (year > 1600) {
            jalaliYear = 979;
            year -= 1600;
        } else {
            jalaliYear = 0;
            year -= 621;
        }

        const adjustedYear = month > 2 ? year + 1 : year;

        let days = (365 * year)
            + Math.floor((adjustedYear + 3) / 4)
            - Math.floor((adjustedYear + 99) / 100)
            + Math.floor((adjustedYear + 399) / 400)
            - 80
            + day
            + gregorianDays[month - 1];

        jalaliYear += 33 * Math.floor(days / 12053);
        days %= 12053;

        jalaliYear += 4 * Math.floor(days / 1461);
        days %= 1461;

        if (days > 365) {
            jalaliYear += Math.floor((days - 1) / 365);
            days = (days - 1) % 365;
        }

        let jalaliMonth;
        let jalaliDay;

        if (days < 186) {
            jalaliMonth = 1 + Math.floor(days / 31);
            jalaliDay = 1 + (days % 31);
        } else {
            jalaliMonth = 7 + Math.floor((days - 186) / 30);
            jalaliDay = 1 + ((days - 186) % 30);
        }

        return [jalaliYear, jalaliMonth, jalaliDay];
    }

    function jalaliToGregorian(year, month, day) {
        year += 1595;

        let days = -355668
            + (365 * year)
            + (Math.floor(year / 33) * 8)
            + Math.floor(((year % 33) + 3) / 4)
            + day;

        if (month < 7) {
            days += (month - 1) * 31;
        } else {
            days += ((month - 7) * 30) + 186;
        }

        let gregorianYear = 400 * Math.floor(days / 146097);
        days %= 146097;

        if (days > 36524) {
            days -= 1;
            gregorianYear += 100 * Math.floor(days / 36524);
            days %= 36524;

            if (days >= 365) {
                days += 1;
            }
        }

        gregorianYear += 4 * Math.floor(days / 1461);
        days %= 1461;

        if (days > 365) {
            gregorianYear += Math.floor((days - 1) / 365);
            days = (days - 1) % 365;
        }

        let gregorianDay = days + 1;

        const monthDays = [
            0,
            31,
            isGregorianLeapYear(gregorianYear) ? 29 : 28,
            31, 30, 31, 30,
            31, 31, 30, 31, 30, 31
        ];

        let gregorianMonth = 1;

        while (gregorianMonth <= 12
            && gregorianDay > monthDays[gregorianMonth]) {
            gregorianDay -= monthDays[gregorianMonth];
            gregorianMonth += 1;
        }

        return [gregorianYear, gregorianMonth, gregorianDay];
    }

    function formatJalali(parts) {
        return `${parts[0]}/${pad(parts[1])}/${pad(parts[2])}`;
    }

    function gregorianIsoToJalali(value) {
        const normalized = normalizeDigits(value).trim();
        const match = normalized.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
        if (!match) return '';

        const year = Number(match[1]);
        const month = Number(match[2]);
        const day = Number(match[3]);

        if (year < 1600 || month < 1 || month > 12 || day < 1 || day > 31) {
            return '';
        }

        return formatJalali(gregorianToJalali(year, month, day));
    }

    function parseJalali(value) {
        const normalized = normalizeDigits(value)
            .trim()
            .replace(/-/g, '/');

        const match = normalized.match(/^(\d{3,4})\/(\d{1,2})\/(\d{1,2})$/);
        if (!match) return null;

        const year = Number(match[1]);
        const month = Number(match[2]);
        const day = Number(match[3]);

        if (year < 1200 || year > 1700 || month < 1 || month > 12 || day < 1 || day > 31) {
            return null;
        }

        if (month >= 7 && day > 30) return null;

        const gregorian = jalaliToGregorian(year, month, day);
        const roundTrip = gregorianToJalali(
            gregorian[0],
            gregorian[1],
            gregorian[2]
        );

        if (roundTrip[0] !== year
            || roundTrip[1] !== month
            || roundTrip[2] !== day) {
            return null;
        }

        return {
            jalali: [year, month, day],
            gregorian,
            iso: `${gregorian[0]}-${pad(gregorian[1])}-${pad(gregorian[2])}`
        };
    }

    function currentJalaliDate() {
        const now = new Date();
        return gregorianToJalali(
            now.getFullYear(),
            now.getMonth() + 1,
            now.getDate()
        );
    }

    function replaceGregorianDatesInText(text) {
        if (!text || text.length > 20000) return text;

        let result = text.replace(
            /\b((?:16|17|18|19|20|21)\d{2})-(\d{2})-(\d{2})\b/g,
            (full, year, month, day) => {
                const converted = gregorianIsoToJalali(
                    `${year}-${month}-${day}`
                );
                return converted || full;
            }
        );

        result = result.replace(
            /\b((?:16|17|18|19|20|21)\d{2})\/(\d{1,2})\/(\d{1,2})\b/g,
            (full, year, month, day) => {
                const converted = gregorianIsoToJalali(
                    `${year}-${pad(month)}-${pad(day)}`
                );
                return converted || full;
            }
        );

        return result;
    }

    function convertTextNodes(root = document.body) {
        if (!root) return;

        const walker = document.createTreeWalker(
            root,
            NodeFilter.SHOW_TEXT,
            {
                acceptNode(node) {
                    const parent = node.parentElement;
                    if (!parent || SKIP_TAGS.has(parent.tagName)) {
                        return NodeFilter.FILTER_REJECT;
                    }

                    if (parent.closest('.jalali-picker-popup')) {
                        return NodeFilter.FILTER_REJECT;
                    }

                    if (!/(?:16|17|18|19|20|21)\d{2}[-/]\d{1,2}[-/]\d{1,2}/.test(node.nodeValue || '')) {
                        return NodeFilter.FILTER_REJECT;
                    }

                    return NodeFilter.FILTER_ACCEPT;
                }
            }
        );

        const nodes = [];
        let node;
        while ((node = walker.nextNode())) {
            nodes.push(node);
        }

        nodes.forEach(textNode => {
            const converted = replaceGregorianDatesInText(textNode.nodeValue);
            if (converted !== textNode.nodeValue) {
                textNode.nodeValue = converted;
            }
        });
    }

    function daysInJalaliMonth(year, month) {
        if (month <= 6) return 31;
        if (month <= 11) return 30;

        const valid30 = parseJalali(`${year}/12/30`);
        return valid30 ? 30 : 29;
    }

    function createSelect(className, ariaLabel) {
        const select = document.createElement('select');
        select.className = className;
        select.setAttribute('aria-label', ariaLabel);
        return select;
    }

    function addOption(select, value, label) {
        const option = document.createElement('option');
        option.value = String(value);
        option.textContent = label;
        select.appendChild(option);
    }

    function setupDateInput(original) {
        if (!original || original.dataset.jalaliReady === 'true') return;
        if (original.type !== 'date' && original.type !== 'datetime-local') return;

        original.dataset.jalaliReady = 'true';

        const isDateTime = original.type === 'datetime-local';
        const wasRequired = original.required;
        original.required = false;

        const wrapper = document.createElement('div');
        wrapper.className = 'jalali-input-wrapper';

        const visible = document.createElement('input');
        visible.type = 'text';
        visible.className = 'jalali-date-input';
        visible.autocomplete = 'off';
        visible.inputMode = 'numeric';
        visible.required = wasRequired;
        visible.placeholder = isDateTime
            ? '1405/05/19 08:30'
            : '1405/05/19';
        visible.setAttribute(
            'aria-label',
            original.getAttribute('aria-label')
                || (isDateTime ? 'تاریخ و ساعت شمسی' : 'تاریخ شمسی')
        );

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'jalali-calendar-button';
        button.setAttribute('aria-label', 'باز کردن تقویم شمسی');
        button.textContent = '📅';

        const popup = document.createElement('div');
        popup.className = 'jalali-picker-popup';
        popup.hidden = true;

        const header = document.createElement('div');
        header.className = 'jalali-picker-title';
        header.textContent = isDateTime
            ? 'انتخاب تاریخ و ساعت شمسی'
            : 'انتخاب تاریخ شمسی';

        const fields = document.createElement('div');
        fields.className = 'jalali-picker-fields';

        const yearSelect = createSelect('jalali-year-select', 'سال شمسی');
        const monthSelect = createSelect('jalali-month-select', 'ماه شمسی');
        const daySelect = createSelect('jalali-day-select', 'روز شمسی');

        const today = currentJalaliDate();
        for (let year = today[0] - 15; year <= today[0] + 10; year += 1) {
            addOption(yearSelect, year, year);
        }

        MONTH_NAMES.forEach((name, index) => {
            addOption(monthSelect, index + 1, name);
        });

        fields.append(yearSelect, monthSelect, daySelect);

        let timeInput = null;
        if (isDateTime) {
            timeInput = document.createElement('input');
            timeInput.type = 'time';
            timeInput.className = 'jalali-time-input';
            timeInput.step = '60';
            fields.appendChild(timeInput);
        }

        const actions = document.createElement('div');
        actions.className = 'jalali-picker-actions';

        const todayButton = document.createElement('button');
        todayButton.type = 'button';
        todayButton.className = 'jalali-picker-secondary';
        todayButton.textContent = 'امروز';

        const cancelButton = document.createElement('button');
        cancelButton.type = 'button';
        cancelButton.className = 'jalali-picker-secondary';
        cancelButton.textContent = 'بستن';

        const confirmButton = document.createElement('button');
        confirmButton.type = 'button';
        confirmButton.className = 'jalali-picker-primary';
        confirmButton.textContent = 'تأیید';

        actions.append(todayButton, cancelButton, confirmButton);
        popup.append(header, fields, actions);

        original.parentNode.insertBefore(wrapper, original);
        wrapper.append(visible, button, popup, original);
        original.classList.add('jalali-original-input');

        function updateDays(preferredDay) {
            const year = Number(yearSelect.value || today[0]);
            const month = Number(monthSelect.value || today[1]);
            const maxDay = daysInJalaliMonth(year, month);
            const oldDay = preferredDay || Number(daySelect.value || 1);

            daySelect.innerHTML = '';
            for (let day = 1; day <= maxDay; day += 1) {
                addOption(daySelect, day, day);
            }

            daySelect.value = String(Math.min(oldDay, maxDay));
        }

        function setPickerFromJalali(jalali, timeValue) {
            yearSelect.value = String(jalali[0]);
            monthSelect.value = String(jalali[1]);
            updateDays(jalali[2]);
            daySelect.value = String(jalali[2]);

            if (timeInput) {
                timeInput.value = timeValue || '08:00';
            }
        }

        function syncVisibleFromOriginal() {
            if (!original.value) {
                visible.value = '';
                setPickerFromJalali(today, isDateTime ? '08:00' : null);
                return;
            }

            if (isDateTime) {
                const [datePart, timePart = '00:00'] = original.value.split('T');
                const jalaliText = gregorianIsoToJalali(datePart);
                visible.value = jalaliText
                    ? `${jalaliText} ${timePart.slice(0, 5)}`
                    : '';

                const parsed = jalaliText ? parseJalali(jalaliText) : null;
                setPickerFromJalali(
                    parsed ? parsed.jalali : today,
                    timePart.slice(0, 5)
                );
            } else {
                const jalaliText = gregorianIsoToJalali(original.value);
                visible.value = jalaliText;
                const parsed = jalaliText ? parseJalali(jalaliText) : null;
                setPickerFromJalali(parsed ? parsed.jalali : today);
            }
        }

        function syncOriginalFromVisible(showError = false) {
            const normalized = normalizeDigits(visible.value).trim();

            if (!normalized) {
                original.value = '';
                visible.setCustomValidity(
                    wasRequired ? 'تاریخ را وارد کنید.' : ''
                );
                return !wasRequired;
            }

            if (isDateTime) {
                const match = normalized.match(/^(\d{3,4}[/-]\d{1,2}[/-]\d{1,2})\s+(\d{1,2}:\d{2})$/);
                if (!match) {
                    visible.setCustomValidity(
                        'فرمت تاریخ و ساعت باید مانند 1405/05/19 08:30 باشد.'
                    );
                    if (showError) visible.reportValidity();
                    return false;
                }

                const parsed = parseJalali(match[1]);
                if (!parsed || !/^([01]?\d|2[0-3]):[0-5]\d$/.test(match[2])) {
                    visible.setCustomValidity('تاریخ یا ساعت شمسی معتبر نیست.');
                    if (showError) visible.reportValidity();
                    return false;
                }

                const time = match[2].padStart(5, '0');
                original.value = `${parsed.iso}T${time}`;
                visible.value = `${formatJalali(parsed.jalali)} ${time}`;
                visible.setCustomValidity('');
                setPickerFromJalali(parsed.jalali, time);
            } else {
                const parsed = parseJalali(normalized);
                if (!parsed) {
                    visible.setCustomValidity(
                        'تاریخ شمسی معتبر نیست. نمونه: 1405/05/19'
                    );
                    if (showError) visible.reportValidity();
                    return false;
                }

                original.value = parsed.iso;
                visible.value = formatJalali(parsed.jalali);
                visible.setCustomValidity('');
                setPickerFromJalali(parsed.jalali);
            }

            original.dispatchEvent(new Event('input', { bubbles: true }));
            original.dispatchEvent(new Event('change', { bubbles: true }));
            return true;
        }

        function openPopup() {
            syncOriginalFromVisible(false);
            popup.hidden = false;
            wrapper.classList.add('jalali-picker-open');
        }

        function closePopup() {
            popup.hidden = true;
            wrapper.classList.remove('jalali-picker-open');
        }

        yearSelect.addEventListener('change', () => updateDays());
        monthSelect.addEventListener('change', () => updateDays());

        button.addEventListener('click', event => {
            event.stopPropagation();
            if (popup.hidden) openPopup(); else closePopup();
        });

        visible.addEventListener('focus', () => {
            visible.setCustomValidity('');
        });

        visible.addEventListener('blur', () => {
            syncOriginalFromVisible(false);
        });

        todayButton.addEventListener('click', () => {
            const now = new Date();
            const jalali = gregorianToJalali(
                now.getFullYear(),
                now.getMonth() + 1,
                now.getDate()
            );
            setPickerFromJalali(
                jalali,
                isDateTime
                    ? `${pad(now.getHours())}:${pad(now.getMinutes())}`
                    : null
            );
        });

        cancelButton.addEventListener('click', closePopup);

        confirmButton.addEventListener('click', () => {
            const jalaliText = `${yearSelect.value}/${pad(monthSelect.value)}/${pad(daySelect.value)}`;

            if (isDateTime) {
                visible.value = `${jalaliText} ${timeInput.value || '00:00'}`;
            } else {
                visible.value = jalaliText;
            }

            if (syncOriginalFromVisible(true)) {
                closePopup();
            }
        });

        document.addEventListener('click', event => {
            if (!wrapper.contains(event.target)) {
                closePopup();
            }
        });

        const form = original.closest('form');
        if (form && form.dataset.jalaliSubmitReady !== 'true') {
            form.dataset.jalaliSubmitReady = 'true';
            form.addEventListener('submit', event => {
                let valid = true;
                form.querySelectorAll('.jalali-date-input').forEach(input => {
                    const relatedOriginal = input.parentElement
                        ?.querySelector('.jalali-original-input');
                    if (!relatedOriginal) return;

                    const wrapperApi = relatedOriginal._jalaliApi;
                    if (wrapperApi && !wrapperApi.sync(true)) {
                        valid = false;
                    }
                });

                if (!valid) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            });
        }

        original._jalaliApi = {
            sync: syncOriginalFromVisible,
            refresh: syncVisibleFromOriginal
        };

        syncVisibleFromOriginal();
    }

    function setupDateInputs(root = document) {
        if (!root || !root.querySelectorAll) return;
        root.querySelectorAll('input[type="date"], input[type="datetime-local"]')
            .forEach(setupDateInput);
    }

    function initialize() {
        setupDateInputs(document);
        convertTextNodes(document.body);

        const observer = new MutationObserver(mutations => {
            mutations.forEach(mutation => {
                mutation.addedNodes.forEach(node => {
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        setupDateInputs(node);
                        convertTextNodes(node);
                    } else if (node.nodeType === Node.TEXT_NODE) {
                        const converted = replaceGregorianDatesInText(node.nodeValue);
                        if (converted !== node.nodeValue) {
                            node.nodeValue = converted;
                        }
                    }
                });
            });
        });

        if (document.body) {
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        }
    }

    window.JalaliDate = {
        gregorianToJalali,
        jalaliToGregorian,
        gregorianIsoToJalali,
        parseJalali,
        formatJalali,
        currentJalaliDate,
        refresh() {
            setupDateInputs(document);
            convertTextNodes(document.body);
        }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }
})();
