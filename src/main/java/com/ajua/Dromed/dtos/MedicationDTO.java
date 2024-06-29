package com.ajua.Dromed.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {
    private Long id;
    private String name;
    private int weight;
    private String code;
    private String imageUrl;


}