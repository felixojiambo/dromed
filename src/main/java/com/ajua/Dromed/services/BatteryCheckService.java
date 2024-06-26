package com.ajua.Dromed.services;

import com.ajua.Dromed.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BatteryCheckService implements Runnable {
    @Autowired
    private DroneRepository droneRepository;

    private List<BatteryObserver> observers = new ArrayList<>();

    public void addObserver(BatteryObserver observer) {
        observers.add(observer);
    }

    @Scheduled(fixedRate = 60000) // runs every minute
    public void checkBatteryLevels() {
        List<Drone> drones = droneRepository.findAll();
        drones.forEach(drone -> {
            observers.forEach(observer -> observer.update(drone));
        });
    }

    @Override
    public void run() {
        checkBatteryLevels();
    }
}
