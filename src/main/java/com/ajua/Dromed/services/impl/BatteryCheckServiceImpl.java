package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.interfaces.BatteryCheckService;
import com.ajua.Dromed.services.interfaces.BatteryLevelReader;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatteryCheckServiceImpl implements BatteryCheckService {

    private static final Logger logger = LoggerFactory.getLogger(BatteryCheckServiceImpl.class);

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private BatteryLevelReader batteryLevelReader;

    private final List<BatteryObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(BatteryObserver observer) {
        observers.add(observer);
    }

    @Override
    @Scheduled(fixedRate = 60000) // runs every minute
    public void checkBatteryLevels() {
        List<Drone> drones = droneRepository.findAll();
        drones.forEach(drone -> {
            try {
                int newBatteryLevel = batteryLevelReader.readBatteryLevel(drone);
                drone.setBatteryCapacity(newBatteryLevel);
                droneRepository.save(drone);
                observers.forEach(observer -> observer.update(drone));
            } catch (Exception e) {
                logger.error("Error checking battery level for drone " + drone.getSerialNumber(), e);
            }
        });
    }

    @Override
    public void run() {
        checkBatteryLevels();
    }
}
