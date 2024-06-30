package com.ajua.Dromed.utils;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;

public class DTOConverter {

    public static DroneDTO toDroneDTO(Drone drone) {
        return new DroneDTO(drone.getId(), drone.getSerialNumber(), drone.getModel(),
                drone.getWeightLimit(), drone.getBatteryCapacity(), drone.getState());
    }

    public static MedicationDTO toMedicationDTO(Medication medication) {
        return new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(),
                medication.getCode(), medication.getImageUrl());
    }

    public static DroneMedicationDTO toDroneMedicationDTO(DroneMedication droneMedication) {
        return new DroneMedicationDTO(droneMedication.getId(), toDroneDTO(droneMedication.getDrone()),
                toMedicationDTO(droneMedication.getMedication()));
    }
}
