package com.ajua.Dromed.controllers;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.services.DroneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@Api(value = "Drone Dispatch System for Medication Transport", tags = "Drones")
public class DroneController {
    @Autowired
    private DroneService droneService;

    @PostMapping
    @ApiOperation(value = "Register a new drone", response = Drone.class)
    public ResponseEntity<Drone> registerDrone(@RequestBody Drone drone) {
        return new ResponseEntity<>(droneService.registerDrone(
                drone.getSerialNumber(),
                drone.getModel(),
                drone.getWeightLimit(),
                drone.getBatteryCapacity(),
                drone.getState()
        ), HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @ApiOperation(value = "Get available drones", response = List.class)
    public ResponseEntity<List<Drone>> getAvailableDrones() {
        return new ResponseEntity<>(droneService.getAvailableDrones(), HttpStatus.OK);
    }

    @GetMapping("/{id}/battery")
    @ApiOperation(value = "Check drone battery level", response = Integer.class)
    public ResponseEntity<Integer> checkDroneBatteryLevel(@PathVariable Long id) {
        return new ResponseEntity<>(droneService.checkDroneBatteryLevel(id), HttpStatus.OK);
    }
}
