package com.university.workshop.controller;

import com.university.workshop.entity.User;
import com.university.workshop.service.RegistrationService;
import com.university.workshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MyRegistrationsWebController {

    private final RegistrationService registrationService;
    private final UserService userService;

    @GetMapping("/my/registrations")
    public String myRegistrations(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("registrations", registrationService.getUserRegistrations(user.getId()));
        return "my-registrations";
    }

    @PostMapping("/my/registrations/{id}/cancel")
    public String cancelRegistration(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        User user = userService.findByEmail(auth.getName());
        try {
            registrationService.cancelRegistration(id, user);
            flash.addFlashAttribute("success", "Registration cancelled successfully!");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my/registrations";
    }
}
