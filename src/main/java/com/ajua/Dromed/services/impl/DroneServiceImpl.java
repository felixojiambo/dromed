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
   // private static final int MAX_DRONE_COUNT = 10;
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
     * Registers a new drone if the maximum drone count has not been exceeded.
     *
     * @param serialNumber The serial number of the drone. It should be unique for each drone.
     * @param model The model of the drone. This should be a valid Model enum value.
     * @param weightLimit The weight limit of the drone in grams. It must be within the permissible range defined by the application.
     * @param batteryCapacity The battery capacity of the drone as a percentage (0-100).
     * @param state The initial state of the drone. This should be a valid State enum value.
     * @return The registered drone as a DroneDTO containing the drone's details.
     * @throws IllegalStateException if the maximum number of drones allowed is exceeded.
     */
    @Override
    @Transactional
    public DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
//        if (droneRepository.count() >= MAX_DRONE_COUNT) {
//            throw new IllegalStateException("Cannot register more than " + MAX_DRONE_COUNT + " drones.");
//        }

        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return DTOConverter.toDroneDTO(droneRepository.save(drone));
    }


    /**
     * Loads a drone with medication.
     *
     * @param medicationDTO The medication DTO.
     * @return The loaded drone medication as a DroneMedicationDTO.
     */
    @Override
    @Transactional
    public DroneMedicationDTO loadDroneWithMedication(MedicationDTO medicationDTO) {
        Medication medication = new Medication(medicationDTO.getId(), medicationDTO.getName(), medicationDTO.getWeight(),
                medicationDTO.getCode(), medicationDTO.getImageUrl());

        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.get(0); // Get the first available drone

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
     * Gets the medications loaded on a specific drone.
     *
     * @param droneId The ID of the drone.
     * @return A list of MedicationDTOs loaded on the drone.
     */
    @Override
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
    public List<DroneDTO> getAvailableDrones() {
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
     * Returns a drone to base after delivery.
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
     * Marks a drone as idle.
     *
     * @param id The ID of the drone.
     * @return A ResponseEntity indicating the result of the operation.
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

}
