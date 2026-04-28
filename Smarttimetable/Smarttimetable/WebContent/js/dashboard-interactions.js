(function () {
    function animateStatValues() {
        var statValues = document.querySelectorAll('.js-stat-value[data-target]');

        Array.prototype.forEach.call(statValues, function (element) {
            var target = parseInt(element.getAttribute('data-target'), 10);
            if (isNaN(target)) {
                return;
            }

            var duration = 700;
            var frameRate = 30;
            var steps = Math.max(1, Math.floor(duration / frameRate));
            var increment = target / steps;
            var current = 0;
            var stepCount = 0;

            var intervalId = window.setInterval(function () {
                stepCount += 1;
                current += increment;

                if (stepCount >= steps) {
                    element.textContent = String(target);
                    window.clearInterval(intervalId);
                } else {
                    element.textContent = String(Math.round(current));
                }
            }, frameRate);
        });
    }

    function setupCommandCenter() {
        var summaryCards = document.querySelectorAll('.summary-card-action');
        var metricChips = document.querySelectorAll('.command-metric-chip');
        var viewButtons = document.querySelectorAll('.dashboard-view-btn');
        var quickFilterButtons = document.querySelectorAll('.quick-filter-btn[data-action-filter]');
        var quickActionLinks = document.querySelectorAll('.quick-action-link[data-action-group]');

        var focusTitle = document.getElementById('metricFocusTitle');
        var focusValue = document.getElementById('metricFocusValue');
        var focusText = document.getElementById('metricFocusText');
        var missionList = document.getElementById('dashboardMissionList');
        var attentionValue = document.getElementById('attentionValue');
        var attentionFill = document.getElementById('attentionFill');
        var quickActionVisibleCount = document.getElementById('quickActionVisibleCount');

        if (!summaryCards.length) {
            return;
        }

        var metricData = {};
        Array.prototype.forEach.call(summaryCards, function (card) {
            var key = card.getAttribute('data-metric') || '';
            if (!key || metricData[key]) {
                return;
            }

            var value = parseInt(card.getAttribute('data-value'), 10);
            metricData[key] = {
                label: card.getAttribute('data-label') || 'Metric',
                value: isNaN(value) ? 0 : value,
                description: card.getAttribute('data-description') || ''
            };
        });

        var metricToActionFilter = {
            total: 'manage',
            today: 'manage',
            subjects: 'review',
            users: 'review'
        };

        var actionNotes = {
            actionAddEntry: 'Add or refine timetable slots where coverage is missing.',
            actionViewEntries: 'Review your full timetable before applying major changes.',
            actionSearch: 'Use quick search and filters to detect conflicts fast.',
            actionGrid: 'Inspect week distribution in the matrix view.',
            actionAi: 'Auto-generate a draft timetable and then fine-tune entries.'
        };

        function setQuickActionFilter(filter) {
            var visible = 0;

            Array.prototype.forEach.call(quickFilterButtons, function (button) {
                if (button.getAttribute('data-action-filter') === filter) {
                    button.classList.add('active');
                } else {
                    button.classList.remove('active');
                }
            });

            Array.prototype.forEach.call(quickActionLinks, function (link) {
                var groups = (link.getAttribute('data-action-group') || '').split(/\s+/);
                var show = filter === 'all' || groups.indexOf(filter) !== -1;
                link.style.display = show ? '' : 'none';

                if (show) {
                    visible += 1;
                }
            });

            if (quickActionVisibleCount) {
                quickActionVisibleCount.textContent = String(visible);
            }
        }

        function pushUnique(list, actionId) {
            if (list.indexOf(actionId) === -1) {
                list.push(actionId);
            }
        }

        function getActionPlan(view, metricKey) {
            var plan = [];
            var primaryByMetric = {
                total: 'actionViewEntries',
                today: 'actionAddEntry',
                subjects: 'actionSearch',
                users: 'actionViewEntries'
            };

            if (view === 'planning') {
                pushUnique(plan, 'actionAddEntry');
                pushUnique(plan, 'actionGrid');
                pushUnique(plan, 'actionViewEntries');
                return plan;
            }

            if (view === 'automation') {
                pushUnique(plan, 'actionAi');
                pushUnique(plan, 'actionViewEntries');
                pushUnique(plan, primaryByMetric[metricKey] || 'actionSearch');
                return plan;
            }

            pushUnique(plan, primaryByMetric[metricKey] || 'actionViewEntries');
            pushUnique(plan, 'actionViewEntries');
            pushUnique(plan, 'actionAi');
            return plan;
        }

        function renderMissionList(view, metricKey) {
            if (!missionList) {
                return;
            }

            var plan = getActionPlan(view, metricKey);
            missionList.innerHTML = '';

            Array.prototype.forEach.call(plan, function (actionId) {
                var target = document.getElementById(actionId);
                if (!target) {
                    return;
                }

                var li = document.createElement('li');
                li.className = 'mission-item';

                var link = document.createElement('a');
                link.className = 'mission-link';
                link.href = target.getAttribute('href') || '#';
                link.textContent = target.textContent.trim();

                var note = document.createElement('p');
                note.className = 'mission-note mb-0';
                note.textContent = actionNotes[actionId] || 'Use this action to improve your timetable workflow.';

                li.appendChild(link);
                li.appendChild(note);
                missionList.appendChild(li);
            });
        }

        function updateAttentionMeter(view, metricKey) {
            var metric = metricData[metricKey] || {
                value: 0
            };
            var viewBoost = {
                overview: 20,
                planning: 34,
                automation: 45
            };
            var maxValue = 1;

            Object.keys(metricData).forEach(function (key) {
                if (metricData[key].value > maxValue) {
                    maxValue = metricData[key].value;
                }
            });

            var ratio = Math.max(0, metric.value) / maxValue;
            var attention = Math.min(100, Math.round(ratio * 55 + (viewBoost[view] || 20)));

            if (attentionValue) {
                attentionValue.textContent = String(attention) + '%';
            }

            if (attentionFill) {
                attentionFill.style.width = String(attention) + '%';
                attentionFill.classList.remove('is-low', 'is-mid', 'is-high');

                if (attention >= 70) {
                    attentionFill.classList.add('is-high');
                } else if (attention >= 45) {
                    attentionFill.classList.add('is-mid');
                } else {
                    attentionFill.classList.add('is-low');
                }
            }
        }

        var currentMetric = (document.querySelector('.summary-card-action.is-active') || summaryCards[0])
            .getAttribute('data-metric') || 'total';
        var currentView = (document.querySelector('.dashboard-view-btn.active') || {
            getAttribute: function () {
                return 'overview';
            }
        }).getAttribute('data-dashboard-view');

        function setMetric(metricKey) {
            if (!metricData[metricKey]) {
                return;
            }

            currentMetric = metricKey;

            Array.prototype.forEach.call(summaryCards, function (card) {
                if (card.getAttribute('data-metric') === metricKey) {
                    card.classList.add('is-active');
                } else {
                    card.classList.remove('is-active');
                }
            });

            Array.prototype.forEach.call(metricChips, function (chip) {
                if (chip.getAttribute('data-metric') === metricKey) {
                    chip.classList.add('active');
                } else {
                    chip.classList.remove('active');
                }
            });

            if (focusTitle) {
                focusTitle.textContent = metricData[metricKey].label;
            }

            if (focusValue) {
                focusValue.textContent = String(metricData[metricKey].value);
            }

            if (focusText) {
                focusText.textContent = metricData[metricKey].description;
            }

            if (currentView === 'overview') {
                setQuickActionFilter(metricToActionFilter[metricKey] || 'all');
            }

            renderMissionList(currentView, currentMetric);
            updateAttentionMeter(currentView, currentMetric);
        }

        function setView(viewKey) {
            currentView = viewKey;

            Array.prototype.forEach.call(viewButtons, function (button) {
                if (button.getAttribute('data-dashboard-view') === viewKey) {
                    button.classList.add('active');
                } else {
                    button.classList.remove('active');
                }
            });

            if (viewKey === 'planning') {
                setQuickActionFilter('manage');
            } else if (viewKey === 'automation') {
                setQuickActionFilter('ai');
            } else {
                setQuickActionFilter(metricToActionFilter[currentMetric] || 'all');
            }

            renderMissionList(currentView, currentMetric);
            updateAttentionMeter(currentView, currentMetric);
        }

        Array.prototype.forEach.call(summaryCards, function (card) {
            card.addEventListener('click', function () {
                setMetric(card.getAttribute('data-metric') || 'total');
            });
        });

        Array.prototype.forEach.call(metricChips, function (chip) {
            chip.addEventListener('click', function () {
                setMetric(chip.getAttribute('data-metric') || 'total');
            });
        });

        Array.prototype.forEach.call(viewButtons, function (button) {
            button.addEventListener('click', function () {
                setView(button.getAttribute('data-dashboard-view') || 'overview');
            });
        });

        Array.prototype.forEach.call(quickFilterButtons, function (button) {
            button.addEventListener('click', function () {
                setQuickActionFilter(button.getAttribute('data-action-filter') || 'all');
            });
        });

        setMetric(currentMetric);
        setView(currentView);
    }

    function setupInfoTabs() {
        var tabButtons = document.querySelectorAll('.info-tab-btn[data-info-target]');
        var panels = document.querySelectorAll('.info-panel[data-info-panel]');

        if (!tabButtons.length || !panels.length) {
            return;
        }

        function activateTab(target) {
            Array.prototype.forEach.call(tabButtons, function (button) {
                if (button.getAttribute('data-info-target') === target) {
                    button.classList.add('active');
                } else {
                    button.classList.remove('active');
                }
            });

            Array.prototype.forEach.call(panels, function (panel) {
                if (panel.getAttribute('data-info-panel') === target) {
                    panel.classList.remove('d-none');
                } else {
                    panel.classList.add('d-none');
                }
            });
        }

        Array.prototype.forEach.call(tabButtons, function (button) {
            button.addEventListener('click', function () {
                activateTab(button.getAttribute('data-info-target') || 'overview');
            });
        });

        var initial = document.querySelector('.info-tab-btn.active');
        activateTab(initial ? initial.getAttribute('data-info-target') : 'overview');
    }

    function init() {
        animateStatValues();
        setupCommandCenter();
        setupInfoTabs();
    }

    document.addEventListener('DOMContentLoaded', init);
})();
