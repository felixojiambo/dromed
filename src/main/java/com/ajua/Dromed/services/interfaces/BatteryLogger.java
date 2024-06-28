package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;

public interface BatteryLogger extends BatteryObserver {
    void update(Drone drone);
}
