(function () {
    function validateTimeRange(form) {
        var startInput = form.querySelector('input[name="startTime"]');
        var endInput = form.querySelector('input[name="endTime"]');

        if (!startInput || !endInput) {
            return true;
        }

        var start = startInput.value;
        var end = endInput.value;

        if (!start || !end) {
            endInput.setCustomValidity("");
            return true;
        }

        if (start >= end) {
            endInput.setCustomValidity("End time must be after start time.");
            return false;
        }

        endInput.setCustomValidity("");
        return true;
    }

    function validateMatchFields(form) {
        var matchFields = form.querySelectorAll('[data-match-target]');
        var isValid = true;

        Array.prototype.forEach.call(matchFields, function (field) {
            var targetSelector = field.getAttribute('data-match-target');
            var message = field.getAttribute('data-match-message') || 'Values do not match.';
            var targetField = targetSelector ? form.querySelector(targetSelector) : null;

            if (!targetField || !field.value) {
                field.setCustomValidity('');
                return;
            }

            if (field.value !== targetField.value) {
                field.setCustomValidity(message);
                isValid = false;
                return;
            }

            field.setCustomValidity('');
        });

        return isValid;
    }

    function bindValidation() {
        var forms = document.querySelectorAll('.needs-validation-custom');

        Array.prototype.forEach.call(forms, function (form) {
            form.addEventListener('submit', function (event) {
                var timeValid = validateTimeRange(form);
                var matchValid = validateMatchFields(form);

                if (!form.checkValidity() || !timeValid || !matchValid) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            });

            var startInput = form.querySelector('input[name="startTime"]');
            var endInput = form.querySelector('input[name="endTime"]');

            if (startInput && endInput) {
                startInput.addEventListener('change', function () {
                    validateTimeRange(form);
                });
                endInput.addEventListener('change', function () {
                    validateTimeRange(form);
                });
            }

            var matchFields = form.querySelectorAll('[data-match-target]');
            Array.prototype.forEach.call(matchFields, function (field) {
                var targetSelector = field.getAttribute('data-match-target');
                var targetField = targetSelector ? form.querySelector(targetSelector) : null;

                field.addEventListener('input', function () {
                    validateMatchFields(form);
                });

                if (targetField) {
                    targetField.addEventListener('input', function () {
                        validateMatchFields(form);
                    });
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', bindValidation);
})();
