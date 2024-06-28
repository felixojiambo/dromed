package com.ajua.Dromed.services.patterns;

import com.ajua.Dromed.models.Drone;

/**
 * Interface defining the contract for components interested in receiving updates about drone battery levels.
 * Implementations of this interface should define how they respond to updates regarding a drone's battery status.
 */
public interface BatteryObserver {
    /**
     * Called when there is an update to the battery level of a drone.
     * Implementations should contain the logic to handle the updated battery information.
     *
     * @param drone The drone whose battery level has been updated.
     */
    void update(Drone drone);
}
