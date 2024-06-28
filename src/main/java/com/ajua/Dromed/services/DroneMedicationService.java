package com.ajua.Dromed.services;

import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneMedicationService {

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    @Autowired
    private DroneRepository droneRepository;

    /**
     * Loads a drone with a specified medication, ensuring the drone's battery level is above 25%
     * and the total weight of medications does not exceed the drone's weight limit.
     *
     * @param droneId The unique identifier of the drone to load.
     * @param medication The medication to load onto the drone.
     * @return A DroneMedication object representing the association between the drone and medication.
     */
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

        return droneMedicationRepository.save(droneMedication);
    }

    /**
     * Retrieves all medications associated with a specific drone.
     *
     * @param droneId The unique identifier of the drone.
     * @return A list of Medication objects associated with the drone.
     */
    public List<Medication> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(DroneMedication::getMedication)
                .collect(Collectors.toList());
    }

    /**
     * Checks the total weight of medications loaded onto a drone.
     *
     * @param droneId The unique identifier of the drone.
     * @return The total weight of medications loaded onto the drone.
     */
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }
}
