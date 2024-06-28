package com.ajua.Dromed.services;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.repository.MedicationRepository;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@EnableRetry
@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    /**
     * Saves a new medication to the repository.
     *
     * @param medication The medication to save.
     * @return The saved Medication object.
     */
    public Medication loadMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    /**
     * Retrieves all medications associated with a specific drone, with retry logic to handle transient failures.
     *
     * @param droneId The unique identifier of the drone.
     * @return A list of Medication objects associated with the drone.
     */
    @Retryable(maxAttempts = 3, retryFor = RuntimeException.class, backoff = @Backoff(delay = 2000))
    public List<Medication> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(DroneMedication::getMedication)
                .collect(Collectors.toList());
    }

}
