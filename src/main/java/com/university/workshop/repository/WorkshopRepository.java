package com.university.workshop.repository;

import com.university.workshop.entity.Workshop;
import com.university.workshop.enums.WorkshopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    List<Workshop> findByStatus(WorkshopStatus status);
}
