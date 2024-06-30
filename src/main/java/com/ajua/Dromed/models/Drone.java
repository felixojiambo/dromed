package com.ajua.Dromed.models;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Serial number is mandatory")
    @Column(length = 100, unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Model model;

    @Positive(message = "Weight limit must be positive")
    @Max(value = 500, message = "Weight limit cannot exceed 500 grams")
    @Column(nullable = false)
    private int weightLimit;

    @Positive(message = "Battery capacity must be positive")
    @Max(value = 100, message = "Battery capacity cannot exceed 100%")
    @Column(nullable = false)
    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

}
