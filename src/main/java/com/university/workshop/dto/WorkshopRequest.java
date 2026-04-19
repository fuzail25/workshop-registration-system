package com.university.workshop.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 80, message = "Title must be between 5 and 80 characters")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Start date/time is required")
    @Future(message = "Start date/time must be in the future")
    private LocalDateTime startDatetime;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;
}
