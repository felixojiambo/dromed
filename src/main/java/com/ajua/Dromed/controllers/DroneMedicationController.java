package com.ajua.Dromed.controllers;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.DroneMedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/drone-medications")
public class DroneMedicationController {
    @Autowired
    private DroneMedicationService droneMedicationService;

    @PostMapping("/load")
    public ResponseEntity<DroneMedication> loadDroneWithMedication(@RequestParam Long droneId, @RequestBody Medication medication) {
        return new ResponseEntity<>(droneMedicationService.loadDroneWithMedication(droneId, medication), HttpStatus.CREATED);
    }
}
