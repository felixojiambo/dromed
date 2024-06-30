package com.ajua.Dromed.services.patterns;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.impl.AbstractDroneService;

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
        validateSerialNumber(serialNumber);
        validateWeightLimit(weightLimit);
        validateBatteryCapacity(batteryCapacity);
        validateState(state);

        return new Drone(null, serialNumber, model, weightLimit, batteryCapacity, state);
    }

    private static void validateSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.length() > 100) {
            throw new IllegalArgumentException("Serial number must be non-null and up to 100 characters");
        }
    }

    private static void validateWeightLimit(int weightLimit) {
        if (weightLimit <= 0 || weightLimit > AbstractDroneService.MAX_WEIGHT_LIMIT) {
            throw new IllegalArgumentException("Weight limit must be greater than 0 and up to 500 grams");
        }
    }

    private static void validateBatteryCapacity(int batteryCapacity) {
        if (batteryCapacity < 0 || batteryCapacity > 100) {
            throw new IllegalArgumentException("Battery capacity must be between 0 and 100 percent");
        }
    }

    private static void validateState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State must be non-null");
        }
    }
}
