package com.ajua.Dromed.services;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.repository.MedicationRepository;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.services.MedicationService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationServiceTest {

    @InjectMocks
    private MedicationService medicationService;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private DroneMedicationRepository droneMedicationRepository;

    @Test
    public void testLoadMedication() {
        Medication medication = new Medication(null, "Paracetamol", 50, "PARA_001", "image.jpg");
        Mockito.when(medicationRepository.save(any(Medication.class))).thenReturn(medication);

        Medication loadedMedication = medicationService.loadMedication(medication);

        assertEquals("Paracetamol", loadedMedication.getName());
        assertEquals(50, loadedMedication.getWeight());
        assertEquals("PARA_001", loadedMedication.getCode());
    }

    @Test
    public void testGetMedicationsByDrone() {
        Medication medication1 = new Medication(null, "Paracetamol", 50, "PARA_001", "image.jpg");
        Medication medication2 = new Medication(null, "Aspirin", 30, "ASPI_002", "image.jpg");

        DroneMedication droneMedication1 = new DroneMedication(null, null, medication1);
        DroneMedication droneMedication2 = new DroneMedication(null, null, medication2);

        List<DroneMedication> droneMedications = Arrays.asList(droneMedication1, droneMedication2);

        Mockito.when(droneMedicationRepository.findByDroneId(1L)).thenReturn(droneMedications);

        List<Medication> medications = medicationService.getMedicationsByDrone(1L);

        assertEquals(2, medications.size());
        assertEquals("Paracetamol", medications.get(0).getName());
        assertEquals("Aspirin", medications.get(1).getName());
    }
}
