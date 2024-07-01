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

    public static class Builder {
        private Long id;
        private String serialNumber;
        private Model model;
        private int weightLimit;
        private int batteryCapacity;
        private State state;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder weightLimit(int weightLimit) {
            this.weightLimit = weightLimit;
            return this;
        }

        public Builder batteryCapacity(int batteryCapacity) {
            this.batteryCapacity = batteryCapacity;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Drone build() {
            return new Drone(id, serialNumber, model, weightLimit, batteryCapacity, state);
        }
    }
}
