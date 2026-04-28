(function () {
    function animateCounters() {
        var counters = document.querySelectorAll('.preview-stat-value[data-preview-target]');

        Array.prototype.forEach.call(counters, function (counter) {
            var target = parseInt(counter.getAttribute('data-preview-target'), 10);
            if (isNaN(target)) {
                return;
            }

            var duration = 600;
            var frameRate = 30;
            var steps = Math.max(1, Math.floor(duration / frameRate));
            var increment = target / steps;
            var current = 0;
            var stepCount = 0;

            var intervalId = window.setInterval(function () {
                stepCount += 1;
                current += increment;

                if (stepCount >= steps) {
                    counter.textContent = String(target);
                    window.clearInterval(intervalId);
                } else {
                    counter.textContent = String(Math.round(current));
                }
            }, frameRate);
        });
    }

    function updateFocus(card) {
        var label = card.getAttribute('data-label') || 'Metric';
        var value = card.getAttribute('data-value') || '0';
        var description = card.getAttribute('data-description') || '';
        var focusText = document.getElementById('previewFocusText');

        if (focusText) {
            focusText.textContent = label + ': ' + value + '. ' + description;
        }
    }

    function bindMetricCards() {
        var cards = document.querySelectorAll('.preview-stat-card');

        if (!cards.length) {
            return;
        }

        Array.prototype.forEach.call(cards, function (card) {
            card.addEventListener('click', function () {
                Array.prototype.forEach.call(cards, function (item) {
                    item.classList.remove('active');
                });

                card.classList.add('active');
                updateFocus(card);
            });
        });
    }

    function bindScopeButtons() {
        var buttons = document.querySelectorAll('.preview-scope-btn');
        var activityTag = document.getElementById('previewActivityTag');
        var listItems = document.querySelectorAll('#previewActivityList li[data-scope]');

        if (!buttons.length || !listItems.length) {
            return;
        }

        function applyScope(scope, label) {
            var visible = 0;

            Array.prototype.forEach.call(listItems, function (item) {
                var scopes = item.getAttribute('data-scope') || '';
                var shouldShow = scopes.indexOf(scope) !== -1;
                item.style.display = shouldShow ? '' : 'none';
                if (shouldShow) {
                    visible += 1;
                }
            });

            if (!visible) {
                Array.prototype.forEach.call(listItems, function (item) {
                    item.style.display = '';
                });
            }

            if (activityTag) {
                activityTag.textContent = label;
            }
        }

        Array.prototype.forEach.call(buttons, function (button) {
            button.addEventListener('click', function () {
                Array.prototype.forEach.call(buttons, function (item) {
                    item.classList.remove('active');
                });

                button.classList.add('active');
                applyScope(button.getAttribute('data-preview-scope') || 'today', button.textContent || 'Today');
            });
        });

        var activeButton = document.querySelector('.preview-scope-btn.active');
        if (activeButton) {
            applyScope(activeButton.getAttribute('data-preview-scope') || 'today', activeButton.textContent || 'Today');
        }
    }

    function init() {
        animateCounters();
        bindMetricCards();
        bindScopeButtons();
    }

    document.addEventListener('DOMContentLoaded', init);
})();
