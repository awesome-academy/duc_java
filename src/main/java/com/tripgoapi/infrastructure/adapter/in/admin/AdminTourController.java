package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.AdminTourSearchQuery;
import com.tripgoapi.application.port.in.CreateTourUseCase;
import com.tripgoapi.application.port.in.DeleteTourUseCase;
import com.tripgoapi.application.port.in.GetAdminDestinationsUseCase;
import com.tripgoapi.application.port.in.GetAdminTourDetailUseCase;
import com.tripgoapi.application.port.in.GetAdminToursUseCase;
import com.tripgoapi.application.port.in.GetCategoriesUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.SaveTourCommand;
import com.tripgoapi.application.port.in.UpdateTourUseCase;
import com.tripgoapi.domain.exception.UnprocessableException;
import com.tripgoapi.domain.model.AdminTourSummary;
import com.tripgoapi.domain.model.TourStatus;
import com.tripgoapi.infrastructure.adapter.in.admin.form.TourForm;
import com.tripgoapi.infrastructure.adapter.in.admin.view.PageInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tours")
@RequiredArgsConstructor
public class AdminTourController {

    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW = "admin/tours/form";

    private final GetAdminToursUseCase getAdminToursUseCase;
    private final GetAdminTourDetailUseCase getAdminTourDetailUseCase;
    private final CreateTourUseCase createTourUseCase;
    private final UpdateTourUseCase updateTourUseCase;
    private final DeleteTourUseCase deleteTourUseCase;
    private final GetAdminDestinationsUseCase getAdminDestinationsUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;

    @ModelAttribute("activeMenu")
    public String activeMenu() {
        return "tours";
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        PageResult<AdminTourSummary> result =
                getAdminToursUseCase.searchTours(new AdminTourSearchQuery(q, page, PAGE_SIZE));

        model.addAttribute("tours", result.data());
        model.addAttribute("pageInfo", PageInfo.of(result));
        model.addAttribute("q", q);
        return "admin/tours/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tourForm", new TourForm());
        return prepareForm(model, false);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("tourForm") TourForm tourForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return save(null, tourForm, bindingResult, model, redirectAttributes);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("tourForm", TourForm.from(getAdminTourDetailUseCase.getTourForEdit(id)));
        return prepareForm(model, true);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("tourForm") TourForm tourForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return save(id, tourForm, bindingResult, model, redirectAttributes);
    }

    /**
     * Create and update differ only in which use case runs and the wording of the success
     * message; every other step — cross-field validation, error handling, which view to
     * re-render — has to stay identical, so it lives here once instead of twice.
     */
    private String save(
            Long id,
            TourForm tourForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        boolean editing = id != null;
        tourForm.setId(id);
        validatePricing(tourForm, bindingResult);

        if (!bindingResult.hasErrors()) {
            try {
                if (editing) {
                    updateTourUseCase.updateTour(id, toCommand(tourForm));
                } else {
                    createTourUseCase.createTour(toCommand(tourForm));
                }
                redirectAttributes.addFlashAttribute("successMessage",
                        editing ? "Đã cập nhật tour thành công" : "Đã thêm tour mới thành công");
                return "redirect:/admin/tours";
            } catch (UnprocessableException ex) {
                // Business rules the annotations can't express (image type/size, cross-field
                // pricing) are raised by the application layer — surface them on the form, not
                // as an error page.
                bindingResult.reject("tour.save.failed", ex.getMessage());
            }
        }

        return prepareForm(model, editing);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        deleteTourUseCase.deleteTour(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tour");
        return "redirect:/admin/tours";
    }

    private SaveTourCommand toCommand(TourForm form) {
        return new SaveTourCommand(
                form.getTitle(),
                form.getDestinationId(),
                form.getCategoryId(),
                form.getPrice(),
                form.getDiscountPrice(),
                form.getDurationDays(),
                form.getMaxGuests(),
                form.getDescription(),
                form.isFeatured(),
                form.getStatus(),
                form.toItineraryDays(),
                form.getKeptImageUrls(),
                MultipartFiles.toUploadedImages(form.getNewImages()),
                form.getThumbnailUrl()
        );
    }

    /**
     * Cross-field rule, so it cannot live on a single-field annotation. Reported against
     * discountPrice to put the message next to the input the admin has to fix.
     */
    private void validatePricing(TourForm form, BindingResult bindingResult) {
        if (form.getPrice() != null
                && form.getDiscountPrice() != null
                && form.getDiscountPrice().compareTo(form.getPrice()) >= 0) {
            bindingResult.rejectValue("discountPrice", "tour.discountPrice.invalid",
                    "Giá khuyến mãi phải nhỏ hơn giá gốc");
        }
    }

    private String prepareForm(Model model, boolean editing) {
        model.addAttribute("destinations", getAdminDestinationsUseCase.getDestinations());
        model.addAttribute("categories", getCategoriesUseCase.getCategories());
        model.addAttribute("statuses", TourStatus.editableValues());
        model.addAttribute("editing", editing);
        return FORM_VIEW;
    }
}
