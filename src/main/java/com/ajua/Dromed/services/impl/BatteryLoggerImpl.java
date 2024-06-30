package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation for logging battery levels of drones.
 * Implements the BatteryObserver interface to receive updates on battery levels.
 */
@Service
public class BatteryLoggerImpl implements BatteryObserver {

    private static final Logger logger = LoggerFactory.getLogger(BatteryLoggerImpl.class);

    /**
     * Updates the observer with the latest battery level of a drone.
     *
     * @param drone The drone whose battery level has been updated.
     */
    @Override
    public void update(Drone drone) {
        logger.info("Drone " + drone.getSerialNumber() + " battery level: " + drone.getBatteryCapacity() + "%");
    }
}
