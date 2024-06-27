package com.ajua.Dromed.controllers;

import com.ajua.Dromed.messaging.DroneRegisteredEvent;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.DroneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@Tag(name = "Drones", description = "Drone Dispatch System for Medication Transport")
public class DroneController {

    @Autowired
    private DroneService droneService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    @Operation(summary = "Register a new drone")
    public ResponseEntity<Drone> registerDrone(@RequestBody Drone drone) {
        Drone registeredDrone = droneService.registerDrone(
                drone.getSerialNumber(),
                drone.getModel(),
                drone.getWeightLimit(),
                drone.getBatteryCapacity(),
                drone.getState()
        );

        // Publish an event
        DroneRegisteredEvent event = new DroneRegisteredEvent(
                registeredDrone.getId(),
                registeredDrone.getSerialNumber(),
                registeredDrone.getModel().toString(),
                registeredDrone.getWeightLimit(),
                registeredDrone.getBatteryCapacity(),
                registeredDrone.getState().toString()
        );
        kafkaTemplate.send("drone-events", event);

        return new ResponseEntity<>(registeredDrone, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available drones")
    public ResponseEntity<List<Drone>> getAvailableDrones() {
        return new ResponseEntity<>(droneService.getAvailableDrones(), HttpStatus.OK);
    }

    @GetMapping("/{id}/battery")
    @Operation(summary = "Check drone battery level")
    public ResponseEntity<Integer> checkDroneBatteryLevel(@PathVariable Long id) {
        return new ResponseEntity<>(droneService.checkDroneBatteryLevel(id), HttpStatus.OK);
    }
}
