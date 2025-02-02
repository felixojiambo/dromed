package com.ajua.Dromed.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWithDrone {
    private boolean success;
    private String message;
    private DroneDTO data;
}
