package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.*;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.interfaces.DroneService;
import com.ajua.Dromed.services.patterns.DroneFactory;
import com.ajua.Dromed.utils.DTOConverter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing drones.
 * Provides methods for registering, loading, and managing drones.
 */
@Service
public class DroneServiceImpl extends AbstractDroneService implements DroneService {
    /**
     * Constructor for DroneServiceImpl.
     *
     * @param droneRepository The drone repository.
     * @param droneMedicationRepository The drone medication repository.
     */
    public DroneServiceImpl(DroneRepository droneRepository, DroneMedicationRepository droneMedicationRepository) {
        super(droneRepository, droneMedicationRepository);
    }

    @Override
    @Transactional
    @Retry(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @CircuitBreaker(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @RateLimiter(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @Bulkhead(name = "registerDrone", fallbackMethod = "registerDroneFallback")
    @TimeLimiter(name = "default", fallbackMethod = "registerDroneFallback")
    public DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        droneRepository.save(drone);
        return DTOConverter.toDroneDTO(drone);
    }

    public DroneDTO registerDroneFallback(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state, Throwable t) {
        return new DroneDTO(0L, "defaultSerial", model, weightLimit, batteryCapacity, State.IDLE);
    }

    @Override
    @Transactional
    @Retry(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @CircuitBreaker(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @RateLimiter(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @Bulkhead(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @TimeLimiter(name = "default", fallbackMethod = "loadDroneWithMedicationFallback")
    public DroneMedicationDTO loadDroneWithMedication(MedicationDTO medicationDTO) {
        Medication medication = new Medication.Builder()
                .id(medicationDTO.getId())
                .name(medicationDTO.getName())
                .weight(medicationDTO.getWeight())
                .code(medicationDTO.getCode())
                .imageUrl(medicationDTO.getImageUrl())
                .build();

        Drone drone = getAvailableDrone();
        validateLoadingConditions(drone, medication);

        transitionDroneState(drone, State.LOADING);
        DroneMedication droneMedication = new DroneMedication(drone, medication);
        droneMedicationRepository.save(droneMedication);
        transitionDroneState(drone, State.LOADED);

        return DTOConverter.toDroneMedicationDTO(droneMedication);
    }

    public DroneMedicationDTO loadDroneWithMedicationFallback(MedicationDTO medicationDTO, Throwable t) {
        return new DroneMedicationDTO();
    }

    @Override
    @Cacheable(cacheNames = "getAvailableDrones")
    public List<MedicationDTO> getMedicationsByDrone(Long droneId) {
        return findMedicationsByDrone(droneId) // Method renamed
                .stream()
                .map(DTOConverter::toMedicationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "getAvailableDrones")
    public List<DroneDTO> getAvailableDrones() {
        return droneRepository.findByState(State.IDLE)
                .stream()
                .map(DTOConverter::toDroneDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    @Override
    @Transactional
    public void startDelivery(Long droneId) {
        Drone drone = getDroneById(droneId);
        validateDroneState(drone, State.LOADED);
        transitionDroneState(drone, State.DELIVERING);
    }

    @Override
    @Transactional
    public void completeDelivery(Long droneId) {
        Drone drone = getDroneById(droneId);
        validateDroneState(drone, State.DELIVERING);
        transitionDroneState(drone, State.DELIVERED);
    }

    @Override
    @Transactional
    public void returnToBase(Long droneId) {
        Drone drone = getDroneById(droneId);
        validateDroneState(drone, State.DELIVERED);
        transitionDroneState(drone, State.RETURNING);
        transitionDroneState(drone, State.IDLE);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> markIdle(Long id) {
        return droneRepository.findById(id)
                .map(drone -> {
                    if (drone.getState() == State.RETURNING) {
                        transitionDroneState(drone, State.IDLE);
                        return ResponseEntity.ok().build();
                    }
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
