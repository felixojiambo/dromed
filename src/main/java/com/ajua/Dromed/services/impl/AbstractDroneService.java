package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
public abstract class AbstractDroneService {
    public static final int MAX_WEIGHT_LIMIT = 500;

    protected final DroneRepository droneRepository;
    protected final DroneMedicationRepository droneMedicationRepository;

    protected AbstractDroneService(DroneRepository droneRepository, DroneMedicationRepository droneMedicationRepository) {
        this.droneRepository = droneRepository;
        this.droneMedicationRepository = droneMedicationRepository;
    }

    protected void validateBatteryLevel(Drone drone) {
        if (drone.getBatteryCapacity() < 25) {
            throw new DroneNotAvailableException("Drone battery is too low to proceed");
        }
    }

    protected void validateDroneState(Drone drone, State expectedState) {
        if (!drone.getState().equals(expectedState)) {
            throw new IllegalStateException("Drone is not in the expected state: " + expectedState);
        }
    }

    protected void transitionDroneState(Drone drone, State newState) {
        drone.setState(newState);
        droneRepository.save(drone);
    }

    protected Drone getAvailableDrone() {
        return droneRepository.findByState(State.IDLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new DroneNotAvailableException("No available drones for loading"));
    }

    protected Drone getDroneById(Long droneId) {
        return droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    protected void validateLoadingConditions(Drone drone, Medication medication) {
        validateBatteryLevel(drone);
        int totalWeight = getTotalLoadedWeight(drone.getId());
        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
            throw new DroneNotAvailableException("Drone cannot carry this weight");
        }
    }

    public int getTotalLoadedWeight(Long droneId) { // Kept public
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

    protected List<Medication> findMedicationsByDrone(Long droneId) { // Method renamed
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(DroneMedication::getMedication)
                .collect(Collectors.toList());
    }
}
