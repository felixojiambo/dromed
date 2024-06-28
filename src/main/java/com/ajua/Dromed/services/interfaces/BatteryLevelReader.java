package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.models.Drone;

public interface BatteryLevelReader {
    int readBatteryLevel(Drone drone);
}