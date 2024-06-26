package com.ajua.Dromed.services.patterns;

import com.ajua.Dromed.models.Drone;

public interface BatteryObserver {
    void update(Drone drone);
}