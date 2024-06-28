package com.ajua.Dromed.services.patterns;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;

/**
 * Factory class for creating Drone objects.
 * It simplifies the creation of Drone instances by encapsulating the instantiation logic.
 */
public class DroneFactory {
    /**
     * Creates and returns a new Drone object with the specified properties.
     *
     * @param serialNumber The unique serial number of the drone.
     * @param model The model of the drone.
     * @param weightLimit The maximum weight limit the drone can carry.
     * @param batteryCapacity The battery capacity of the drone.
     * @param state The initial state of the drone.
     * @return A new Drone object initialized with the provided parameters.
     */
    public static Drone createDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        return new Drone(null, serialNumber, model, weightLimit, batteryCapacity, state);
    }
}
