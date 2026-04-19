package com.university.workshop.controller;

import com.university.workshop.dto.WorkshopRequest;
import com.university.workshop.entity.Registration;
import com.university.workshop.entity.Workshop;
import com.university.workshop.service.RegistrationService;
import com.university.workshop.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/workshops")
@RequiredArgsConstructor
public class AdminWorkshopController {

    private final WorkshopService workshopService;
    private final RegistrationService registrationService;

    /**
     * POST /api/v1/admin/workshops
     * Create a new workshop (ADMIN only).
     */
    @PostMapping
    public ResponseEntity<Workshop> createWorkshop(@Valid @RequestBody WorkshopRequest request) {
        Workshop created = workshopService.createWorkshop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/admin/workshops/{id}
     * Update an existing workshop (ADMIN only).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workshop> updateWorkshop(
            @PathVariable Long id,
            @Valid @RequestBody WorkshopRequest request) {
        Workshop updated = workshopService.updateWorkshop(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/v1/admin/workshops/{id}/cancel
     * Cancel a workshop (ADMIN only).
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Workshop> cancelWorkshop(@PathVariable Long id) {
        Workshop cancelled = workshopService.cancelWorkshop(id);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * GET /api/v1/admin/workshops/{id}/registrations
     * View all registrations for a workshop (ADMIN only).
     */
    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<Registration>> getWorkshopRegistrations(@PathVariable Long id) {
        // Verify workshop exists
        workshopService.getWorkshopById(id);
        return ResponseEntity.ok(registrationService.getWorkshopRegistrations(id));
    }
}
