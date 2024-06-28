package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.patterns.BatteryObserver;
import java.util.List;

public interface BatteryCheckService extends Runnable {
    void addObserver(BatteryObserver observer);
    void checkBatteryLevels();
}
