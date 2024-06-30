package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.Medication;

public abstract class AbstractDroneService {
    protected static final int MAX_WEIGHT_LIMIT = 500;
    protected static final int MIN_BATTERY_LEVEL = 25;

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

    protected void validateBatteryLevel(int batteryCapacity) {
        if (batteryCapacity < MIN_BATTERY_LEVEL) {
            throw new IllegalStateException("Battery level is below 25%");
        }
    }

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

    protected abstract int getTotalLoadedWeight(Long droneId);
}
