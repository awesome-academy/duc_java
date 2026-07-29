package com.tripgoapi.infrastructure.adapter.in.admin;

import com.tripgoapi.application.port.in.CreateDestinationUseCase;
import com.tripgoapi.application.port.in.DeleteDestinationUseCase;
import com.tripgoapi.application.port.in.GetAdminDestinationsUseCase;
import com.tripgoapi.application.port.in.SaveDestinationCommand;
import com.tripgoapi.application.port.in.UpdateDestinationUseCase;
import com.tripgoapi.domain.exception.UnprocessableException;
import com.tripgoapi.infrastructure.adapter.in.admin.form.DestinationForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/destinations")
@RequiredArgsConstructor
public class AdminDestinationController {

    private static final String FORM_VIEW = "admin/destinations/form";

    private final GetAdminDestinationsUseCase getAdminDestinationsUseCase;
    private final CreateDestinationUseCase createDestinationUseCase;
    private final UpdateDestinationUseCase updateDestinationUseCase;
    private final DeleteDestinationUseCase deleteDestinationUseCase;

    @ModelAttribute("activeMenu")
    public String activeMenu() {
        return "destinations";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("destinations", getAdminDestinationsUseCase.getDestinations());
        return "admin/destinations/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("destinationForm", new DestinationForm());
        model.addAttribute("editing", false);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("destinationForm") DestinationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return save(null, form, bindingResult, model, redirectAttributes);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("destinationForm",
                DestinationForm.from(getAdminDestinationsUseCase.getDestination(id)));
        model.addAttribute("editing", true);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("destinationForm") DestinationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return save(id, form, bindingResult, model, redirectAttributes);
    }

    /**
     * Create and update differ only in which use case runs and the wording of the success
     * message — kept in one place so the two paths cannot silently diverge.
     */
    private String save(
            Long id,
            DestinationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        boolean editing = id != null;
        form.setId(id);

        if (!bindingResult.hasErrors()) {
            try {
                if (editing) {
                    updateDestinationUseCase.updateDestination(id, toCommand(form));
                } else {
                    createDestinationUseCase.createDestination(toCommand(form));
                }
                redirectAttributes.addFlashAttribute("successMessage",
                        editing ? "Đã cập nhật điểm đến" : "Đã thêm điểm đến mới");
                return "redirect:/admin/destinations";
            } catch (UnprocessableException ex) {
                bindingResult.reject("destination.save.failed", ex.getMessage());
            }
        }

        model.addAttribute("editing", editing);
        return FORM_VIEW;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        deleteDestinationUseCase.deleteDestination(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa điểm đến");
        return "redirect:/admin/destinations";
    }

    private SaveDestinationCommand toCommand(DestinationForm form) {
        return new SaveDestinationCommand(
                form.getName(),
                form.getDescription(),
                form.getKeptImageUrl(),
                MultipartFiles.toUploadedImage(form.getNewImage())
        );
    }
}
