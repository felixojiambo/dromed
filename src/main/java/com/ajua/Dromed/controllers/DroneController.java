package com.ajua.Dromed.controllers;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.services.interfaces.DroneService;
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
@RequestMapping("/api/v1/drones")
@Tag(name = "Drones", description = "Drone Dispatch System for Medication Transport")
public class DroneController {

    @Autowired
    private DroneService droneService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new drone",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Drone registered successfully",
                            content = @Content(schema = @Schema(implementation = DroneDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<DroneDTO> registerDrone(
            @RequestParam String serialNumber,
            @RequestParam Model model,
            @RequestParam int weightLimit,
            @RequestParam int batteryCapacity,
            @RequestParam State state) {
        DroneDTO droneDTO = droneService.registerDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return new ResponseEntity<>(droneDTO, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/mark-idle")
    @Operation(
            summary = "Mark a drone as idle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Drone marked as idle successfully"),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "409", description = "Drone is not in the returning state")
            }
    )
    public ResponseEntity<Void> markIdle(@PathVariable Long id) {
        return droneService.markIdle(id);
    }
    @PostMapping("/load-medication")
    @Operation(
            summary = "Load a drone with medication",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Medication loaded successfully",
                            content = @Content(schema = @Schema(implementation = DroneMedicationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or weight limit exceeded")
            }
    )
    public ResponseEntity<DroneMedicationDTO> loadDroneWithMedication(
            @RequestBody MedicationDTO medicationDTO) {
        DroneMedicationDTO droneMedicationDTO = droneService.loadDroneWithMedication(medicationDTO);
        return new ResponseEntity<>(droneMedicationDTO, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @Operation(
            summary = "Get available drones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available drones",
                            content = @Content(schema = @Schema(implementation = DroneDTO.class))),
            }
    )
    public ResponseEntity<List<DroneDTO>> getAvailableDrones() {
        List<DroneDTO> availableDrones = droneService.getAvailableDrones();
        return new ResponseEntity<>(availableDrones, HttpStatus.OK);
    }

    @GetMapping("/{droneId}/battery-level")
    @Operation(
            summary = "Check drone battery level",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Battery level retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<Integer> checkDroneBatteryLevel(@PathVariable Long droneId) {
        int batteryLevel = droneService.checkDroneBatteryLevel(droneId);
        return new ResponseEntity<>(batteryLevel, HttpStatus.OK);
    }

    @GetMapping("/{droneId}/medications")
    @Operation(
            summary = "Get medications loaded on a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of medications retrieved successfully",
                            content = @Content(schema = @Schema(implementation = MedicationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<List<MedicationDTO>> getMedicationsByDrone(@PathVariable Long droneId) {
        List<MedicationDTO> medications = droneService.getMedicationsByDrone(droneId);
        return new ResponseEntity<>(medications, HttpStatus.OK);
    }

    @PostMapping("/{droneId}/start-delivery")
    @Operation(
            summary = "Start delivery for a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery started successfully"),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "400", description = "Drone is not ready for delivery")
            }
    )
    public ResponseEntity<Void> startDelivery(@PathVariable Long droneId) {
        droneService.startDelivery(droneId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{droneId}/complete-delivery")
    @Operation(
            summary = "Complete delivery for a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Delivery completed successfully"),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "400", description = "Drone is not delivering")
            }
    )
    public ResponseEntity<Void> completeDelivery(@PathVariable Long droneId) {
        droneService.completeDelivery(droneId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{droneId}/return-to-base")
    @Operation(
            summary = "Return a drone to base",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Drone returned to base successfully"),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "400", description = "Drone is not in a state to return")
            }
    )
    public ResponseEntity<Void> returnToBase(@PathVariable Long droneId) {
        droneService.returnToBase(droneId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
