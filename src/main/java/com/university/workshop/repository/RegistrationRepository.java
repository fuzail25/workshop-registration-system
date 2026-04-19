package com.university.workshop.repository;

import com.university.workshop.entity.Registration;
import com.university.workshop.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByUserIdAndWorkshopId(Long userId, Long workshopId);
    List<Registration> findByUserId(Long userId);
    List<Registration> findByWorkshopId(Long workshopId);
}
