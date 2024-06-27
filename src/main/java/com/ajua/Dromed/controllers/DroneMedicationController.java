package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.DroneMedicationService;
import com.ajua.Dromed.messaging.MedicationLoadedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;

@RestController
@RequestMapping("/api/drone-medications")
@Tag(name = "Drone Medications", description = "Drone Dispatch System for Medication Transport")
public class DroneMedicationController {

    @Autowired
    private DroneMedicationService droneMedicationService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/load")
    @Operation(summary = "Load a drone with medication")
    public ResponseEntity<DroneMedication> loadDroneWithMedication(@RequestParam Long droneId, @RequestBody Medication medication) {
        DroneMedication droneMedication = droneMedicationService.loadDroneWithMedication(droneId, medication);

        // Publish an event
        MedicationLoadedEvent event = new MedicationLoadedEvent(droneId, medication.getId());
        kafkaTemplate.send("drone-events", event);

        return new ResponseEntity<>(droneMedication, HttpStatus.CREATED);
    }
}
