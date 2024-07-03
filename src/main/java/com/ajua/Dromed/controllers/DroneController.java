package com.ajua.Dromed.controllers;

import com.ajua.Dromed.dtos.*;
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

    @PostMapping
    @Operation(
            summary = "Register a new drone",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Drone registered successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponseWithDrone.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<ApiResponseWithDrone> registerDrone(
            @RequestBody DroneDTO droneRegisterDTO) {
        DroneDTO droneDTO = droneService.registerDrone(droneRegisterDTO.getSerialNumber(), droneRegisterDTO.getModel(),
                droneRegisterDTO.getWeightLimit(), droneRegisterDTO.getBatteryCapacity(), droneRegisterDTO.getState());
        ApiResponseWithDrone response = new ApiResponseWithDrone(true, "Drone registered successfully", droneDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/state")
    @Operation(
            summary = "Update drone state",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Drone state updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "409", description = "Invalid drone state transition")
            }
    )
    public ResponseEntity<ApiResponseSuccess> updateDroneState(@PathVariable Long id, @RequestBody DroneDTO droneStateDTO) {
        droneService.updateDroneState(id, droneStateDTO.getState());
        ApiResponseSuccess response = new ApiResponseSuccess(true, "Drone state updated successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/medications")
    @Operation(
            summary = "Load a drone with medication",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Medication loaded successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponseWithDroneMedication.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or weight limit exceeded")
            }
    )
    public ResponseEntity<ApiResponseWithDroneMedication> loadDroneWithMedication(@PathVariable Long id, @RequestBody MedicationDTO medicationDTO) {
        DroneMedicationDTO droneMedicationDTO = droneService.loadDroneWithMedication(id, medicationDTO);
        ApiResponseWithDroneMedication response = new ApiResponseWithDroneMedication(true, "Medication loaded successfully", droneMedicationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get available drones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available drones",
                            content = @Content(schema = @Schema(implementation = ApiResponseWithDrones.class))),
            }
    )
    public ResponseEntity<ApiResponseWithDrones> getAvailableDrones(@RequestParam(required = false) State state) {
        List<DroneDTO> availableDrones = droneService.getAvailableDrones(state);
        ApiResponseWithDrones response = new ApiResponseWithDrones(true, "List of available drones", availableDrones);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{droneId}/battery-level")
    @Operation(
            summary = "Check drone battery level",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Battery level retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponseWithInteger.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<ApiResponseWithInteger> checkDroneBatteryLevel(@PathVariable Long droneId) {
        int batteryLevel = droneService.checkDroneBatteryLevel(droneId);
        ApiResponseWithInteger response = new ApiResponseWithInteger(true, "Battery level retrieved successfully", batteryLevel);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{droneId}/medications")
    @Operation(
            summary = "Get medications loaded on a drone",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of medications retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponseWithMedications.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found")
            }
    )
    public ResponseEntity<ApiResponseWithMedications> getMedicationsByDrone(@PathVariable Long droneId) {
        List<MedicationDTO> medications = droneService.getMedicationsByDrone(droneId);
        ApiResponseWithMedications response = new ApiResponseWithMedications(true, "List of medications retrieved successfully", medications);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{droneId}/deliveries")
    @Operation(
            summary = "Start or complete delivery for a drone",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Delivery process started",
                            content = @Content(schema = @Schema(implementation = ApiResponseSuccess.class))),
                    @ApiResponse(responseCode = "200", description = "Delivery completed successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponseSuccess.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid delivery state transition")
            }
    )
    public ResponseEntity<ApiResponseSuccess> handleDelivery(@PathVariable Long droneId, @RequestParam String action) {
        if ("start".equals(action)) {
            droneService.startDelivery(droneId);
            ApiResponseSuccess response = new ApiResponseSuccess(true, "Delivery process started");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } else if ("complete".equals(action)) {
            droneService.completeDelivery(droneId);
            ApiResponseSuccess response = new ApiResponseSuccess(true, "Delivery completed successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ApiResponseSuccess response = new ApiResponseSuccess(false, "Invalid action");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{droneId}/return-to-base")
    @Operation(
            summary = "Return a drone to base",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Drone return process started",
                            content = @Content(schema = @Schema(implementation = ApiResponseSuccess.class))),
                    @ApiResponse(responseCode = "404", description = "Drone not found"),
                    @ApiResponse(responseCode = "400", description = "Drone is not in a state to return")
            }
    )
    public ResponseEntity<ApiResponseSuccess> returnToBase(@PathVariable Long droneId) {
        droneService.returnToBase(droneId);
        ApiResponseSuccess response = new ApiResponseSuccess(true, "Drone return process started");
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
