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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DroneServiceImplTest {

    @InjectMocks
    private DroneServiceImpl droneService;

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneMedicationRepository droneMedicationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        DroneDTO registeredDrone = droneService.registerDrone("SN123", Model.LIGHTWEIGHT, 200, 100, State.IDLE);

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

        MedicationDTO medicationDTO = new MedicationDTO();
        medicationDTO.setId(1L);
        medicationDTO.setName("Med1");
        medicationDTO.setWeight(100);

        Medication medication = new Medication();
        medication.setId(1L);
        medication.setName("Med1");
        medication.setWeight(100);

        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone));
        when(droneMedicationRepository.findByDroneId(1L)).thenReturn(List.of());
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(new DroneMedication(drone, medication));

        DroneMedicationDTO droneMedicationDTO = droneService.loadDroneWithMedication(medicationDTO);

        assertNotNull(droneMedicationDTO);
        assertEquals(medicationDTO.getName(), droneMedicationDTO.getMedication().getName());
        assertEquals(State.LOADED, droneMedicationDTO.getDrone().getState());
    }

    @Test
    void testGetAvailableDrones() {
        Drone drone1 = new Drone();
        drone1.setState(State.IDLE);

        Drone drone2 = new Drone();
        drone2.setState(State.IDLE);

        when(droneRepository.findByState(State.IDLE)).thenReturn(List.of(drone1, drone2));

        List<DroneDTO> availableDrones = droneService.getAvailableDrones();

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

        List<MedicationDTO> medications = droneService.getMedicationsByDrone(1L);

        // Debug statements
        System.out.println("Expected Medication 1: " + medication1);
        System.out.println("Expected Medication 2: " + medication2);
        System.out.println("Medications returned: " + medications);

        assertEquals(2, medications.size());
        assertTrue(medications.stream().anyMatch(med -> med.getName().equals("Med1")));
        assertTrue(medications.stream().anyMatch(med -> med.getName().equals("Med2")));
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
    @Test
    void testMarkIdle() {

        Drone drone = new Drone();
        drone.setId(1L);
        drone.setState(State.RETURNING);


        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        when(droneRepository.save(any(Drone.class))).thenReturn(drone);


        droneService.markIdle(1L);

        assertEquals(State.IDLE, drone.getState());

        verify(droneRepository, times(1)).save(drone);
    }
}
