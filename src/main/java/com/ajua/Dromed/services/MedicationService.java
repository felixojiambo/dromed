package com.ajua.Dromed.services;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationService {
    @Autowired
    private MedicationRepository medicationRepository;

    public Medication loadMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public List<Medication> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(DroneMedication::getMedication)
                .collect(Collectors.toList());
    }
}
