package com.university.workshop.entity;

import com.university.workshop.enums.WorkshopStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workshops")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 80, message = "Title must be between 5 and 80 characters")
    @Column(nullable = false)
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    @NotNull(message = "Start date/time is required")
    @Future(message = "Start date/time must be in the future")
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "seats_remaining", nullable = false)
    private int seatsRemaining;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkshopStatus status = WorkshopStatus.ACTIVE;
}
