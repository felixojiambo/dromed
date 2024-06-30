package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.patterns.DroneFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for DroneServiceImpl.
 * Uses Mockito for mocking dependencies and JUnit for testing.
 */
@ExtendWith(MockitoExtension.class)
class DroneServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneMedicationRepository droneMedicationRepository;

    @InjectMocks
    private DroneServiceImpl droneService;

    private Drone drone;
    private Medication medication;
    private DroneMedication droneMedication;
    //private static final int MAX_DRONE_COUNT = 10;
    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        drone = new Drone();
        drone.setId(1L);
        drone.setSerialNumber("12345");
        drone.setModel(Model.LIGHTWEIGHT);
        drone.setWeightLimit(400);
        drone.setBatteryCapacity(50);
        drone.setState(State.IDLE);

        medication = new Medication();
        medication.setId(1L);
        medication.setName("Med1");
        medication.setWeight(100);
        medication.setCode("MED123");
        medication.setImageUrl("http://example.com/image.jpg");

        droneMedication = new DroneMedication(drone, medication);
    }

    /**
     * Tests the registerDrone method.
     */
    @Test
    void testRegisterDrone() {
        // Mock the save method of droneRepository
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Mock the count method to return less than the maximum limit
       // when(droneRepository.count()).thenReturn(5L);

        // Call the service method
        DroneDTO droneDTO = droneService.registerDrone(drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit(), drone.getBatteryCapacity(), drone.getState());

        // Verify the results
        assertEquals(drone.getSerialNumber(), droneDTO.getSerialNumber());
        assertEquals(drone.getModel(), droneDTO.getModel());
        assertEquals(drone.getWeightLimit(), droneDTO.getWeightLimit());
        assertEquals(drone.getBatteryCapacity(), droneDTO.getBatteryCapacity());
        assertEquals(drone.getState(), droneDTO.getState());
    }

