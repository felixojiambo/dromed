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
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DroneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterDrone() throws Exception {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber", is("SN123")))
                .andExpect(jsonPath("$.model", is(Model.LIGHTWEIGHT.name())))
                .andExpect(jsonPath("$.weightLimit", is(200)))
                .andExpect(jsonPath("$.batteryCapacity", is(80)))
                .andExpect(jsonPath("$.state", is(State.IDLE.name())));
    }

    @Test
    public void testGetAvailableDrones() throws Exception {
        Drone drone1 = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Drone drone2 = new Drone(null, "SN124", Model.MIDDLEWEIGHT, 300, 60, State.IDLE);

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/drones/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    public void testCheckDroneBatteryLevel() throws Exception {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone)))
                .andExpect(status().isCreated());

        Long droneId = 1L; // Assuming the first drone registered has ID 1

        mockMvc.perform(get("/api/drones/{id}/battery", droneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(80)));
    }
}
