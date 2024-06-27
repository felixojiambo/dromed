package com.ajua.Dromed.services;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.patterns.DroneFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DroneService {

    @Autowired
    private DroneRepository droneRepository;

    /**
     * Registers a new drone with the given parameters.
     *
     * @param serialNumber Unique identifier for the drone.
     * @param model The model of the drone.
     * @param weightLimit Maximum weight the drone can carry.
     * @param batteryCapacity Current battery capacity percentage.
     * @param state The initial state of the drone.
     * @return The saved Drone object.
     */
    @Transactional
    public Drone registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return droneRepository.save(drone);
    }

    /**
     * Retrieves all drones currently in the IDLE state, indicating they are available for loading.
     *
     * @return A list of available drones.
     */
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
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }
}