//    @Test
//    void testRegisterDroneThrowsIllegalStateException() {
//        // Mock the count method to return the maximum limit
//        when(droneRepository.count()).thenReturn(10L);
//
//        // Verify that the exception is thrown
//        assertThrows(IllegalStateException.class, () -> droneService.registerDrone(drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit(), drone.getBatteryCapacity(), drone.getState()));
//    }

    /**
     * Tests the loadDroneWithMedication method.
     */
    @Test
    void testLoadDroneWithMedication() {
        // Mock the necessary repository methods
        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(droneMedication);
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Prepare the medication DTO
        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        // Call the service method
        DroneMedicationDTO result = droneService.loadDroneWithMedication(medicationDTO);

        // Verify the results
        assertNotNull(result);
        assertEquals(medicationDTO.getId(), result.getMedication().getId());
        assertEquals(drone.getId(), result.getDrone().getId());
    }

    /**
     * Tests that loadDroneWithMedication throws DroneNotAvailableException when no drone is available.
     */
    @Test
    void testLoadDroneWithMedicationThrowsDroneNotAvailableException() {
        // Mock the repository method to return an empty list
        when(droneRepository.findByState(State.IDLE)).thenReturn(new ArrayList<>());

        // Prepare the medication DTO
        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        // Verify that the exception is thrown
        assertThrows(DroneNotAvailableException.class, () -> droneService.loadDroneWithMedication(medicationDTO));
    }

    /**
     * Tests that loadDroneWithMedication throws OverweightException when the medication exceeds the drone's weight limit.
     */
    @Test
    void testLoadDroneWithMedicationThrowsOverweightException() {
        // Set the drone's weight limit to a value less than the medication weight
        drone.setWeightLimit(50);

        // Mock the repository method to return the drone
        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));

        // Prepare the medication DTO
        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        // Verify that the exception is thrown
        assertThrows(OverweightException.class, () -> droneService.loadDroneWithMedication(medicationDTO));
    }

    /**
     * Tests the getMedicationsByDrone method.
     */
    @Test
    void testGetMedicationsByDrone() {
        // Mock the repository method to return the drone medication
        when(droneMedicationRepository.findByDroneId(drone.getId())).thenReturn(List.of(droneMedication));

        // Call the service method
        List<MedicationDTO> result = droneService.getMedicationsByDrone(drone.getId());

        // Verify the results
        assertEquals(1, result.size());
        assertEquals(medication.getId(), result.get(0).getId());
    }

    /**
     * Tests the getAvailableDrones method.
     */
    @Test
    void testGetAvailableDrones() {
        // Mock the repository method to return the drone
        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));

        // Call the service method
        List<DroneDTO> result = droneService.getAvailableDrones();

        // Verify the results
        assertEquals(1, result.size());
        assertEquals(drone.getId(), result.get(0).getId());
    }

    /**
     * Tests the checkDroneBatteryLevel method.
     */
    @Test
    void testCheckDroneBatteryLevel() {
        // Mock the repository method to return the drone
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        // Call the service method
        int batteryLevel = droneService.checkDroneBatteryLevel(drone.getId());

        // Verify the results
        assertEquals(drone.getBatteryCapacity(), batteryLevel);
    }

    /**
     * Tests that checkDroneBatteryLevel throws ResourceNotFoundException when the drone is not found.
     */
    @Test
    void testCheckDroneBatteryLevelThrowsResourceNotFoundException() {
        // Mock the repository method to return an empty optional
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.empty());

        // Verify that the exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> droneService.checkDroneBatteryLevel(drone.getId()));
    }

    /**
     * Tests the startDelivery method.
     */
    @Test
    void testStartDelivery() {
        // Set the drone's state to LOADED
        drone.setState(State.LOADED);

        // Mock the repository methods
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Call the service method
        droneService.startDelivery(drone.getId());

        // Verify the state change
        assertEquals(State.DELIVERING, drone.getState());
    }

    /**
     * Tests that startDelivery throws IllegalStateException when the drone is not in a valid state.
     */
    @Test
    void testStartDeliveryThrowsIllegalStateException() {
        // Set the drone's state to IDLE
        drone.setState(State.IDLE);

        // Mock the repository method
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        // Verify that the exception is thrown
        assertThrows(IllegalStateException.class, () -> droneService.startDelivery(drone.getId()));
    }

    /**
     * Tests the completeDelivery method.
     */
    @Test
    void testCompleteDelivery() {
        // Set the drone's state to DELIVERING
        drone.setState(State.DELIVERING);

        // Mock the repository methods
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Call the service method
        droneService.completeDelivery(drone.getId());

        // Verify the state change
        assertEquals(State.DELIVERED, drone.getState());
    }

    /**
     * Tests that completeDelivery throws IllegalStateException when the drone is not in a valid state.
     */
    @Test
    void testCompleteDeliveryThrowsIllegalStateException() {
        // Set the drone's state to IDLE
        drone.setState(State.IDLE);

        // Mock the repository method
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        // Verify that the exception is thrown
        assertThrows(IllegalStateException.class, () -> droneService.completeDelivery(drone.getId()));
    }

    /**
     * Tests the returnToBase method.
     */
    @Test
    void testReturnToBase() {
        // Set the drone's state to DELIVERED
        drone.setState(State.DELIVERED);

        // Mock the repository methods
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Call the service method
        droneService.returnToBase(drone.getId());

        // Verify the state change
        assertEquals(State.IDLE, drone.getState());
    }

    /**
     * Tests that returnToBase throws IllegalStateException when the drone is not in a valid state.
     */
    @Test
    void testReturnToBaseThrowsIllegalStateException() {
        // Set the drone's state to DELIVERING
        drone.setState(State.DELIVERING);

        // Mock the repository method
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        // Verify that the exception is thrown
        assertThrows(IllegalStateException.class, () -> droneService.returnToBase(drone.getId()));
    }

    /**
     * Tests the markIdle method.
     */
    @Test
    void testMarkIdle() {
        // Set the drone's state to RETURNING
        drone.setState(State.RETURNING);

        // Mock the repository methods
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        // Call the service method
        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        // Verify the response and state change
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(State.IDLE, drone.getState());
    }

    /**
     * Tests that markIdle returns CONFLICT status when the drone is not in a valid state.
     */
    @Test
    void testMarkIdleReturnsConflict() {
        // Set the drone's state to DELIVERING
        drone.setState(State.DELIVERING);

        // Mock the repository method
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        // Call the service method
        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    /**
     * Tests that markIdle returns NOT_FOUND status when the drone is not found.
     */
    @Test
    void testMarkIdleReturnsNotFound() {
        // Mock the repository method to return an empty optional
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.empty());

        // Call the service method
        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
