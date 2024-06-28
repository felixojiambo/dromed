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
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@Transactional
//public class CheckDroneBatteryLevelIntegrationTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Test
//    public void whenCheckDroneBatteryLevel_thenBatteryLevelIsReturned() {
//        Drone drone = new Drone();
//        drone.setSerialNumber("SN128");
//        drone.setModel(Model.MIDDLEWEIGHT);
//        drone.setWeightLimit(450);
//        drone.setBatteryCapacity(85);
//        drone.setState(State.IDLE);
//        droneRepository.save(drone);
//
//        ResponseEntity<Integer> response = restTemplate.getForEntity("/api/drones/" + drone.getId() + "/battery", Integer.class);
//
//        assertThat(response.getStatusCode().value()).isEqualTo(201);
//        assertThat(response.getBody()).isEqualTo(85);
//    }
//}
