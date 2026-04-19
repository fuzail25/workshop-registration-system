package com.university.workshop.service;

import com.university.workshop.dto.WorkshopRequest;
import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.WorkshopStatus;
import com.university.workshop.exception.ConflictException;
import com.university.workshop.exception.ResourceNotFoundException;
import com.university.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public List<Workshop> getAllWorkshops() {
        return workshopRepository.findAll();
    }

    public Workshop getWorkshopById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop not found with id: " + id));
    }

    public Workshop createWorkshop(WorkshopRequest request) {
        Workshop workshop = new Workshop();
        workshop.setTitle(request.getTitle());
        workshop.setDescription(request.getDescription());
        workshop.setLocation(request.getLocation());
        workshop.setStartDatetime(request.getStartDatetime());
        workshop.setTotalSeats(request.getTotalSeats());
        workshop.setSeatsRemaining(request.getTotalSeats());
        workshop.setStatus(WorkshopStatus.ACTIVE);

        return workshopRepository.save(workshop);
    }

    public Workshop updateWorkshop(Long id, WorkshopRequest request) {
        Workshop workshop = getWorkshopById(id);

        if (workshop.getStatus() == WorkshopStatus.CANCELLED) {
            throw new ConflictException("Cannot update a cancelled workshop");
        }

        workshop.setTitle(request.getTitle());
        workshop.setDescription(request.getDescription());
        workshop.setLocation(request.getLocation());
        workshop.setStartDatetime(request.getStartDatetime());

        int oldTotal = workshop.getTotalSeats();
        int newTotal = request.getTotalSeats();
        int seatDifference = newTotal - oldTotal;
        workshop.setTotalSeats(newTotal);
        workshop.setSeatsRemaining(workshop.getSeatsRemaining() + seatDifference);

        return workshopRepository.save(workshop);
    }

    public Workshop cancelWorkshop(Long id) {
        Workshop workshop = getWorkshopById(id);

        if (workshop.getStatus() == WorkshopStatus.CANCELLED) {
            throw new ConflictException("Workshop is already cancelled");
        }

        workshop.setStatus(WorkshopStatus.CANCELLED);
        return workshopRepository.save(workshop);
    }
}
