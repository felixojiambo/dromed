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

/**
 * Service implementation for managing drones.
 */
@Service
public class DroneServiceImpl implements DroneService {

    private static final int MAX_WEIGHT_LIMIT = 500;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    /**
     * Registers a new drone with the specified parameters.
     *
     * @param serialNumber the serial number of the drone
     * @param model the model of the drone
     * @param weightLimit the weight limit of the drone
     * @param batteryCapacity the battery capacity of the drone
     * @param state the state of the drone
     * @return the registered drone
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
     *
     * @param medication the medication to load
     * @return the DroneMedication record
     */
    @Override
    @Transactional
    public DroneMedication loadDroneWithMedication(Medication medication) {
        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.getFirst(); // Get the first available drone

        if (drone.getBatteryCapacity() < 25) {
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
     * Retrieves the list of medications loaded on a specified drone.
     *
     * @param droneId the ID of the drone
     * @return the list of medications
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
     * Retrieves the list of available drones (in IDLE state).
     *
     * @return the list of available drones
     */
    @Override
    @Transactional
    public List<Drone> getAvailableDrones() {
        return droneRepository.findByState(State.IDLE);
    }

    /**
     * Checks the battery level of a specified drone.
     *
     * @param droneId the ID of the drone
     * @return the battery level
     */
    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    /**
     * Calculates the total weight of the medications loaded on a specified drone.
     *
     * @param droneId the ID of the drone
     * @return the total loaded weight
     */
    @Override
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

    /**
     * Starts the delivery process for a specified drone.
     *
     * @param droneId the ID of the drone
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
     * Completes the delivery process for a specified drone.
     *
     * @param droneId the ID of the drone
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
     * Returns a specified drone to base after delivery.
     *
     * @param droneId the ID of the drone
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
