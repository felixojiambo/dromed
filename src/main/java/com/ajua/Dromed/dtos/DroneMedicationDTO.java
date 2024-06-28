package com.ajua.Dromed.dtos;

import lombok.Data;

@Data
public class DroneMedicationDTO {
    private Long id;
    private DroneDTO drone;
    private MedicationDTO medication;
}