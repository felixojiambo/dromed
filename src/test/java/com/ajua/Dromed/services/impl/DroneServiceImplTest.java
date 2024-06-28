package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setUp() {
        drone = new Drone();
        drone.setId(1L);
        drone.setSerialNumber("12345");
        drone.setModel(Model.HEAVYWEIGHT);
        drone.setWeightLimit(500);
        drone.setBatteryCapacity(100);
        drone.setState(State.IDLE);

        medication = new Medication();
        medication.setName("Med1");
        medication.setWeight(100);
    }

    @Test
    void registerDrone_Success() {
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Drone result = droneService.registerDrone("12345", Model.HEAVYWEIGHT, 500, 100, State.IDLE);

        assertNotNull(result);
        assertEquals(drone.getSerialNumber(), result.getSerialNumber());
        verify(droneRepository, times(1)).save(any(Drone.class));
    }

    @Test
    void loadDroneWithMedication_NoAvailableDrones() {
        when(droneRepository.findByState(State.IDLE)).thenReturn(new ArrayList<>());

        assertThrows(DroneNotAvailableException.class, () -> droneService.loadDroneWithMedication(medication));
    }

    @Test
    void loadDroneWithMedication_Overweight() {
        drone.setWeightLimit(50);
        List<Drone> drones = new ArrayList<>();
        drones.add(drone);

        when(droneRepository.findByState(State.IDLE)).thenReturn(drones);

        assertThrows(OverweightException.class, () -> droneService.loadDroneWithMedication(medication));
    }

    @Test
    void loadDroneWithMedication_Success() {
        List<Drone> drones = new ArrayList<>();
        drones.add(drone);

        when(droneRepository.findByState(State.IDLE)).thenReturn(drones);
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(new DroneMedication());

        DroneMedication result = droneService.loadDroneWithMedication(medication);

        assertNotNull(result);
        verify(droneMedicationRepository, times(1)).save(any(DroneMedication.class));
    }

    @Test
    void getMedicationsByDrone_Success() {
        List<DroneMedication> droneMedications = new ArrayList<>();
        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);
        droneMedications.add(droneMedication);

        when(droneMedicationRepository.findByDroneId(drone.getId())).thenReturn(droneMedications);

        List<Medication> result = droneService.getMedicationsByDrone(drone.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(medication.getName(), result.get(0).getName());
    }

    @Test
    void checkDroneBatteryLevel_Success() {
        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        int result = droneService.checkDroneBatteryLevel(drone.getId());

        assertEquals(drone.getBatteryCapacity(), result);
    }

    @Test
    void getTotalLoadedWeight_Success() {
        List<DroneMedication> droneMedications = new ArrayList<>();
        DroneMedication droneMedication = new DroneMedication();
        droneMedication.setDrone(drone);
        droneMedication.setMedication(medication);
        droneMedications.add(droneMedication);

        when(droneMedicationRepository.findByDroneId(drone.getId())).thenReturn(droneMedications);

        int result = droneService.getTotalLoadedWeight(drone.getId());

        assertEquals(medication.getWeight(), result);
    }

    @Test
    void startDelivery_Success() {
        drone.setState(State.LOADED);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        droneService.startDelivery(drone.getId());

        assertEquals(State.DELIVERING, drone.getState());
        verify(droneRepository, times(1)).save(drone);
    }

    @Test
    void completeDelivery_Success() {
        drone.setState(State.DELIVERING);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        droneService.completeDelivery(drone.getId());

        assertEquals(State.DELIVERED, drone.getState());
        verify(droneRepository, times(1)).save(drone);
    }

    @Test
    void returnToBase_Success() {
        drone.setState(State.DELIVERED);

        when(droneRepository.findById(drone.getId())).thenReturn(Optional.of(drone));

        droneService.returnToBase(drone.getId());

        assertEquals(State.IDLE, drone.getState());
        verify(droneRepository, times(2)).save(drone); // called twice, once for RETURNING and once for IDLE
    }
}
