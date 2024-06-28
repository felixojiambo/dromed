package com.ajua.Dromed.services.impl;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.interfaces.DroneService;
import com.ajua.Dromed.services.patterns.DroneFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneServiceImpl implements DroneService {

    private static final int MAX_WEIGHT_LIMIT = 500;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    /**
     * Registers a new drone with the given parameters, ensuring the weight limit does not exceed the maximum allowed.
     * Creates and saves a new drone instance using the DroneFactory.
     *
     * @param serialNumber Unique identifier for the drone.
     * @param model The model of the drone.
     * @param weightLimit Maximum weight the drone can carry.
     * @param batteryCapacity Current battery capacity percentage.
     * @param state The initial state of the drone.
     * @return The saved Drone object.
     */
    @Override
    @Transactional
    public Drone registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        if (weightLimit > MAX_WEIGHT_LIMIT) {
            throw new IllegalArgumentException("Weight limit cannot exceed 500 grams");
        }
        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return droneRepository.save(drone);
    }

    /**
     * Loads a specified medication onto a drone, ensuring the drone's battery level is sufficient and the total weight does not exceed the drone's limit.
     * Saves the association between the drone and medication.
     *
     * @param droneId The unique identifier of the drone to load.
     * @param medication The medication to load onto the drone.
     * @return A DroneMedication object representing the association.
     */
    @Override
    @Transactional
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
     * Retrieves all medications associated with a specific drone, applying retry logic for transient failures.
     *
     * @param droneId The unique identifier of the drone.
     * @return A list of Medication objects associated with the drone.
     */
    @Override
    @Retryable(maxAttempts = 3, retryFor = RuntimeException.class, backoff = @Backoff(delay = 2000))
    public List<Medication> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(DroneMedication::getMedication)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all drones currently in the IDLE state, indicating availability for loading.
     *
     * @return A list of available drones.
     */
    @Override
    @Transactional
    public List<Drone> getAvailableDrones() {
        return droneRepository.findByState(State.IDLE);
    }
    /**
     * Checks the battery level of a drone identified by its ID.
     *
     * @param droneId The unique identifier of the drone.
     * @return The current battery capacity percentage of the drone.
     */
    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }
    /**
     * Calculates the total weight of medications loaded onto a drone.
     *
     * @param droneId The unique identifier of the drone.
     * @return The total weight of medications loaded onto the drone.
     */
    @Override
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }
}
