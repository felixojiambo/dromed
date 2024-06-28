package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
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

    private static final int MAX_WEIGHT_LIMIT = 500; // Max weight limit for drones
    private static final int MIN_BATTERY_LEVEL = 25; // Min battery level for loading

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    DroneMedicationRepository droneMedicationRepository;

    /**
     * Registers a new drone with the specified parameters.
     * Validates weight limit against a maximum threshold.
     *
     * @param serialNumber   Serial number of the drone.
     * @param model          Model of the drone.
     * @param weightLimit    Weight limit of the drone.
     * @param batteryCapacity Battery capacity of the drone.
     * @param state          Initial state of the drone.
     * @return The newly registered Drone object.
     * @throws IllegalArgumentException if weight limit exceeds the maximum allowed.
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
     * Loads a drone with a specified medication.
     * Ensures drone availability, battery level, weight limit, and state before loading.
     *
     * @param medication The medication to load onto the drone.
     * @return The DroneMedication object representing the loaded medication.
     * @throws DroneNotAvailableException if no drone is available for loading.
     * @throws IllegalStateException     if battery level is below 25%, weight limit is exceeded,
     *                                   or drone is not in a suitable state for loading.
     */
    @Override
    @Transactional
    public DroneMedication loadDroneWithMedication(Medication medication) {
        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.get(0); // Get the first available drone

        if (drone.getBatteryCapacity() < MIN_BATTERY_LEVEL) {
            throw new IllegalStateException("Battery level is below 25%");
        }

        int totalWeight = getTotalLoadedWeight(drone.getId());

        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
            throw new OverweightException("Weight limit exceeded");
        }

        if (!drone.getState().equals(State.IDLE) && !drone.getState().equals(State.LOADING)) {
            throw new DroneNotAvailableException("Drone is not available for loading");
        }

        drone.setState(State.LOADING);
        droneRepository.save(drone);

        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);

        droneMedicationRepository.save(droneMedication);

        // Update state to LOADED if the drone is fully loaded
        drone.setState(State.LOADED);
        droneRepository.save(drone);

        return droneMedication;
    }

    /**
     * Retrieves medications loaded onto a specific drone.
     * Uses retry mechanism with 3 attempts on failure, with a delay of 2 seconds between retries.
     *
     * @param droneId The ID of the drone to fetch medications for.
     * @return List of medications loaded onto the drone.
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
     * Retrieves a list of available drones that are currently idle.
     *
     * @return List of available drones.
     */
    @Override
    public List<Drone> getAvailableDrones() {
        return droneRepository.findByState(State.IDLE);
    }

    /**
     * Checks the battery level of a specific drone.
     *
     * @param droneId The ID of the drone to check battery level for.
     * @return Battery level percentage.
     * @throws ResourceNotFoundException if the drone with the given ID is not found.
     */
    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    /**
     * Calculates the total weight of medications loaded onto a specific drone.
     *
     * @param droneId The ID of the drone to calculate loaded weight for.
     * @return Total weight of medications loaded on the drone.
     */
    @Override
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

    /**
     * Initiates delivery for a specific drone.
     * Changes the drone state to DELIVERING if it's loaded.
     *
     * @param droneId The ID of the drone to start delivery for.
     * @throws IllegalStateException if the drone is not in a loaded state.
     */
    @Override
    public void startDelivery(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.LOADED)) {
            throw new IllegalStateException("Drone is not ready for delivery");
        }

        drone.setState(State.DELIVERING);
        droneRepository.save(drone);
    }

    /**
     * Completes delivery for a specific drone.
     * Changes the drone state to DELIVERED if it's currently delivering.
     *
     * @param droneId The ID of the drone to complete delivery for.
     * @throws IllegalStateException if the drone is not in a delivering state.
     */
    @Override
    public void completeDelivery(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.DELIVERING)) {
            throw new IllegalStateException("Drone is not delivering");
        }

        drone.setState(State.DELIVERED);
        droneRepository.save(drone);
    }

    /**
     * Commands a drone to return to base after completing a delivery.
     * Changes the drone state to RETURNING if it's delivered.
     * Updates the state to IDLE upon return.
     *
     * @param droneId The ID of the drone to return to base.
     * @throws IllegalStateException if the drone is not in a delivered state.
     */
    @Override
    public void returnToBase(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.DELIVERED)) {
            throw new IllegalStateException("Drone is not in a state to return");
        }

        drone.setState(State.RETURNING);
        droneRepository.save(drone);

        // After returning
        drone.setState(State.IDLE);
        droneRepository.save(drone);
    }
}
