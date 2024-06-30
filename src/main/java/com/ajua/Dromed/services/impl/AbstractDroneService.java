package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.Medication;

/**
 * Abstract service class for drone-related operations.
 * Provides common validation and utility methods for managing drones and their loading conditions.
 */
public abstract class AbstractDroneService {
    protected static final int MAX_WEIGHT_LIMIT = 500;
    protected static final int MIN_BATTERY_LEVEL = 25;

    /**
     * Validates the loading conditions of a drone for a given medication.
     *
     * @param drone The drone to be validated.
     * @param medication The medication to be loaded onto the drone.
     * @throws OverweightException if the weight limit is exceeded.
     * @throws DroneNotAvailableException if the drone is not available for loading.
     */
    protected void validateLoadingConditions(Drone drone, Medication medication) {
        validateBatteryLevel(drone.getBatteryCapacity());
        validateMedication(medication);

        int totalWeight = getTotalLoadedWeight(drone.getId());
        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
            throw new OverweightException("Weight limit exceeded");
        }

        if (!drone.getState().equals(State.IDLE) && !drone.getState().equals(State.LOADING)) {
            throw new DroneNotAvailableException("Drone is not available for loading");
        }
    }

    /**
     * Validates the battery level of a drone.
     *
     * @param batteryCapacity The current battery capacity of the drone.
     * @throws IllegalStateException if the battery level is below the minimum required.
     */
    protected void validateBatteryLevel(int batteryCapacity) {
        if (batteryCapacity < MIN_BATTERY_LEVEL) {
            throw new IllegalStateException("Battery level is below 25%");
        }
    }

    /**
     * Validates the properties of a medication.
     *
     * @param medication The medication to be validated.
     * @throws IllegalArgumentException if the medication's name or code contains invalid characters.
     */
    protected void validateMedication(Medication medication) {
        String namePattern = "^[a-zA-Z0-9-_]+$";
        String codePattern = "^[A-Z0-9_]+$";

        if (!medication.getName().matches(namePattern)) {
            throw new IllegalArgumentException("Medication name contains invalid characters");
        }
        if (!medication.getCode().matches(codePattern)) {
            throw new IllegalArgumentException("Medication code contains invalid characters");
        }
    }

    /**
     * Gets the total weight of medications loaded on a drone.
     * Must be implemented by subclasses.
     *
     * @param droneId The ID of the drone.
     * @return The total weight of medications loaded on the drone.
     */
    protected abstract int getTotalLoadedWeight(Long droneId);
}
