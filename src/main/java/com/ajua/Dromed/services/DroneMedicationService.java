package com.ajua.Dromed.services;

import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.messaging.MedicationLoadedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DroneMedicationService {

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public DroneMedication loadDroneWithMedication(Long droneId, Medication medication) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (drone.getBatteryCapacity() < 25) {
            throw new IllegalStateException("Battery level is below 25%");
        }

        int totalWeight = droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();

        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
            throw new IllegalStateException("Weight limit exceeded");
        }

        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);

        droneMedication = droneMedicationRepository.save(droneMedication);

        // Publish an event
        MedicationLoadedEvent event = new MedicationLoadedEvent(droneId, medication.getId());
        kafkaTemplate.send("drone-events", event);

        return droneMedication;
    }
}
