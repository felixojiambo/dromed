//package com.ajua.Dromed.integration;
//
//import com.ajua.Dromed.models.Drone;
//import com.ajua.Dromed.models.DroneMedication;
//import com.ajua.Dromed.models.Medication;
//import com.ajua.Dromed.repository.DroneMedicationRepository;
//import com.ajua.Dromed.repository.DroneRepository;
//import com.ajua.Dromed.repository.MedicationRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@Transactional
//public class LoadDroneWithMedicationIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Autowired
//    private MedicationRepository medicationRepository;
//
//    @Autowired
//    private DroneMedicationRepository droneMedicationRepository;
//
//    @Test
//    public void whenLoadDroneWithMedication_thenDroneMedicationIsCreated() {
//        Drone drone = droneRepository.findById(1L).orElseThrow();
//        Medication medication = medicationRepository.findById(1L).orElseThrow();
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<Medication> request = new HttpEntity<>(medication, headers);
//
//        ResponseEntity<DroneMedication> response = restTemplate.exchange("/api/drones/load?droneId=" + drone.getId(), HttpMethod.POST, request, DroneMedication.class);
//
//
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getDrone().getId()).isEqualTo(drone.getId());
//        assertThat(response.getBody().getMedication().getId()).isEqualTo(medication.getId());
//
//        DroneMedication savedDroneMedication = (DroneMedication) droneMedicationRepository.findByDroneAndMedication(drone, medication).orElse(null);
//        assertThat(savedDroneMedication).isNotNull();
//    }
//}
