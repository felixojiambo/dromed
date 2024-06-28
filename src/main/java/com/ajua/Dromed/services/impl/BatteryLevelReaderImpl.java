package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.interfaces.BatteryLevelReader;
import org.springframework.stereotype.Service;

@Service
public class BatteryLevelReaderImpl implements BatteryLevelReader {
    @Override
    public int readBatteryLevel(Drone drone) {
        // Simulate reading battery
        return (int) (Math.random() * 100);
    }
}
