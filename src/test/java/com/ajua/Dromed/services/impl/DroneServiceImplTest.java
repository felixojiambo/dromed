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

    @Test
    void testRegisterDrone() {
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        DroneDTO droneDTO = droneService.registerDrone(drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit(), drone.getBatteryCapacity(), drone.getState());

        assertEquals(drone.getSerialNumber(), droneDTO.getSerialNumber());
        assertEquals(drone.getModel(), droneDTO.getModel());
        assertEquals(drone.getWeightLimit(), droneDTO.getWeightLimit());
        assertEquals(drone.getBatteryCapacity(), droneDTO.getBatteryCapacity());
        assertEquals(drone.getState(), droneDTO.getState());
    }

    @Test
    void testLoadDroneWithMedication() {
        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(droneMedication);
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        DroneMedicationDTO result = droneService.loadDroneWithMedication(medicationDTO);

        assertNotNull(result);
        assertEquals(medicationDTO.getId(), result.getMedication().getId());
        assertEquals(drone.getId(), result.getDrone().getId());
    }

    @Test
    void testLoadDroneWithMedicationThrowsDroneNotAvailableException() {
        when(droneRepository.findByState(State.IDLE)).thenReturn(new ArrayList<>());

        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        assertThrows(DroneNotAvailableException.class, () -> droneService.loadDroneWithMedication(medicationDTO));
    }

    @Test
    void testLoadDroneWithMedicationThrowsOverweightException() {
        drone.setWeightLimit(50);

        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));

        MedicationDTO medicationDTO = new MedicationDTO(medication.getId(), medication.getName(), medication.getWeight(), medication.getCode(), medication.getImageUrl());

        assertThrows(OverweightException.class, () -> droneService.loadDroneWithMedication(medicationDTO));
    }

    @Test
    void testGetMedicationsByDrone() {
        when(droneMedicationRepository.findByDroneId(drone.getId())).thenReturn(List.of(droneMedication));

        List<MedicationDTO> result = droneService.getMedicationsByDrone(drone.getId());

        assertEquals(1, result.size());
        assertEquals(medication.getId(), result.get(0).getId());
    }

    @Test
    void testGetAvailableDrones() {
        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));

        List<DroneDTO> result = droneService.getAvailableDrones();

        assertEquals(1, result.size());
        assertEquals(drone.getId(), result.get(0).getId());
    }

    @Test
    void testCheckDroneBatteryLevel() {
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        int batteryLevel = droneService.checkDroneBatteryLevel(drone.getId());

        assertEquals(drone.getBatteryCapacity(), batteryLevel);
    }

    @Test
    void testCheckDroneBatteryLevelThrowsResourceNotFoundException() {
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> droneService.checkDroneBatteryLevel(drone.getId()));
    }

    @Test
    void testStartDelivery() {
        drone.setState(State.LOADED);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.startDelivery(drone.getId());

        assertEquals(State.DELIVERING, drone.getState());
    }

    @Test
    void testStartDeliveryThrowsIllegalStateException() {
        drone.setState(State.IDLE);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        assertThrows(IllegalStateException.class, () -> droneService.startDelivery(drone.getId()));
    }

    @Test
    void testCompleteDelivery() {
        drone.setState(State.DELIVERING);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.completeDelivery(drone.getId());

        assertEquals(State.DELIVERED, drone.getState());
    }

    @Test
    void testCompleteDeliveryThrowsIllegalStateException() {
        drone.setState(State.IDLE);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        assertThrows(IllegalStateException.class, () -> droneService.completeDelivery(drone.getId()));
    }

    @Test
    void testReturnToBase() {
        drone.setState(State.DELIVERED);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.returnToBase(drone.getId());

        assertEquals(State.IDLE, drone.getState());
    }

    @Test
    void testReturnToBaseThrowsIllegalStateException() {
        drone.setState(State.DELIVERING);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        assertThrows(IllegalStateException.class, () -> droneService.returnToBase(drone.getId()));
    }

    @Test
    void testMarkIdle() {
        drone.setState(State.RETURNING);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(State.IDLE, drone.getState());
    }

    @Test
    void testMarkIdleReturnsConflict() {
        drone.setState(State.DELIVERING);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testMarkIdleReturnsNotFound() {
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.empty());

        ResponseEntity<Object> response = droneService.markIdle(drone.getId());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
