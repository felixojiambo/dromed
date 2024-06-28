package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DroneServiceImplTest {

    private DroneServiceImpl droneService;
    private DroneRepository droneRepository;
    private DroneMedicationRepository droneMedicationRepository;

    @BeforeEach
    void setUp() {
        droneRepository = mock(DroneRepository.class);
        droneMedicationRepository = mock(DroneMedicationRepository.class);
        droneService = new DroneServiceImpl();
        droneService.droneRepository = droneRepository;
        droneService.droneMedicationRepository = droneMedicationRepository;
    }

    @Test
    void testRegisterDrone() {
        Drone drone = new Drone();
        drone.setSerialNumber("SN123");
        drone.setModel(Model.LIGHTWEIGHT);
        drone.setWeightLimit(200);
        drone.setBatteryCapacity(100);
        drone.setState(State.IDLE);

        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Drone registeredDrone = droneService.registerDrone("SN123", Model.LIGHTWEIGHT, 200, 100, State.IDLE);

        assertNotNull(registeredDrone);
        assertEquals("SN123", registeredDrone.getSerialNumber());
        verify(droneRepository, times(1)).save(any(Drone.class));
    }

    @Test
    void testLoadDroneWithMedication() {
        Drone drone = new Drone();
        drone.setId(1L);
        drone.setWeightLimit(200);
        drone.setBatteryCapacity(100);
        drone.setState(State.IDLE);

        Medication medication = new Medication();
        medication.setName("Med1");
        medication.setWeight(100);

        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));
        when(droneMedicationRepository.findByDroneId(1L)).thenReturn(List.of());
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(new DroneMedication(drone, medication));

        DroneMedication droneMedication = droneService.loadDroneWithMedication(medication);

        assertNotNull(droneMedication);
        assertEquals(medication, droneMedication.getMedication());
        assertEquals(State.LOADED, drone.getState());
    }

    @Test
    void testGetAvailableDrones() {
        Drone drone1 = new Drone();
        drone1.setState(State.IDLE);

        Drone drone2 = new Drone();
        drone2.setState(State.IDLE);

        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone1, drone2));

        List<Drone> availableDrones = droneService.getAvailableDrones();

        assertEquals(2, availableDrones.size());
    }

    @Test
    void testCheckDroneBatteryLevel() {
        Drone drone = new Drone();
        drone.setId(1L);
        drone.setBatteryCapacity(100);

        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        int batteryLevel = droneService.checkDroneBatteryLevel(1L);

        assertEquals(100, batteryLevel);
    }

    @Test
    void testGetMedicationsByDrone() {
        Drone drone = new Drone();
        drone.setId(1L);

        Medication medication1 = new Medication();
        medication1.setName("Med1");

        Medication medication2 = new Medication();
        medication2.setName("Med2");

        DroneMedication droneMedication1 = new DroneMedication(drone, medication1);
        DroneMedication droneMedication2 = new DroneMedication(drone, medication2);

        when(droneMedicationRepository.findByDroneId(1L)).thenReturn(List.of(droneMedication1, droneMedication2));

        List<Medication> medications = droneService.getMedicationsByDrone(1L);

        assertEquals(2, medications.size());
        assertTrue(medications.contains(medication1));
        assertTrue(medications.contains(medication2));
    }

    @Test
    void testStartDelivery() {
        Drone drone = new Drone();
        drone.setId(1L);
        drone.setState(State.LOADED);

        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        droneService.startDelivery(1L);

        assertEquals(State.DELIVERING, drone.getState());
        verify(droneRepository, times(1)).save(drone);
    }

    @Test
    void testCompleteDelivery() {
        Drone drone = new Drone();
        drone.setId(1L);
        drone.setState(State.DELIVERING);

        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        droneService.completeDelivery(1L);

        assertEquals(State.DELIVERED, drone.getState());
        verify(droneRepository, times(1)).save(drone);
    }

    @Test
    void testReturnToBase() {
        Drone drone = new Drone();
        drone.setId(1L);
        drone.setState(State.DELIVERED);

        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        droneService.returnToBase(1L);

        assertEquals(State.IDLE, drone.getState());
        verify(droneRepository, times(2)).save(drone); // Called twice: once for RETURNING and once for IDLE
    }
}
