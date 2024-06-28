package com.ajua.Dromed.dtos;

import lombok.Data;

@Data
public class MedicationDTO {
    private Long id;
    private String name;
    private int weight;
    private String code;
    private String imageUrl;
}