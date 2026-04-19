package com.university.workshop.service;

import com.university.workshop.dto.WorkshopRequest;
import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.WorkshopStatus;
import com.university.workshop.exception.ConflictException;
import com.university.workshop.exception.ResourceNotFoundException;
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
class WorkshopServiceTest {

    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private WorkshopService workshopService;

    private Workshop workshop;
    private WorkshopRequest request;

    @BeforeEach
    void setUp() {
        workshop = new Workshop();
        workshop.setId(1L);
        workshop.setTitle("Spring Boot Basics");
        workshop.setDescription("Intro to Spring Boot");
        workshop.setLocation("Main Campus Hall A");
        workshop.setStartDatetime(LocalDateTime.now().plusDays(7));
        workshop.setTotalSeats(20);
        workshop.setSeatsRemaining(20);
        workshop.setStatus(WorkshopStatus.ACTIVE);

        request = new WorkshopRequest();
        request.setTitle("Spring Boot Basics");
        request.setDescription("Intro to Spring Boot");
        request.setLocation("Main Campus Hall A");
        request.setStartDatetime(LocalDateTime.now().plusDays(7));
        request.setTotalSeats(20);
    }

    @Test
    @DisplayName("getAllWorkshops should return all workshops")
    void getAllWorkshops_success() {
        when(workshopRepository.findAll()).thenReturn(List.of(workshop));

        List<Workshop> result = workshopService.getAllWorkshops();

        assertEquals(1, result.size());
        assertEquals("Spring Boot Basics", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getWorkshopById should return workshop when found")
    void getWorkshopById_success() {
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));

        Workshop result = workshopService.getWorkshopById(1L);

        assertEquals("Spring Boot Basics", result.getTitle());
    }

    @Test
    @DisplayName("getWorkshopById should throw 404 when not found")
    void getWorkshopById_notFound() {
        when(workshopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> workshopService.getWorkshopById(99L));
    }

    @Test
    @DisplayName("createWorkshop should set seats_remaining = total_seats and status ACTIVE")
    void createWorkshop_success() {
        when(workshopRepository.save(any(Workshop.class))).thenAnswer(inv -> {
            Workshop w = inv.getArgument(0);
            w.setId(1L);
            return w;
        });

        Workshop result = workshopService.createWorkshop(request);

        assertEquals(20, result.getTotalSeats());
        assertEquals(20, result.getSeatsRemaining());
        assertEquals(WorkshopStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("updateWorkshop should update fields and adjust seats_remaining")
    void updateWorkshop_success() {
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(workshopRepository.save(any(Workshop.class))).thenAnswer(inv -> inv.getArgument(0));

        request.setTitle("Updated Title");
        request.setTotalSeats(30);

        Workshop result = workshopService.updateWorkshop(1L, request);

        assertEquals("Updated Title", result.getTitle());
        assertEquals(30, result.getTotalSeats());
        assertEquals(30, result.getSeatsRemaining()); // was 20 remaining + 10 seat increase
    }

    @Test
    @DisplayName("updateWorkshop should throw conflict for cancelled workshop")
    void updateWorkshop_cancelled_shouldThrow() {
        workshop.setStatus(WorkshopStatus.CANCELLED);
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));

        assertThrows(ConflictException.class,
                () -> workshopService.updateWorkshop(1L, request));
    }

    @Test
    @DisplayName("cancelWorkshop should set status to CANCELLED")
    void cancelWorkshop_success() {
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(workshopRepository.save(any(Workshop.class))).thenAnswer(inv -> inv.getArgument(0));

        Workshop result = workshopService.cancelWorkshop(1L);

        assertEquals(WorkshopStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("cancelWorkshop should throw if already cancelled")
    void cancelWorkshop_alreadyCancelled() {
        workshop.setStatus(WorkshopStatus.CANCELLED);
        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));

        assertThrows(ConflictException.class,
                () -> workshopService.cancelWorkshop(1L));
    }
}
