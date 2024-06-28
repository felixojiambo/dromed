package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import jakarta.transaction.Transactional;

import java.util.List;

public interface DroneService {

    Drone registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state);

    DroneMedication loadDroneWithMedication(Long droneId, Medication medication);

    List<Medication> getMedicationsByDrone(Long droneId);

    List<Drone> getAvailableDrones();

    int checkDroneBatteryLevel(Long droneId);

    int getTotalLoadedWeight(Long droneId);
}
