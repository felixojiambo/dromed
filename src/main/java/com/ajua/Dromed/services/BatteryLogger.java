package com.ajua.Dromed.services;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.stereotype.Service;

/**
 * Implements the BatteryObserver interface to log battery levels of drones.
 * Whenever a drone's battery level is updated, this service logs the serial number and current battery capacity percentage.
 */
@Service
public class BatteryLogger implements BatteryObserver {
    /**
     * Updates the battery level of a given drone and logs the information.
     *
     * @param drone The drone whose battery level has been updated.
     */
    @Override
    public void update(Drone drone) {
        System.out.println("Drone " + drone.getSerialNumber() + " battery level: " + drone.getBatteryCapacity() + "%");
    }
}
