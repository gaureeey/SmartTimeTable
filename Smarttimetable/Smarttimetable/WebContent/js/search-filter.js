document.addEventListener('DOMContentLoaded', function () {
    var searchInput = document.getElementById('searchInput');
    var dayFilter = document.getElementById('dayFilter');
    var semesterFilter = document.getElementById('semesterFilter');
    var rows = document.querySelectorAll('#entriesTableBody tr[data-entry-id]');
    var visibleCount = document.getElementById('visibleCount');

    function updateVisibleCount(count) {
        if (visibleCount) {
            visibleCount.textContent = String(count);
        }
    }

    function applyClientFilter() {
        if (!rows.length) {
            updateVisibleCount(0);
            return;
        }

        var searchValue = searchInput ? searchInput.value.toLowerCase().trim() : '';
        var dayValue = dayFilter ? dayFilter.value.trim() : '';
        var semesterValue = semesterFilter ? semesterFilter.value.trim() : '';
        var count = 0;

        Array.prototype.forEach.call(rows, function (row) {
            var searchable = row.getAttribute('data-search') || '';
            var rowDay = row.getAttribute('data-day') || '';
            var rowSemester = row.getAttribute('data-semester') || '';

            var matchSearch = !searchValue || searchable.indexOf(searchValue) !== -1;
            var matchDay = !dayValue || dayValue === rowDay;
            var matchSemester = !semesterValue || semesterValue === rowSemester;

            if (matchSearch && matchDay && matchSemester) {
                row.style.display = '';
                count += 1;
            } else {
                row.style.display = 'none';
            }
        });

        updateVisibleCount(count);
    }

    if (searchInput) {
        searchInput.addEventListener('input', applyClientFilter);
    }

    if (dayFilter) {
        dayFilter.addEventListener('change', applyClientFilter);
    }

    if (semesterFilter) {
        semesterFilter.addEventListener('change', applyClientFilter);
    }

    var deleteButtons = document.querySelectorAll('.btn-delete-entry');
    var deleteEntryInput = document.getElementById('deleteEntryId');
    var deleteEntryLabel = document.getElementById('deleteEntryLabel');

    Array.prototype.forEach.call(deleteButtons, function (button) {
        button.addEventListener('click', function () {
            var entryId = button.getAttribute('data-entry-id');
            var subject = button.getAttribute('data-subject') || 'this entry';

            if (deleteEntryInput) {
                deleteEntryInput.value = entryId;
            }

            if (deleteEntryLabel) {
                deleteEntryLabel.textContent = 'Delete ' + subject + ' (ID ' + entryId + ')?';
            }
        });
    });

    applyClientFilter();
});
