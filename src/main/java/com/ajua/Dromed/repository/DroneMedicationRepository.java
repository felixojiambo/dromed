package com.ajua.Dromed.repository;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DroneMedicationRepository extends JpaRepository<DroneMedication, Long> {
    List<DroneMedication> findByDroneId(Long droneId);

    //ScopedValue<Object> findByDroneAndMedication(Drone drone, Medication medication);
}
