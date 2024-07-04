package com.ajua.Dromed.contollers;
import com.ajua.Dromed.controllers.DroneController;
import com.ajua.Dromed.dtos.*;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.services.interfaces.DroneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DroneController.class)
public class DroneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DroneService droneService;

    @Autowired
    private ObjectMapper objectMapper;

    private DroneDTO droneDTO;
    private MedicationDTO medicationDTO;

    @BeforeEach
    public void setup() {
        droneDTO = new DroneDTO(1L, "SN123", Model.LIGHTWEIGHT, 300, 75, State.IDLE);
        medicationDTO = new MedicationDTO(1L, "Med1", 100, "CODE1", "http://image.url");
    }

    @Test
    public void testRegisterDrone() throws Exception {
        Mockito.when(droneService.registerDrone(any(), any(), anyInt(), anyInt(), any())).thenReturn(droneDTO);

        mockMvc.perform(post("/api/v1/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(droneDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Drone registered successfully"))
                .andExpect(jsonPath("$.data.serialNumber").value("SN123"));
    }

    @Test
    public void testUpdateDroneState() throws Exception {
        DroneStateDTO droneStateDTO = new DroneStateDTO(State.RETURNING);

        mockMvc.perform(patch("/api/v1/drones/1/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(droneStateDTO)))
                .andExpect(status().isOk());
    }
    @Test
    public void testLoadDroneWithMedication() throws Exception {
        DroneMedicationDTO droneMedicationDTO = new DroneMedicationDTO(1L, droneDTO, medicationDTO);

        Mockito.when(droneService.loadDroneWithMedication(anyLong(), any(MedicationDTO.class))).thenReturn(droneMedicationDTO);

        mockMvc.perform(post("/api/v1/drones/1/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Medication loaded successfully"))
                .andExpect(jsonPath("$.data.drone.serialNumber").value("SN123"));
    }


    @Test
    public void testGetAvailableDrones() throws Exception {
        List<DroneDTO> drones = Collections.singletonList(droneDTO);

        Mockito.when(droneService.getAvailableDrones(any())).thenReturn(drones);

        mockMvc.perform(get("/api/v1/drones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List of available drones"))
                .andExpect(jsonPath("$.data[0].serialNumber").value("SN123"));
    }

    @Test
    public void testCheckDroneBatteryLevel() throws Exception {
        Mockito.when(droneService.checkDroneBatteryLevel(anyLong())).thenReturn(75);

        mockMvc.perform(get("/api/v1/drones/1/battery-level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Battery level retrieved successfully"))
                .andExpect(jsonPath("$.data").value(75));
    }

    @Test
    public void testGetMedicationsByDrone() throws Exception {
        List<MedicationDTO> medications = Collections.singletonList(medicationDTO);

        Mockito.when(droneService.getMedicationsByDrone(anyLong())).thenReturn(medications);

        mockMvc.perform(get("/api/v1/drones/1/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List of medications retrieved successfully"))
                .andExpect(jsonPath("$.data[0].name").value("Med1"));
    }

    @Test
    public void testHandleDelivery() throws Exception {
        mockMvc.perform(post("/api/v1/drones/1/deliveries?action=start"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery process started"));

        mockMvc.perform(post("/api/v1/drones/1/deliveries?action=complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery completed successfully"));
    }

    @Test
    public void testReturnToBase() throws Exception {
        mockMvc.perform(post("/api/v1/drones/1/return-to-base"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Drone return process started"));
    }
}
