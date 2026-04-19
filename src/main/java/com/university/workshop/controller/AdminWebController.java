package com.university.workshop.controller;

import com.university.workshop.dto.WorkshopRequest;
import com.university.workshop.entity.Workshop;
import com.university.workshop.service.RegistrationService;
import com.university.workshop.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final WorkshopService workshopService;
    private final RegistrationService registrationService;

    @GetMapping("/workshops")
    public String manageWorkshops(Model model) {
        model.addAttribute("workshops", workshopService.getAllWorkshops());
        return "admin/workshops";
    }

    @GetMapping("/workshops/new")
    public String newWorkshopForm(Model model) {
        model.addAttribute("workshop", new WorkshopRequest());
        model.addAttribute("isEdit", false);
        return "admin/workshop-form";
    }

    @PostMapping("/workshops/new")
    public String createWorkshop(@Valid @ModelAttribute("workshop") WorkshopRequest request,
                                  BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/workshop-form";
        }
        workshopService.createWorkshop(request);
        flash.addFlashAttribute("success", "Workshop created successfully!");
        return "redirect:/admin/workshops";
    }

    @GetMapping("/workshops/{id}/edit")
    public String editWorkshopForm(@PathVariable Long id, Model model) {
        Workshop w = workshopService.getWorkshopById(id);
        WorkshopRequest request = new WorkshopRequest();
        request.setTitle(w.getTitle());
        request.setDescription(w.getDescription());
        request.setLocation(w.getLocation());
        request.setStartDatetime(w.getStartDatetime());
        request.setTotalSeats(w.getTotalSeats());
        model.addAttribute("workshop", request);
        model.addAttribute("workshopId", id);
        model.addAttribute("isEdit", true);
        return "admin/workshop-form";
    }

    @PostMapping("/workshops/{id}/edit")
    public String updateWorkshop(@PathVariable Long id,
                                  @Valid @ModelAttribute("workshop") WorkshopRequest request,
                                  BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("workshopId", id);
            model.addAttribute("isEdit", true);
            return "admin/workshop-form";
        }
        workshopService.updateWorkshop(id, request);
        flash.addFlashAttribute("success", "Workshop updated successfully!");
        return "redirect:/admin/workshops";
    }

    @PostMapping("/workshops/{id}/cancel")
    public String cancelWorkshop(@PathVariable Long id, RedirectAttributes flash) {
        try {
            workshopService.cancelWorkshop(id);
            flash.addFlashAttribute("success", "Workshop cancelled successfully!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/workshops";
    }

    @GetMapping("/workshops/{id}/registrations")
    public String viewRegistrations(@PathVariable Long id, Model model) {
        model.addAttribute("workshop", workshopService.getWorkshopById(id));
        model.addAttribute("registrations", registrationService.getWorkshopRegistrations(id));
        return "admin/registrations";
    }
}
