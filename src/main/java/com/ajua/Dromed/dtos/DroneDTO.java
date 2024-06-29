package com.ajua.Dromed.dtos;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneDTO {
    private Long id;
    private String serialNumber;
    private Model model;
    private int weightLimit;
    private int batteryCapacity;
    private State state;
}