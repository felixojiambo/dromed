package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@Tag(name = "Medications", description = "Drone Dispatch System for Medication Transport")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping
    @Operation(summary = "Load a new medication")
    public ResponseEntity<Medication> loadMedication(@RequestBody Medication medication) {
        return new ResponseEntity<>(medicationService.loadMedication(medication), HttpStatus.CREATED);
    }

    @GetMapping("/drone/{droneId}")
    @Operation(summary = "Get medications loaded on a specific drone")
    public ResponseEntity<List<Medication>> getMedicationsByDrone(@PathVariable Long droneId) {
        return new ResponseEntity<>(medicationService.getMedicationsByDrone(droneId), HttpStatus.OK);
    }
}
