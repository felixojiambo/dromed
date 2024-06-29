package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DroneService {

    DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state);

    DroneMedicationDTO loadDroneWithMedication(MedicationDTO medicationDTO);

    List<MedicationDTO> getMedicationsByDrone(Long droneId);

    List<DroneDTO> getAvailableDrones();

    int checkDroneBatteryLevel(Long droneId);

    int getTotalLoadedWeight(Long droneId);

    void startDelivery(Long droneId);

    void completeDelivery(Long droneId);

    void returnToBase(Long droneId);

    @Transactional
    ResponseEntity<Object> markIdle(Long id);
}
