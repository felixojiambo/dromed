package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.DroneMedicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/drone-medications")
@Api(value = "Drone Dispatch System for Medication Transport", tags = "Drone Medications")
public class DroneMedicationController {

    @Autowired
    private DroneMedicationService droneMedicationService;

    @PostMapping("/load")
    @ApiOperation(value = "Load a drone with medication", response = DroneMedication.class)
    public ResponseEntity<DroneMedication> loadDroneWithMedication(@RequestParam Long droneId, @RequestBody Medication medication) {
        return new ResponseEntity<>(droneMedicationService.loadDroneWithMedication(droneId, medication), HttpStatus.CREATED);
    }
}
