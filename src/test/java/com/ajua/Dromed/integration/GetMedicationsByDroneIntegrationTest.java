//package com.ajua.Dromed.integration;
//
//import com.ajua.Dromed.enums.Model;
//import com.ajua.Dromed.enums.State;
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
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@Transactional
//public class GetMedicationsByDroneIntegrationTest {
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
//    public void whenGetMedicationsByDrone_thenMedicationsAreReturned() {
//        Drone drone = new Drone();
//        drone.setSerialNumber("SN129");
//        drone.setModel(Model.CRUISERWEIGHT);
//        drone.setWeightLimit(450);
//        drone.setBatteryCapacity(90);
//        drone.setState(State.IDLE);
//        droneRepository.save(drone);
//
//        Medication medication = new Medication();
//        medication.setName("Paracetamol");
//        medication.setWeight(50);
//        medication.setCode("PARA_002");
//        medication.setImageUrl("http://example.com/images/paracetamol.jpg");
//        medicationRepository.save(medication);
//
//        DroneMedication droneMedication = new DroneMedication();
//        droneMedication.setDrone(drone);
//        droneMedication.setMedication(medication);
//        droneMedicationRepository.save(droneMedication);
//
//        ResponseEntity<Medication[]> response = restTemplate.getForEntity("/api/drones/" + drone.getId() + "/medications", Medication[].class);
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isNotEmpty();
//
//        List<Medication> medications = List.of(response.getBody());
//        assertThat(medications).extracting(Medication::getName).contains("Paracetamol");
//    }
//}
