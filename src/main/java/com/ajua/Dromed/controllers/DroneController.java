package com.ajua.Dromed.controllers;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.services.impl.DroneServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@Tag(name = "Drones", description = "Drone Dispatch System for Medication Transport")
public class DroneController {

    @Autowired
    private DroneServiceImpl droneService;

    @PostMapping
    @Operation(
            summary = "Register a new drone",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Drone registered successfully",
                            content = @Content(schema = @Schema(implementation = Drone.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<Drone> registerDrone(
            @RequestParam String serialNumber,
            @RequestParam Model model,
            @RequestParam int weightLimit,
            @RequestParam int batteryCapacity,
            @RequestParam State state) {
        Drone registeredDrone = droneService.registerDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return new ResponseEntity<>(registeredDrone, HttpStatus.CREATED);
    }

    @PostMapping("/load")
    @Operation(
            summary = "Load a drone with medication",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Medication loaded successfully",
                            content = @Content(schema = @Schema(implementation = DroneMedication.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or weight limit exceeded")
            }
    )
    public ResponseEntity<DroneMedication> loadDroneWithMedication(
            @RequestBody Medication medication) {
        DroneMedication droneMedication = droneService.loadDroneWithMedication(medication);
        return new ResponseEntity<>(droneMedication, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @Operation(
            summary = "Get available drones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available drones",
                            content = @Content(schema = @Schema(implementation = Drone.class))),
            }
    )
    public ResponseEntity<List<Drone>> getAvailableDrones() {
        return new ResponseEntity<>(droneService.getAvailableDrones(), HttpStatus.OK);
    }

    @GetMapping("/{id}/battery")
    @Operation(
            summary = "Check drone battery level",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Battery level retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<Integer> checkDroneBatteryLevel(@PathVariable Long id) {
        return new ResponseEntity<>(droneService.checkDroneBatteryLevel(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/medications")
    @Operation(
            summary = "Get medications loaded on a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of medications retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Medication.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<List<Medication>> getMedicationsByDrone(@PathVariable Long id) {
        return new ResponseEntity<>(droneService.getMedicationsByDrone(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/startDelivery")
    @Operation(
            summary = "Start delivery with a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery started successfully"),
                    @ApiResponse(responseCode = "400", description = "Drone is not ready for delivery"),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<Void> startDelivery(@PathVariable Long id) {
        droneService.startDelivery(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/completeDelivery")
    @Operation(
            summary = "Complete delivery with a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery completed successfully"),
                    @ApiResponse(responseCode = "400", description = "Drone is not delivering"),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<Void> completeDelivery(@PathVariable Long id) {
        droneService.completeDelivery(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/returnToBase")
    @Operation(
            summary = "Return drone to base",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Drone returned to base successfully"),
                    @ApiResponse(responseCode = "400", description = "Drone is not in a state to return"),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<Void> returnToBase(@PathVariable Long id) {
        droneService.returnToBase(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
