/* TripGo Admin — progressive enhancement only. Every action here has a server-side
   counterpart, so the portal still works with JavaScript disabled. */
(function () {
    'use strict';

    /* Confirmation for destructive forms (Xóa tour, Hủy đơn, ...). Declarative via
       data-confirm so templates never need inline event handlers. */
    document.addEventListener('submit', function (event) {
        var message = event.target.getAttribute('data-confirm');
        if (message && !window.confirm(message)) {
            event.preventDefault();
        }
    });

    /* ---- Itinerary editor ---- */

    var rows = document.getElementById('itinerary-rows');
    var template = document.getElementById('itinerary-template');
    var addButton = document.getElementById('add-itinerary');

    /* Spring's indexed binding stops at the first missing index, so the rows must always be
       numbered 0..n-1 with no gaps — renumber after every add and remove. */
    function renumber() {
        Array.prototype.forEach.call(rows.children, function (row, index) {
            row.querySelector('.itinerary-day-number').textContent = String(index + 1);
            Array.prototype.forEach.call(row.querySelectorAll('input[name^="itinerary["]'), function (input) {
                input.name = input.name.replace(/itinerary\[\d+\]/, 'itinerary[' + index + ']');
            });
        });
    }

    if (rows && template && addButton) {
        addButton.addEventListener('click', function () {
            var markup = template.innerHTML.replace(/__INDEX__/g, String(rows.children.length));
            var holder = document.createElement('div');
            holder.innerHTML = markup.trim();
            rows.appendChild(holder.firstChild);
            renumber();
        });

        rows.addEventListener('click', function (event) {
            if (event.target.hasAttribute('data-remove-itinerary')) {
                event.target.closest('.itinerary-row').remove();
                renumber();
            }
        });
    }

    /* ---- Image tiles ----
       Removing a tile drops its hidden input, so the url simply isn't submitted; the server
       treats "absent from keptImageUrls" as "the admin removed this image". */
    document.addEventListener('click', function (event) {
        if (event.target.hasAttribute('data-remove-image')) {
            event.target.closest('.image-item').remove();
        }
    });

    /* ---- Upload button feedback ---- */

    Array.prototype.forEach.call(document.querySelectorAll('.upload-box input[type="file"]'), function (input) {
        input.addEventListener('change', function () {
            var hint = input.closest('.upload-box').querySelector('.upload-hint');
            if (!hint) {
                return;
            }
            if (input.files.length === 1) {
                hint.textContent = input.files[0].name;
            } else if (input.files.length > 1) {
                hint.textContent = 'Đã chọn ' + input.files.length + ' ảnh';
            }
        });
    });
})();
