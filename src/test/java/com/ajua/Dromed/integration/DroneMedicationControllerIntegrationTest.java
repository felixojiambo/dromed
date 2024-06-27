package com.ajua.Dromed.integration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DroneMedicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLoadDroneWithMedication() throws Exception {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Medication medication = new Medication(null, "Med1", 100, "M123", "image.png");

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/drones/SN123/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber", is("SN123")))
                .andExpect(jsonPath("$.medications[0].name", is("Med1")));
    }

    @Test
    public void testGetLoadedMedications() throws Exception {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Medication medication = new Medication(null, "Med1", 100, "M123", "image.png");

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/drones/SN123/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/drones/SN123/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Med1")));
    }
}
