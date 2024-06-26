package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.MedicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/medications")
@Api(value = "Drone Dispatch System for Medication Transport", tags = "Medications")
public class MedicationController {
    @Autowired
    private MedicationService medicationService;

    @PostMapping
    @ApiOperation(value = "Load a new medication", response = Medication.class)
    public ResponseEntity<Medication> loadMedication(@RequestBody Medication medication) {
        return new ResponseEntity<>(medicationService.loadMedication(medication), HttpStatus.CREATED);
    }

    @GetMapping("/drone/{droneId}")
    @ApiOperation(value = "Get medications loaded on a specific drone", response = List.class)
    public ResponseEntity<List<Medication>> getMedicationsByDrone(@PathVariable Long droneId) {
        return new ResponseEntity<>(medicationService.getMedicationsByDrone(droneId), HttpStatus.OK);
    }
}
