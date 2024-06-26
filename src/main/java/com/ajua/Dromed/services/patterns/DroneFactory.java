package com.ajua.Dromed.services.patterns;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
public class DroneFactory {
    public static Drone createDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        return new Drone(null, serialNumber, model, weightLimit, batteryCapacity, state);
    }
}
