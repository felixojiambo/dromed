package com.ajua.Dromed.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneMedicationDTO {
    private Long id;
    private DroneDTO drone;
    private MedicationDTO medication;

    public DroneMedicationDTO(DroneDTO droneDTO, MedicationDTO medicationDTO) {
    }
}