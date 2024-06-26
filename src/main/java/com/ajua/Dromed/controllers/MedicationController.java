package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    @Autowired
    private MedicationService medicationService;

    @PostMapping
    public ResponseEntity<Medication> loadMedication(@RequestBody Medication medication) {
        return new ResponseEntity<>(medicationService.loadMedication(medication), HttpStatus.CREATED);
    }

    @GetMapping("/drone/{droneId}")
    public ResponseEntity<List<Medication>> getMedicationsByDrone(@PathVariable Long droneId) {
        return new ResponseEntity<>(medicationService.getMedicationsByDrone(droneId), HttpStatus.OK);
    }
}
