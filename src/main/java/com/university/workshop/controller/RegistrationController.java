package com.university.workshop.controller;

import com.university.workshop.entity.Registration;
import com.university.workshop.entity.User;
import com.university.workshop.service.RegistrationService;
import com.university.workshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    /**
     * POST /api/v1/workshops/{id}/registrations
     * Registers the logged-in user for a workshop.
     * Note: For Part 1, userId is passed via request header X-User-Id.
     *       In Part 2, this will be replaced with SecurityContext (logged-in user).
     */
    @PostMapping("/workshops/{id}/registrations")
    public ResponseEntity<Registration> register(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        User user = userService.findById(userId);
        Registration registration = registrationService.register(user, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    /**
     * DELETE /api/v1/registrations/{registrationId}
     * Cancels a registration (must belong to the logged-in user unless ADMIN).
     */
    @DeleteMapping("/registrations/{registrationId}")
    public ResponseEntity<Registration> cancelRegistration(
            @PathVariable Long registrationId,
            @RequestHeader("X-User-Id") Long userId) {
        User user = userService.findById(userId);
        Registration cancelled = registrationService.cancelRegistration(registrationId, user);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * GET /api/v1/me/registrations
     * Returns the logged-in user's registrations.
     */
    @GetMapping("/me/registrations")
    public ResponseEntity<List<Registration>> getMyRegistrations(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(registrationService.getUserRegistrations(userId));
    }
}
