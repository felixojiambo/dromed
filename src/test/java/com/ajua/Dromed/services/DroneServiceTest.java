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

import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.DroneService;

@RunWith(MockitoJUnitRunner.class)
public class DroneServiceTest {

    @InjectMocks
    private DroneService droneService;

    @Mock
    private DroneRepository droneRepository;

    @Test
    public void testRegisterDrone() {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Mockito.when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Drone createdDrone = droneService.registerDrone("SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);

        assertEquals("SN123", createdDrone.getSerialNumber());
        assertEquals(Model.LIGHTWEIGHT, createdDrone.getModel());
        assertEquals(200, createdDrone.getWeightLimit());
        assertEquals(80, createdDrone.getBatteryCapacity());
        assertEquals(State.IDLE, createdDrone.getState());
    }

    @Test
    public void testGetAvailableDrones() {
        List<Drone> drones = Arrays.asList(
                new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE),
                new Drone(null, "SN124", Model.MIDDLEWEIGHT, 300, 60, State.IDLE)
        );
        Mockito.when(droneRepository.findByState(State.IDLE)).thenReturn(drones);

        List<Drone> availableDrones = droneService.getAvailableDrones();

        assertEquals(2, availableDrones.size());
    }

    @Test
    public void testCheckDroneBatteryLevel() {
        Drone drone = new Drone(null, "SN123", Model.LIGHTWEIGHT, 200, 80, State.IDLE);
        Mockito.when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        int batteryLevel = droneService.checkDroneBatteryLevel(1L);

        assertEquals(80, batteryLevel);
    }
}
