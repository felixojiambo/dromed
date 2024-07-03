package com.ajua.Dromed.dtos;

import io.micrometer.observation.Observation;
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

    public DroneMedicationDTO(Long id, MedicationDTO medicationDTO) {
    }

    public Observation.ContextView getMedicationDTO() {
        return null;
    }
}