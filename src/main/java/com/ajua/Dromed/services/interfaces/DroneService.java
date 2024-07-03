package com.ajua.Dromed.services.interfaces;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DroneService {


    @Transactional
    @Retry(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @CircuitBreaker(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @RateLimiter(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @Bulkhead(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @TimeLimiter(name = "default", fallbackMethod = "registerDroneFallback")
    DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state);

    @Transactional
    @Retry(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @CircuitBreaker(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @RateLimiter(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @Bulkhead(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @TimeLimiter(name = "default", fallbackMethod = "loadDroneWithMedicationFallback")
    DroneMedicationDTO loadDroneWithMedication(Long id, MedicationDTO medicationDTO);

    @Cacheable(cacheNames = "getAvailableDrones")
    List<MedicationDTO> getMedicationsByDrone(Long droneId);

    @Cacheable(cacheNames = "getAvailableDrones")
    List<DroneDTO> getAvailableDrones(State state);

    int checkDroneBatteryLevel(Long droneId);

    @Transactional
    void startDelivery(Long droneId);

    @Transactional
    void completeDelivery(Long droneId);

    @Transactional
    void returnToBase(Long droneId);

    @Transactional
    ResponseEntity<Object> markIdle(Long id);

    void updateDroneState(Long id, State state);
}
