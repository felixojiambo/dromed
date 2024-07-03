package com.ajua.Dromed.dtos;

import com.ajua.Dromed.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneStateDTO {
    private State state;
}
