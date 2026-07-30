package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.AdminBookingSearchQuery;
import com.tripgoapi.application.port.in.GetAdminBookingsUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.UpdateBookingStatusUseCase;
import com.tripgoapi.domain.exception.ConflictException;
import com.tripgoapi.domain.model.Booking;
import com.tripgoapi.domain.model.BookingStatus;
import com.tripgoapi.infrastructure.adapter.in.admin.view.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private static final int PAGE_SIZE = 10;

    private final GetAdminBookingsUseCase getAdminBookingsUseCase;
    private final UpdateBookingStatusUseCase updateBookingStatusUseCase;

    @ModelAttribute("activeMenu")
    public String activeMenu() {
        return "bookings";
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        BookingStatus filter = parseStatus(status);
        PageResult<Booking> result =
                getAdminBookingsUseCase.searchBookings(new AdminBookingSearchQuery(filter, page, PAGE_SIZE));

        model.addAttribute("bookings", result.data());
        model.addAttribute("pageInfo", PageInfo.of(result));
        model.addAttribute("statusFilter", filter);
        return "admin/bookings/list";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String returnStatus,
            @RequestParam(defaultValue = "1") int page,
            RedirectAttributes redirectAttributes
    ) {
        BookingStatus target = parseStatus(status);
        if (target == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ: " + status);
        } else {
            try {
                Booking updated = updateBookingStatusUseCase.updateStatus(id, target);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đơn " + updated.bookingCode() + " đã chuyển sang trạng thái " + updated.status());
            } catch (ConflictException ex) {
                // Two admins acting on the same booking (or an outright invalid transition) is a
                // routine occurrence, not a system fault: keep them on the list with an explanation
                // instead of throwing them to the full-page error screen.
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            }
        }

        // Send the admin back to the tab and page they were on, not to the top of the list.
        // returnStatus is re-parsed through the same enum guard as the filter itself — never
        // echo the raw request parameter into the redirect url.
        BookingStatus returnFilter = parseStatus(returnStatus);
        return returnFilter == null
                ? "redirect:/admin/bookings?page=" + page
                : "redirect:/admin/bookings?page=" + page + "&status=" + returnFilter.name();
    }

    /** @return {@code null} for a blank/unknown value, which the list treats as "Tất cả" */
    private BookingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return BookingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
