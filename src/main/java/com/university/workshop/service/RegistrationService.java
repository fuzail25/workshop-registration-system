package com.university.workshop.service;

import com.university.workshop.entity.Registration;
import com.university.workshop.entity.User;
import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.RegistrationStatus;
import com.university.workshop.enums.Role;
import com.university.workshop.exception.ConflictException;
import com.university.workshop.exception.ResourceNotFoundException;
import com.university.workshop.exception.UnauthorizedException;
import com.university.workshop.repository.RegistrationRepository;
import com.university.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final WorkshopRepository workshopRepository;

    /**
     * Register a user for a workshop.
     * Business Rules enforced:
     *   Rule A — Seat limit: if seats_remaining == 0, fail with 409
     *   Rule B — No duplicate: user cannot register twice for the same workshop
     *   Rule C — Past workshop: cannot register for workshops in the past
     */
    @Transactional
    public Registration register(User user, Long workshopId) {
        Workshop workshop = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop not found with id: " + workshopId));

        // Rule C — Cannot register for past workshops
        if (workshop.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot register for a workshop that has already started");
        }

        // Rule B — No duplicate registration
        if (registrationRepository.existsByUserIdAndWorkshopId(user.getId(), workshopId)) {
            throw new ConflictException("You are already registered for this workshop");
        }

        // Rule A — Seat limit (sold out)
        if (workshop.getSeatsRemaining() <= 0) {
            throw new ConflictException("Workshop is sold out — no seats remaining");
        }

        // Decrease seats remaining
        workshop.setSeatsRemaining(workshop.getSeatsRemaining() - 1);
        workshopRepository.save(workshop);

        // Create registration
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setWorkshop(workshop);
        registration.setStatus(RegistrationStatus.ACTIVE);

        return registrationRepository.save(registration);
    }

    /**
     * Cancel a registration.
     * Business Rule D — Can only cancel if the workshop has not started.
     * Cancelling marks registration CANCELLED and increases seats_remaining by 1.
     */
    @Transactional
    public Registration cancelRegistration(Long registrationId, User currentUser) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + registrationId));

        // Authorization: must be the owner or an ADMIN
        if (!registration.getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to cancel this registration");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ConflictException("Registration is already cancelled");
        }

        // Rule D — Cannot cancel after workshop has started
        Workshop workshop = registration.getWorkshop();
        if (workshop.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot cancel registration — the workshop has already started");
        }

        // Mark cancelled and increase seats
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);

        workshop.setSeatsRemaining(workshop.getSeatsRemaining() + 1);
        workshopRepository.save(workshop);

        return registration;
    }

    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    public List<Registration> getWorkshopRegistrations(Long workshopId) {
        return registrationRepository.findByWorkshopId(workshopId);
    }
}
