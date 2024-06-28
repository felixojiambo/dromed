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
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@Transactional
//public class GetTotalLoadedWeightIntegrationTest {
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
//    public void whenGetTotalLoadedWeight_thenWeightIsReturned() {
//        Drone drone = new Drone();
//        drone.setSerialNumber("SN130");
//        drone.setModel(Model.CRUISERWEIGHT);
//        drone.setWeightLimit(450);
//        drone.setBatteryCapacity(90);
//        drone.setState(State.IDLE);
//        droneRepository.save(drone);
//
//        Medication medication1 = new Medication();
//        medication1.setName("Paracetamol");
//        medication1.setWeight(50);
//        medication1.setCode("PARA_002");
//        medication1.setImageUrl("http://example.com/images/paracetamol.jpg");
//        medicationRepository.save(medication1);
//
//        Medication medication2 = new Medication();
//        medication2.setName("Ibuprofen");
//        medication2.setWeight(100);
//        medication2.setCode("IBUP_003");
//        medication2.setImageUrl("http://example.com/images/ibuprofen.jpg");
//        medicationRepository.save(medication2);
//
//        DroneMedication droneMedication1 = new DroneMedication();
//        droneMedication1.setDrone(drone);
//        droneMedication1.setMedication(medication1);
//        droneMedicationRepository.save(droneMedication1);
//
//        DroneMedication droneMedication2 = new DroneMedication();
//        droneMedication2.setDrone(drone);
//        droneMedication2.setMedication(medication2);
//        droneMedicationRepository.save(droneMedication2);
//
//        ResponseEntity<Integer> response = restTemplate.getForEntity("/api/drones/" + drone.getId() + "/total-weight", Integer.class);
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isEqualTo(150);
//    }
//}
