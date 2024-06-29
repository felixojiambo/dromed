package com.ajua.Dromed.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWithDrones {
    private boolean success;
    private String message;
    private List<DroneDTO> data;
}