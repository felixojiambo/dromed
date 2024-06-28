package com.ajua.Dromed.services.impl;
import com.ajua.Dromed.dtos.DroneDTO;
import com.ajua.Dromed.dtos.DroneMedicationDTO;
import com.ajua.Dromed.dtos.MedicationDTO;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.exceptions.ResourceNotFoundException;
import com.ajua.Dromed.exceptions.DroneNotAvailableException;
import com.ajua.Dromed.exceptions.OverweightException;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.services.interfaces.DroneService;
import com.ajua.Dromed.services.patterns.DroneFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class DroneServiceImpl implements DroneService {

    private static final int MAX_WEIGHT_LIMIT = 500; // Max weight limit for drones
    private static final int MIN_BATTERY_LEVEL = 25; // Min battery level for loading

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    /**
     * Converts a Drone entity to its corresponding DTO.
     *
     * @param drone The Drone entity to convert.
     * @return Corresponding DroneDTO.
     */
    private DroneDTO toDroneDTO(Drone drone) {
        DroneDTO dto = new DroneDTO();
        dto.setId(drone.getId());
        dto.setSerialNumber(drone.getSerialNumber());
        dto.setModel(drone.getModel());
        dto.setWeightLimit(drone.getWeightLimit());
        dto.setBatteryCapacity(drone.getBatteryCapacity());
        dto.setState(drone.getState());
        return dto;
    }

    /**
     * Converts a Medication entity to its corresponding DTO.
     *
     * @param medication The Medication entity to convert.
     * @return Corresponding MedicationDTO.
     */
    private MedicationDTO toMedicationDTO(Medication medication) {
        MedicationDTO dto = new MedicationDTO();
        dto.setId(medication.getId());
        dto.setName(medication.getName());
        dto.setWeight(medication.getWeight());
        dto.setCode(medication.getCode());
        dto.setImageUrl(medication.getImageUrl());
        return dto;
    }

    /**
     * Converts a DroneMedication entity to its corresponding DTO.
     *
     * @param droneMedication The DroneMedication entity to convert.
     * @return Corresponding DroneMedicationDTO.
     */
    private DroneMedicationDTO toDroneMedicationDTO(DroneMedication droneMedication) {
        DroneMedicationDTO dto = new DroneMedicationDTO();
        dto.setId(droneMedication.getId());
        dto.setDrone(toDroneDTO(droneMedication.getDrone()));
        dto.setMedication(toMedicationDTO(droneMedication.getMedication()));
        return dto;
    }

    /**
     * Registers a new drone with the specified parameters.
     *
     * @param serialNumber   Serial number of the drone.
     * @param model          Model of the drone.
     * @param weightLimit    Weight limit of the drone.
     * @param batteryCapacity Battery capacity of the drone.
     * @param state          Initial state of the drone.
     * @return The newly registered DroneDTO object.
     * @throws IllegalArgumentException if weight limit exceeds the maximum allowed.
     */
    @Override
    @Transactional
    public DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        if (weightLimit > MAX_WEIGHT_LIMIT) {
            throw new IllegalArgumentException("Weight limit cannot exceed 500 grams");
        }
        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return toDroneDTO(droneRepository.save(drone));
    }

    /**
     * Loads a drone with a specified medication.
     *
     * @param medicationDTO The medication to load onto the drone as DTO.
     * @return The DroneMedicationDTO object representing the loaded medication.
     * @throws DroneNotAvailableException if no drone is available for loading.
     * @throws IllegalStateException     if battery level is below 25%, weight limit is exceeded,
     *                                   or drone is not in a suitable state for loading.
     */
    @Override
    @Transactional
    public DroneMedicationDTO loadDroneWithMedication(MedicationDTO medicationDTO) {
        Medication medication = new Medication();
        medication.setId(medicationDTO.getId());
        medication.setName(medicationDTO.getName());
        medication.setWeight(medicationDTO.getWeight());
        medication.setCode(medicationDTO.getCode());
        medication.setImageUrl(medicationDTO.getImageUrl());

        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.getFirst(); // Get the first available drone

        if (drone.getBatteryCapacity() < MIN_BATTERY_LEVEL) {
            throw new IllegalStateException("Battery level is below 25%");
        }

        int totalWeight = getTotalLoadedWeight(drone.getId());

        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
            throw new OverweightException("Weight limit exceeded");
        }

        if (!drone.getState().equals(State.IDLE) && !drone.getState().equals(State.LOADING)) {
            throw new DroneNotAvailableException("Drone is not available for loading");
        }

        drone.setState(State.LOADING);
        droneRepository.save(drone);

        DroneMedication droneMedication = new DroneMedication(drone, medication);
        droneMedicationRepository.save(droneMedication);

        // Update state to LOADED if the drone is fully loaded
        drone.setState(State.LOADED);
        droneRepository.save(drone);

        return toDroneMedicationDTO(droneMedication);
    }

    /**
     * Retrieves medications loaded onto a specific drone.
     *
     * @param droneId The ID of the drone to fetch medications for.
     * @return List of MedicationDTO loaded onto the drone.
     */
    @Override
    @Retryable(maxAttempts = 3, retryFor = RuntimeException.class, backoff = @Backoff(delay = 2000))
    public List<MedicationDTO> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(droneMedication -> toMedicationDTO(droneMedication.getMedication()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of available drones that are currently idle.
     *
     * @return List of available DroneDTO objects.
     */
    @Override
    public List<DroneDTO> getAvailableDrones() {
        return droneRepository.findByState(State.IDLE)
                .stream()
                .map(this::toDroneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Checks the battery increased of a specific drone.
     *
     * @param droneId The ID of the drone to check battery level for.
     * @return Battery level percentage.
     * @throws ResourceNotFoundException if the drone with the given ID is not found.
     */
    @Override
    public int checkDroneBatteryLevel(Long droneId) {
        return droneRepository.findById(droneId)
                .map(Drone::getBatteryCapacity)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
    }

    /**
     * Calculates the total weight of medications loaded onto a specific drone.
     *
     * @param droneId The ID of the drone to calculate loaded weight for.
     * @return Total weight of medications loaded on the drone.
     */
    @Override
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

    /**
     * Initiates delivery for a specific drone.
     *
     * @param droneId The ID of the drone to start delivery for.
     * @throws IllegalStateException if the drone is not in a loaded state.
     */
    @Override
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
     * Completes delivery for a specific drone.
     *
     * @param droneId The ID of the drone to complete delivery for.
     * @throws IllegalStateException if the drone is not in a delivering state.
     */
    @Override
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
     * Initiates the process of returning a drone to base after delivery.
     *
     * @param droneId The ID of the drone to return to base.
     * @throws IllegalStateException if the drone is not in a delivered state.
     */
    @Override
    public void returnToBase(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));

        if (!drone.getState().equals(State.DELIVERED)) {
            throw new IllegalStateException("Drone is not in a state to return");
        }

        drone.setState(State.RETURNING);
        droneRepository.save(drone);

        // After returning
        drone.setState(State.IDLE);
        droneRepository.save(drone);
    }
    /**
     * Marks a drone as idle if it is currently returning.
     *
     * @param id The ID of the drone to mark as idle.
     * @return ResponseEntity with status 200 (OK) if successful,
     *         404 (Not Found) if no drone with the given ID exists,
     *         or 409 (Conflict) if the drone is not in the RETURNING state.
     */
    @Override
    @Transactional
    public ResponseEntity<Void> markIdle(Long id) {
        Optional<Drone> droneOptional = droneRepository.findById(id);
        if (droneOptional.isPresent()) {
            Drone drone = droneOptional.get();
            if (drone.getState() == State.RETURNING) {
                drone.setState(State.IDLE);
                droneRepository.save(drone);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.notFound().build();
    }

}
