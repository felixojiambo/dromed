package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .build();
        circuitBreaker = CircuitBreaker.of("droneService", circuitBreakerConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();
        retry = Retry.of("droneService", retryConfig);
    }

    @Test
    void testRegisterDrone() {
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Callable<DroneDTO> registerDroneCallable = CircuitBreaker.decorateCallable(circuitBreaker,
                () -> Retry.decorateCallable(retry,
                        () -> droneService.registerDrone(drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit(),
                                drone.getBatteryCapacity(), drone.getState())).call()
        );

        DroneDTO result = Try.ofCallable(registerDroneCallable).getOrElse(new DroneDTO(0L, "defaultSerial", Model.LIGHTWEIGHT, 400, 50, State.IDLE));

        assertNotNull(result);
        assertEquals(drone.getSerialNumber(), result.getSerialNumber());
    }

    @Test
    void testLoadDroneWithMedication() {
        when(droneRepository.findAll()).thenReturn(List.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(droneMedication);

        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        Callable<DroneMedicationDTO> loadDroneCallable = CircuitBreaker.decorateCallable(circuitBreaker,
                () -> Retry.decorateCallable(retry,
                        () -> droneService.loadDroneWithMedication(medicationDTO)).call()
        );

        DroneMedicationDTO result;
        result = Try.ofCallable(loadDroneCallable).getOrElse(new DroneMedicationDTO(new DroneDTO(), new MedicationDTO()));

        assertNotNull(result);
        assertNotNull(result.getMedication());
        assertEquals(medication.getName(), result.getMedication().getName());
    }

    @Test
    void testGetMedicationsByDrone() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        when(droneMedicationRepository.findByDrone(drone)).thenReturn(List.of(droneMedication));

        List<MedicationDTO> result = droneService.getMedicationsByDrone(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(medication.getName(), result.getFirst().getName());
    }

    @Test
    void testGetAvailableDrones() {
        List<Drone> drones = List.of(drone);

        when(droneRepository.findByState(State.IDLE)).thenReturn(drones);

        List<DroneDTO> result = droneService.getAvailableDrones();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(drone.getSerialNumber(), result.getFirst().getSerialNumber());
    }

    @Test
    void testCheckDroneBatteryLevel() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        int result = droneService.checkDroneBatteryLevel(1L);

        assertEquals(drone.getBatteryCapacity(), result);
    }

    @Test
    void testStartDelivery() {
        drone.setState(State.LOADED);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.startDelivery(1L);

        assertEquals(State.DELIVERING, drone.getState());
    }

    @Test
    void testCompleteDelivery() {
        drone.setState(State.DELIVERING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.completeDelivery(1L);

        assertEquals(State.DELIVERED, drone.getState());
    }

    @Test
    void testReturnToBase() {
        drone.setState(State.DELIVERED);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.returnToBase(1L);

        assertEquals(State.IDLE, drone.getState());
    }

    @Test
    void testMarkIdle() {
        drone.setState(State.RETURNING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        ResponseEntity<Object> result = droneService.markIdle(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(State.IDLE, drone.getState());
    }
}
