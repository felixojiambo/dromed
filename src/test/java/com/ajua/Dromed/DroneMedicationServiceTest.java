package com.ajua.Dromed;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.DroneMedicationService;

@RunWith(MockitoJUnitRunner.class)
public class DroneMedicationServiceTest {

    @InjectMocks
    private DroneMedicationService droneMedicationService;

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneMedicationRepository droneMedicationRepository;

    @Test
    public void testLoadDroneWithMedication() {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Medication medication = new Medication(null, "Paracetamol", 50, "PARA_001", "image.jpg");

        Mockito.when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        Mockito.when(droneMedicationRepository.findByDroneId(1L)).thenReturn(new ArrayList<>());

        DroneMedication droneMedication = new DroneMedication(null, drone, medication);
        Mockito.when(droneMedicationRepository.save(any(DroneMedication.class))).thenReturn(droneMedication);

        DroneMedication loadedDroneMedication = droneMedicationService.loadDroneWithMedication(1L, medication);

        assertEquals(drone, loadedDroneMedication.getDrone());
        assertEquals(medication, loadedDroneMedication.getMedication());
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadDroneWithMedication_BatteryBelow25() {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 20, State.IDLE); // Battery level below 25
        Medication medication = new Medication(null, "Paracetamol", 50, "PARA_001", "image.jpg");

        Mockito.when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        droneMedicationService.loadDroneWithMedication(1L, medication);
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadDroneWithMedication_WeightLimitExceeded() {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Medication medication = new Medication(null, "Paracetamol", 250, "PARA_001", "image.jpg");

        List<DroneMedication> droneMedications = Collections.singletonList(
                new DroneMedication(null, drone, new Medication(null, "Aspirin", 200, "ASPI_002", "image.jpg"))
        );

        Mockito.when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));
        Mockito.when(droneMedicationRepository.findByDroneId(1L)).thenReturn(droneMedications);

        droneMedicationService.loadDroneWithMedication(1L, medication);
    }
}
