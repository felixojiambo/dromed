//package com.ajua.Dromed.integration;
//
//import com.ajua.Dromed.enums.Model;
//import com.ajua.Dromed.enums.State;
//import com.ajua.Dromed.models.Drone;
//import com.ajua.Dromed.repository.DroneRepository;
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
//public class GetAvailableDronesIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Test
//    public void whenGetAvailableDrones_thenDronesAreReturned() {
//        Drone drone = new Drone();
//        drone.setSerialNumber("SN127");
//        drone.setModel(Model.LIGHTWEIGHT);
//        drone.setWeightLimit(400);
//        drone.setBatteryCapacity(100);
//        drone.setState(State.IDLE);
//        droneRepository.save(drone);
//
//        ResponseEntity<Drone[]> response = restTemplate.getForEntity("/api/drones/available", Drone[].class);
//
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isNotEmpty();
//
//        List<Drone> availableDrones = List.of(response.getBody());
//        assertThat(availableDrones).extracting(Drone::getSerialNumber).contains("SN127");
//        for (Drone availableDrone : availableDrones) {
//            assertThat(availableDrone.getState()).isEqualTo(State.IDLE);
//        }
//    }
//}
