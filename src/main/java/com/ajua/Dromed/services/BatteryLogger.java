package com.ajua.Dromed.services;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.stereotype.Service;

@Service
public class BatteryLogger implements BatteryObserver {
    @Override
    public void update(Drone drone) {
        System.out.println("Drone " + drone.getSerialNumber() + " battery level: " + drone.getBatteryCapacity() + "%");
    }
}