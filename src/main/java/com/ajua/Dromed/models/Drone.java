package com.ajua.Dromed.models;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Model model;

    @Column(nullable = false)
    private int weightLimit;

    @Column(nullable = false)
    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;
}
