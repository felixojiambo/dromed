package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BatteryLoggerImpl implements BatteryObserver {

    private static final Logger logger = LoggerFactory.getLogger(BatteryLoggerImpl.class);

    @Override
    public void update(Drone drone) {
        logger.info("Drone " + drone.getSerialNumber() + " battery level: " + drone.getBatteryCapacity() + "%");
    }
}
