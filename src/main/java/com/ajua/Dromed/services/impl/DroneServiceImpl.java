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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DroneServiceImpl extends AbstractDroneService implements DroneService {

    private final DroneRepository droneRepository;
    private final DroneMedicationRepository droneMedicationRepository;

    public DroneServiceImpl(DroneRepository droneRepository, DroneMedicationRepository droneMedicationRepository) {
        this.droneRepository = droneRepository;
        this.droneMedicationRepository = droneMedicationRepository;
    }

    @Override
    @Transactional
    public DroneDTO registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
        if (weightLimit > MAX_WEIGHT_LIMIT) {
            throw new IllegalArgumentException("Weight limit cannot exceed 500 grams");
        }
        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
        return DTOConverter.toDroneDTO(droneRepository.save(drone));
    }


    @Override
    @Transactional
    public DroneMedicationDTO loadDroneWithMedication(MedicationDTO medicationDTO) {
        Medication medication = new Medication(medicationDTO.getId(), medicationDTO.getName(), medicationDTO.getWeight(),
                medicationDTO.getCode(), medicationDTO.getImageUrl());

        List<Drone> availableDrones = droneRepository.findByState(State.IDLE);

        if (availableDrones.isEmpty()) {
            throw new DroneNotAvailableException("No available drones for loading");
        }

        Drone drone = availableDrones.get(0);

        validateLoadingConditions(drone, medication);

        drone.setState(State.LOADING);
        droneRepository.save(drone);

        DroneMedication droneMedication = new DroneMedication(drone, medication);
        droneMedicationRepository.save(droneMedication);

        drone.setState(State.LOADED);
        droneRepository.save(drone);

        return DTOConverter.toDroneMedicationDTO(droneMedication);
    }

    @Override
    public List<MedicationDTO> getMedicationsByDrone(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .map(dm -> DTOConverter.toMedicationDTO(dm.getMedication()))
                .collect(Collectors.toList());
    }

    @Override
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
    public int getTotalLoadedWeight(Long droneId) {
        return droneMedicationRepository.findByDroneId(droneId)
                .stream()
                .mapToInt(dm -> dm.getMedication().getWeight())
                .sum();
    }

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
