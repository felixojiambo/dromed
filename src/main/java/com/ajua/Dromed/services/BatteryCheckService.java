package com.ajua.Dromed.services;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for monitoring and updating the battery levels of drones.
 * It periodically checks the battery levels of all drones and notifies registered observers about any changes.
 */
@Service
public class BatteryCheckService implements Runnable {
    /**
     * Repository for accessing drone data from the database.
     */
    @Autowired
    private DroneRepository droneRepository;

    /**
     * List of observers that are notified when a drone's battery level changes.
     */
    private final List<BatteryObserver> observers = new ArrayList<>();

    /**
     * Adds an observer to the list of observers. Observers are notified about changes in drone battery levels.
     *
     * @param observer The observer to be added.
     */
    public void addObserver(BatteryObserver observer) {
        observers.add(observer);
    }

    /**
     * Periodically checks the battery levels of all drones every minute and notifies all registered observers.
     */
    @Scheduled(fixedRate = 60000) // runs every minute
    public void checkBatteryLevels() {
        List<Drone> drones = droneRepository.findAll();
        drones.forEach(drone -> {
            observers.forEach(observer -> observer.update(drone));
        });
    }

    /**
     * Implementation of the Runnable interface's run method, which triggers the periodic battery level check.
     */
    @Override
    public void run() {
        checkBatteryLevels();
    }
}
