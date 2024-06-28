package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DroneServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneMedicationRepository droneMedicationRepository;

    @InjectMocks
    private DroneServiceImpl droneService;

    private Drone drone;
    private Medication medication;

    @BeforeEach
    public void setup() {
        drone = new Drone(1L, "SN12345", Model.CRUISERWEIGHT, 400, 80, State.IDLE);
        medication = new Medication(1L, "Med1", 100, "MED123", null);
    }

    @Test
    public void testRegisterDroneSuccess() {
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Drone result = droneService.registerDrone("SN12345", Model.CRUISERWEIGHT, 400, 80, State.IDLE);

        assertNotNull(result);
        assertEquals("SN12345", result.getSerialNumber());
        verify(droneRepository, times(1)).save(any(Drone.class));
    }

    @Test
    public void testRegisterDroneWeightLimitExceeded() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            droneService.registerDrone("SN12345", Model.CRUISERWEIGHT, 600, 80, State.IDLE);
        });

        assertEquals("Weight limit cannot exceed 500 grams", exception.getMessage());
    }

    @Test
    public void testLoadDroneWithMedicationSuccess() {
        when(droneRepository.findById(anyLong())).thenReturn(Optional.of(drone));
        when(droneMedicationRepository.findByDroneId(anyLong())).thenReturn(Arrays.asList());

        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);

        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(droneMedication);

        DroneMedication result = droneService.loadDroneWithMedication(1L, medication);

        assertNotNull(result);
        assertEquals(drone, result.getDrone());
        assertEquals(medication, result.getMedication());
        verify(droneMedicationRepository, times(1)).save(any(DroneMedication.class));
    }

    @Test
    public void testLoadDroneWithMedicationDroneNotFound() {
        when(droneRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            droneService.loadDroneWithMedication(1L, medication);
        });

        assertEquals("Drone not found", exception.getMessage());
    }

    @Test
    public void testLoadDroneWithMedicationBatteryLow() {
        drone.setBatteryCapacity(20);
        when(droneRepository.findById(anyLong())).thenReturn(Optional.of(drone));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            droneService.loadDroneWithMedication(1L, medication);
        });

        assertEquals("Battery level is below 25%", exception.getMessage());
    }

    @Test
    public void testGetMedicationsByDroneSuccess() {
        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);

        when(droneMedicationRepository.findByDroneId(anyLong())).thenReturn(Arrays.asList(droneMedication));

        List<Medication> medications = droneService.getMedicationsByDrone(1L);

        assertNotNull(medications);
        assertEquals(1, medications.size());
        assertEquals("Med1", medications.get(0).getName());
    }

    @Test
    public void testGetAvailableDrones() {
        when(droneRepository.findByState(State.IDLE)).thenReturn(Arrays.asList(drone));

        List<Drone> availableDrones = droneService.getAvailableDrones();

        assertNotNull(availableDrones);
        assertEquals(1, availableDrones.size());
        assertEquals("SN12345", availableDrones.get(0).getSerialNumber());
    }

    @Test
    public void testCheckDroneBatteryLevelSuccess() {
        when(droneRepository.findById(anyLong())).thenReturn(Optional.of(drone));

        int batteryLevel = droneService.checkDroneBatteryLevel(1L);

        assertEquals(80, batteryLevel);
    }

    @Test
    public void testCheckDroneBatteryLevelDroneNotFound() {
        when(droneRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            droneService.checkDroneBatteryLevel(1L);
        });

        assertEquals("Drone not found", exception.getMessage());
    }

    @Test
    public void testGetTotalLoadedWeight() {
        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);

        when(droneMedicationRepository.findByDroneId(anyLong())).thenReturn(Arrays.asList(droneMedication));

        int totalWeight = droneService.getTotalLoadedWeight(1L);

        assertEquals(100, totalWeight);
    }
}
