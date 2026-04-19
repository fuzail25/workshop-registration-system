package com.university.workshop.service;

import com.university.workshop.entity.Registration;
import com.university.workshop.entity.User;
import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.RegistrationStatus;
import com.university.workshop.enums.Role;
import com.university.workshop.enums.WorkshopStatus;
import com.university.workshop.exception.ConflictException;
import com.university.workshop.exception.ResourceNotFoundException;
import com.university.workshop.exception.UnauthorizedException;
import com.university.workshop.repository.RegistrationRepository;
import com.university.workshop.repository.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private User attendee;
    private User admin;
    private Workshop workshop;

    @BeforeEach
    void setUp() {
        attendee = new User();
        attendee.setId(1L);
        attendee.setName("Test Attendee");
        attendee.setEmail("attendee@test.com");
        attendee.setPasswordHash("hashedpassword");
        attendee.setRole(Role.ATTENDEE);

        admin = new User();
        admin.setId(2L);
        admin.setName("Test Admin");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hashedpassword");
        admin.setRole(Role.ADMIN);

        workshop = new Workshop();
        workshop.setId(1L);
        workshop.setTitle("Spring Boot Basics");
        workshop.setDescription("Learn Spring Boot");
        workshop.setLocation("Main Campus Hall A");
        workshop.setStartDatetime(LocalDateTime.now().plusDays(7));
        workshop.setTotalSeats(20);
        workshop.setSeatsRemaining(10);
        workshop.setStatus(WorkshopStatus.ACTIVE);
    }

    @Test
    @DisplayName("Successful registration should decrease seats and return ACTIVE registration")
    void register_success() {
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(registrationRepository.existsByUserIdAndWorkshopId(1L, 1L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> {
            Registration r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

        Registration result = registrationService.register(attendee, 1L);

        assertNotNull(result);
        assertEquals(RegistrationStatus.ACTIVE, result.getStatus());
        assertEquals(9, workshop.getSeatsRemaining());
        verify(workshopRepository).save(workshop);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("Rule A — Registration should fail when workshop is sold out")
    void register_soldOut_shouldThrowConflict() {
        workshop.setSeatsRemaining(0);
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(registrationRepository.existsByUserIdAndWorkshopId(1L, 1L)).thenReturn(false);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> registrationService.register(attendee, 1L));

        assertTrue(ex.getMessage().contains("sold out"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rule B — Registration should fail for duplicate registration")
    void register_duplicate_shouldThrowConflict() {
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(registrationRepository.existsByUserIdAndWorkshopId(1L, 1L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> registrationService.register(attendee, 1L));

        assertTrue(ex.getMessage().contains("already registered"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rule C — Registration should fail for past workshop")
    void register_pastWorkshop_shouldThrowConflict() {
        workshop.setStartDatetime(LocalDateTime.now().minusDays(1));
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> registrationService.register(attendee, 1L));

        assertTrue(ex.getMessage().contains("already started"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration should fail when workshop not found")
    void register_workshopNotFound_shouldThrow404() {
        when(workshopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> registrationService.register(attendee, 99L));
    }

    @Test
    @DisplayName("Rule D — Successful cancellation should increase seats and mark CANCELLED")
    void cancelRegistration_success() {
        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.ACTIVE);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

        Registration result = registrationService.cancelRegistration(1L, attendee);

        assertEquals(RegistrationStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getCancelledAt());
        assertEquals(11, workshop.getSeatsRemaining());
    }

    @Test
    @DisplayName("Rule D — Cancellation should fail if workshop already started")
    void cancelRegistration_pastWorkshop_shouldThrowConflict() {
        workshop.setStartDatetime(LocalDateTime.now().minusHours(1));
        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.ACTIVE);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> registrationService.cancelRegistration(1L, attendee));

        assertTrue(ex.getMessage().contains("already started"));
    }

    @Test
    @DisplayName("Cancellation should fail if registration already cancelled")
    void cancelRegistration_alreadyCancelled_shouldThrowConflict() {
        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.CANCELLED);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> registrationService.cancelRegistration(1L, attendee));

        assertTrue(ex.getMessage().contains("already cancelled"));
    }

    @Test
    @DisplayName("Non-owner non-admin should not be able to cancel someone else's registration")
    void cancelRegistration_unauthorized_shouldThrow() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setRole(Role.ATTENDEE);

        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.ACTIVE);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        assertThrows(UnauthorizedException.class,
                () -> registrationService.cancelRegistration(1L, otherUser));
    }

    @Test
    @DisplayName("Admin should be able to cancel any registration")
    void cancelRegistration_admin_shouldSucceed() {
        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.ACTIVE);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

        Registration result = registrationService.cancelRegistration(1L, admin);

        assertEquals(RegistrationStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("getUserRegistrations should return list of user's registrations")
    void getUserRegistrations_success() {
        Registration reg = new Registration();
        reg.setId(1L);
        reg.setUser(attendee);
        reg.setWorkshop(workshop);
        reg.setStatus(RegistrationStatus.ACTIVE);

        when(registrationRepository.findByUserId(1L)).thenReturn(List.of(reg));

        List<Registration> result = registrationService.getUserRegistrations(1L);

        assertEquals(1, result.size());
        assertEquals(RegistrationStatus.ACTIVE, result.get(0).getStatus());
    }
}
