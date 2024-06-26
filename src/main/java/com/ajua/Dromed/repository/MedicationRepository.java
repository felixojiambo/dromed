package com.ajua.Dromed.repository;
import com.ajua.Dromed.models.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MedicationRepository extends JpaRepository<Medication, Long> {
}
