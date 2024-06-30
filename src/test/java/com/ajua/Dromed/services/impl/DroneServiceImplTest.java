package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.*;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
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

    private CircuitBreaker circuitBreaker;
    private Retry retry;

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

        // Configure CircuitBreaker
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(5)
                .build();
        circuitBreaker = CircuitBreaker.of("testCircuitBreaker", circuitBreakerConfig);

        // Configure Retry
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();
        retry = Retry.of("testRetry", retryConfig);
    }

    /**
     * Tests the registerDrone method.
     */
    @Test
    void testRegisterDrone() {
        // Mock the save method of droneRepository with CircuitBreaker
        when(droneRepository.save(any(Drone.class)))
                .thenReturn(drone)
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(drone)
                .thenReturn(drone)
                .thenReturn(drone);

        // Use the Retry mechanism with CircuitBreaker for registerDrone method
        Callable<DroneDTO> callable = CircuitBreaker.decorateCallable(circuitBreaker, () -> retry.executeCallable(() ->
                droneService.registerDrone(drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit(), drone.getBatteryCapacity(), drone.getState())));

        // Call the service method
        DroneDTO droneDTO = Try.ofCallable(callable)
                .recover(ResourceNotFoundException.class, ex -> { throw new ResourceNotFoundException("Resource not found"); })
                .recover(RuntimeException.class, ex -> { throw new DroneServiceException("Service unavailable"); })
                .get();

        // Verify the results
        assertEquals(drone.getSerialNumber(), droneDTO.getSerialNumber());
        assertEquals(drone.getModel(), droneDTO.getModel());
        assertEquals(drone.getWeightLimit(), droneDTO.getWeightLimit());
        assertEquals(drone.getBatteryCapacity(), droneDTO.getBatteryCapacity());
        assertEquals(drone.getState(), droneDTO.getState());
    }

    /**
     * Tests the loadDroneWithMedication method.
     */
    @Test
    void testLoadDroneWithMedication() {
        // Mock the necessary repository methods with CircuitBreaker
        when(droneRepository.findByState(State.IDLE))
                .thenReturn(List.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(List.of(drone));

        // Mock the save method of droneMedicationRepository with Retry
        when(droneMedicationRepository.save(any(DroneMedication.class)))
                .thenReturn(droneMedication)
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(droneMedication);

        // Use the Retry mechanism with CircuitBreaker for loadDroneWithMedication method
        Callable<DroneMedicationDTO> callable = CircuitBreaker.decorateCallable(circuitBreaker, () -> retry.executeCallable(() ->
                droneService.loadDroneWithMedication(new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl()))));

        // Call the service method
        DroneMedicationDTO result = Try.ofCallable(callable)
                .recover(ResourceNotFoundException.class, ex -> { throw new ResourceNotFoundException("Resource not found"); })
                .recover(RuntimeException.class, ex -> { throw new DroneServiceException("Service unavailable"); })
                .get();

        // Verify the results
        assertNotNull(result);
        assertEquals(medication.getId(), result.getMedication().getId());
        assertEquals(drone.getId(), result.getDrone().getId());
    }

    /**
     * Tests that loadDroneWithMedication throws DroneNotAvailableException when no drone is available.
     */
    @Test
    void testLoadDroneWithMedicationThrowsDroneNotAvailableException() {
        // Mock the repository method to return an empty list with CircuitBreaker
        when(droneRepository.findByState(State.IDLE))
                .thenReturn(new ArrayList<>())
                .thenThrow(new RuntimeException("Database unavailable"));

        // Use the CircuitBreaker for loadDroneWithMedication method directly
        assertThrows(DroneNotAvailableException.class, () ->
                CircuitBreaker.decorateCallable(circuitBreaker, () ->
                        droneService.loadDroneWithMedication(new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl()))).call());
    }

    /**
     * Tests that loadDroneWithMedication throws OverweightException when the medication exceeds the drone's weight limit.
     */
    @Test
    void testLoadDroneWithMedicationThrowsOverweightException() {
        // Set the drone's weight limit to a value less than the medication weight
        drone.setWeightLimit(50);

        // Mock the repository method to return the drone with CircuitBreaker
        when(droneRepository.findByState(State.IDLE))
                .thenReturn(List.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(List.of(drone));

        // Use the CircuitBreaker for loadDroneWithMedication method directly
        assertThrows(OverweightException.class, () ->
                CircuitBreaker.decorateCallable(circuitBreaker, () ->
                        droneService.loadDroneWithMedication(new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl()))).call());
    }

    /**
     * Tests the getMedicationsByDrone method.
     */
    @Test
    void testGetMedicationsByDrone() throws Exception {
        // Mock the repository method to return the drone medication with CircuitBreaker
        when(droneMedicationRepository.findByDroneId(drone.getId()))
                .thenReturn(List.of(droneMedication))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(List.of(droneMedication));

        // Use the CircuitBreaker for getMedicationsByDrone method directly
        List<MedicationDTO> result = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.getMedicationsByDrone(drone.getId())).call();

        // Verify the results
        assertEquals(1, result.size());
        assertEquals(medication.getId(), result.getFirst().getId());
    }

    /**
     * Tests the getAvailableDrones method.
     */
    @Test
    void testGetAvailableDrones() throws Exception {
        // Mock the repository method to return the drone with CircuitBreaker
        when(droneRepository.findByState(State.IDLE))
                .thenReturn(List.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(List.of(drone));

        // Use the CircuitBreaker for getAvailableDrones method directly
        List<DroneDTO> result = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.getAvailableDrones()).call();

        // Verify the results
        assertEquals(1, result.size());
        assertEquals(drone.getId(), result.getFirst().getId());
    }

    /**
     * Tests the checkDroneBatteryLevel method.
     */
    @Test
    void testCheckDroneBatteryLevel() throws Exception {
        // Mock the repository method to return the drone with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for checkDroneBatteryLevel method directly
        int batteryLevel = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.checkDroneBatteryLevel(drone.getId())).call();

        // Verify the results
        assertEquals(drone.getBatteryCapacity(), batteryLevel);
    }

    /**
     * Tests that checkDroneBatteryLevel throws ResourceNotFoundException when the drone is not found.
     */
    @Test
    void testCheckDroneBatteryLevelThrowsResourceNotFoundException() {
        // Mock the repository method to return an empty optional with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.empty())
                .thenThrow(new RuntimeException("Database unavailable"));

        // Use the CircuitBreaker for checkDroneBatteryLevel method directly
        assertThrows(ResourceNotFoundException.class, () ->
                CircuitBreaker.decorateCallable(circuitBreaker, () ->
                        droneService.checkDroneBatteryLevel(drone.getId())).call());
    }

    /**
     * Tests the startDelivery method.
     */
    @Test
    void testStartDelivery() {
        // Set the drone's state to LOADED
        drone.setState(State.LOADED);

        // Mock the repository methods with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for startDelivery method directly
        CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                droneService.startDelivery(drone.getId())).run();

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

        // Mock the repository method with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for startDelivery method directly
        assertThrows(IllegalStateException.class, () ->
                CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                        droneService.startDelivery(drone.getId())).run());
    }

    /**
     * Tests the completeDelivery method.
     */
    @Test
    void testCompleteDelivery() {
        // Set the drone's state to DELIVERING
        drone.setState(State.DELIVERING);

        // Mock the repository methods with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for completeDelivery method directly
        CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                droneService.completeDelivery(drone.getId())).run();

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

        // Mock the repository method with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for completeDelivery method directly
        assertThrows(IllegalStateException.class, () ->
                CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                        droneService.completeDelivery(drone.getId())).run());
    }

    /**
     * Tests the returnToBase method.
     */
    @Test
    void testReturnToBase() {
        // Set the drone's state to DELIVERED
        drone.setState(State.DELIVERED);

        // Mock the repository methods with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for returnToBase method directly
        CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                droneService.returnToBase(drone.getId())).run();

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

        // Mock the repository method with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for returnToBase method directly
        assertThrows(IllegalStateException.class, () ->
                CircuitBreaker.decorateRunnable(circuitBreaker, () ->
                        droneService.returnToBase(drone.getId())).run());
    }

    /**
     * Tests the markIdle method.
     */
    @Test
    void testMarkIdle() throws Exception {
        // Set the drone's state to RETURNING
        drone.setState(State.RETURNING);

        // Mock the repository methods with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for markIdle method directly
        ResponseEntity<Object> response = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.markIdle(drone.getId())).call();

        // Verify the response and state change
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(State.IDLE, drone.getState());
    }

    /**
     * Tests that markIdle returns CONFLICT status when the drone is not in a valid state.
     */
    @Test
    void testMarkIdleReturnsConflict() throws Exception {
        // Set the drone's state to DELIVERING
        drone.setState(State.DELIVERING);

        // Mock the repository method with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.of(drone))
                .thenThrow(new RuntimeException("Database unavailable"))
                .thenReturn(Optional.of(drone));

        // Use the CircuitBreaker for markIdle method directly
        ResponseEntity<Object> response = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.markIdle(drone.getId())).call();

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    /**
     * Tests that markIdle returns NOT_FOUND status when the drone is not found.
     */
    @Test
    void testMarkIdleReturnsNotFound() throws Exception {
        // Mock the repository method to return an empty optional with CircuitBreaker
        when(droneRepository.findById(drone.getId()))
                .thenReturn(Optional.empty())
                .thenThrow(new RuntimeException("Database unavailable"));

        // Use the CircuitBreaker for markIdle method directly
        ResponseEntity<Object> response = CircuitBreaker.decorateCallable(circuitBreaker, () ->
                droneService.markIdle(drone.getId())).call();

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
