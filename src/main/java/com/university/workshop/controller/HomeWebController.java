package com.university.workshop.controller;

import com.university.workshop.entity.User;
import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.RegistrationStatus;
import com.university.workshop.repository.RegistrationRepository;
import com.university.workshop.service.RegistrationService;
import com.university.workshop.service.UserService;
import com.university.workshop.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final WorkshopService workshopService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("workshops", workshopService.getAllWorkshops());
        return "home";
    }

    @GetMapping("/workshops/{id}")
    public String workshopDetail(@PathVariable Long id, Model model, Authentication auth) {
        Workshop workshop = workshopService.getWorkshopById(id);
        model.addAttribute("workshop", workshop);

        if (auth != null) {
            User user = userService.findByEmail(auth.getName());
            boolean alreadyRegistered = registrationRepository.existsByUserIdAndWorkshopId(user.getId(), id);
            model.addAttribute("alreadyRegistered", alreadyRegistered);
        }
        return "workshop-detail";
    }

    @PostMapping("/workshops/{id}/register")
    public String registerForWorkshop(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        User user = userService.findByEmail(auth.getName());
        try {
            registrationService.register(user, id);
            flash.addFlashAttribute("success", "Successfully registered for the workshop!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/workshops/" + id;
    }
}
