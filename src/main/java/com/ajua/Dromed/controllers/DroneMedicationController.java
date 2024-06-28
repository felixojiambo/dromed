package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drone-medications")
@Tag(name = "Drone Medications", description = "Drone Dispatch System for Medication Transport")
public class DroneMedicationController {

    @Autowired
    private DroneMedicationService droneMedicationService;

    @PostMapping("/load")
    @Operation(summary = "Load a drone with medication")
    public ResponseEntity<DroneMedication> loadDroneWithMedication(@RequestParam Long droneId, @RequestBody Medication medication) {
        DroneMedication droneMedication = droneMedicationService.loadDroneWithMedication(droneId, medication);

        return new ResponseEntity<>(droneMedication, HttpStatus.CREATED);
    }
}
