package com.ajua.Dromed.services.impl;

import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
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
    private final DroneRepository droneRepository;
    private final DroneMedicationRepository droneMedicationRepository;

    /**
     * Constructor for DroneServiceImpl.
     *
     * @param droneRepository The drone repository.
     * @param droneMedicationRepository The drone medication repository.
     */
    public DroneServiceImpl(DroneRepository droneRepository, DroneMedicationRepository droneMedicationRepository) {
        this.droneRepository = droneRepository;
        this.droneMedicationRepository = droneMedicationRepository;
    }

    /**
     * Registers a new drone.
     *
     * @param serialNumber The serial number of the drone. It should be unique for each drone.
     * @param model The model of the drone. This should be a valid Model enum value.
     * @param weightLimit The weight limit of the drone in grams. It must be within the permissible range defined by the application.
     * @param batteryCapacity The battery capacity of the drone as a percentage (0-100).
     * @param state The initial state of the drone. This should be a valid State enum value.
     * @return The registered drone as a DroneDTO containing the drone's details.
     */
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

    /**
     * Loads a drone with medication.
     *
     * @param id
     * @param medicationDTO The medication DTO.
     * @return The loaded drone medication as a DroneMedicationDTO.
     */
    @Override
    @Transactional
    @Retry(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @CircuitBreaker(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @RateLimiter(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @Bulkhead(name = "loadDroneWithMedication", fallbackMethod = "loadDroneWithMedicationFallback")
    @TimeLimiter(name = "default", fallbackMethod = "loadDroneWithMedicationFallback")
    public DroneMedicationDTO loadDroneWithMedication(Long id, MedicationDTO medicationDTO) {
        Medication medication = new Medication.Builder()
                .id(medicationDTO.getId())
                .name(medicationDTO.getName())
                .weight(medicationDTO.getWeight())
                .code(medicationDTO.getCode())
                .imageUrl(medicationDTO.getImageUrl())
                .build();

        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.getFirst();

        validateLoadingConditions(drone, medication);

        drone.setState(State.LOADING);
        droneRepository.save(drone);

        DroneMedication droneMedication = new DroneMedication(drone, medication);
        droneMedicationRepository.save(droneMedication);

        drone.setState(State.LOADED);
        droneRepository.save(drone);

        return DTOConverter.toDroneMedicationDTO(droneMedication);
    }

    /**
     * Fallback method for loadDroneWithMedication in case of failure.
     *
     * @param medicationDTO The medication DTO.
     * @param t The throwable that caused the fallback.
     * @return A default DroneMedicationDTO.
     */
    public DroneMedicationDTO loadDroneWithMedicationFallback(MedicationDTO medicationDTO, Throwable t) {
        return new DroneMedicationDTO();
    }

    /**
     * Gets the medications loaded on a specific drone.
     *
     * @param droneId The ID of the drone.
     * @return A list of MedicationDTOs loaded on the drone.
     */
    @Override
    @Cacheable(cacheNames = "getAvailableDrones")
    public List<MedicationDTO> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(dm -> DTOConverter.toMedicationDTO(dm.getMedication()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all available drones.
     *
     * @return A list of available DroneDTOs.
     */
    @Override
    @Cacheable(cacheNames = "getAvailableDrones")
    public List<DroneDTO> getAvailableDrones(State state) {
        return droneRepository.findByState(State.IDLE)
                .stream()
                .map(DTOConverter::toDroneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Checks the battery level of a specific drone.
     *
     * @param droneId The ID of the drone.
     * @return The battery level of the drone.
     * @throws ResourceNotFoundException if the drone is not found.
     */
    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    /**
     * Gets the total loaded weight on a specific drone.
     *
     * @param droneId The ID of the drone.
     * @return The total loaded weight.
     */
    @Override
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

    /**
     * Starts the delivery process for a specific drone.
     *
     * @param droneId The ID of the drone.
     * @throws ResourceNotFoundException if the drone is not found.
     * @throws IllegalStateException if the drone is not ready for delivery.
     */
    @Override
    @Transactional
    public void startDelivery(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.LOADED)) {
            throw new IllegalStateException("Drone is not ready for delivery");
        }

        drone.setState(State.DELIVERING);
        droneRepository.save(drone);
    }

    /**
     * Completes the delivery process for a specific drone.
     *
     * @param droneId The ID of the drone.
     * @throws ResourceNotFoundException if the drone is not found.
     * @throws IllegalStateException if the drone is not delivering.
     */
    @Override
    @Transactional
    public void completeDelivery(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.DELIVERING)) {
            throw new IllegalStateException("Drone is not delivering");
        }

        drone.setState(State.DELIVERED);
        droneRepository.save(drone);
    }

    /**
     * Returns a specific drone to its base.
     *
     * @param droneId The ID of the drone.
     * @throws ResourceNotFoundException if the drone is not found.
     * @throws IllegalStateException if the drone is not in a state to return.
     */
    @Override
    @Transactional
    public void returnToBase(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.DELIVERED)) {
            throw new IllegalStateException("Drone is not in a state to return");
        }

        drone.setState(State.RETURNING);
        droneRepository.save(drone);

        drone.setState(State.IDLE);
        droneRepository.save(drone);
    }

    /**
     * Marks a drone as idle if it is returning to base.
     *
     * @param id The ID of the drone.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Override
    @Transactional
    public ResponseEntity<Object> markIdle(Long id) {
        return droneRepository.findById(id)
                .map(drone -> {
                    if (drone.getState() == State.RETURNING) {
                        drone.setState(State.IDLE);
                        droneRepository.save(drone);
                        return ResponseEntity.ok().build();
                    }
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @Transactional
    public void updateDroneState(Long droneId, State state) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
        drone.setState(state);
        droneRepository.save(drone);
    }
}