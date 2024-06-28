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
//public class RegisterDroneIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Test
//    public void whenRegisterDrone_thenDroneIsCreated() {
//        Drone drone = new Drone();
//        drone.setSerialNumber("SN126");
//        drone.setModel(Model.HEAVYWEIGHT);
//        drone.setWeightLimit(450);
//        drone.setBatteryCapacity(100);
//        drone.setState(State.IDLE);
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<Drone> request = new HttpEntity<>(drone, headers);
//
//        ResponseEntity<Drone> response = restTemplate.exchange("/api/drones", HttpMethod.POST, request, Drone.class);
//
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getSerialNumber()).isEqualTo("SN126");
//
//        Drone savedDrone = (Drone) droneRepository.findBySerialNumber("SN126").orElse(null);
//        assertThat(savedDrone).isNotNull();
//        assertThat(savedDrone.getModel()).isEqualTo(Model.HEAVYWEIGHT);
//    }
//}
