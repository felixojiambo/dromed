//package com.ajua.Dromed.services.impl;
//
//import com.ajua.Dromed.enums.Model;
//import com.ajua.Dromed.enums.State;
//import com.ajua.Dromed.exceptions.ResourceNotFoundException;
//import com.ajua.Dromed.models.Drone;
//import com.ajua.Dromed.models.DroneMedication;
//import com.ajua.Dromed.models.Medication;
//import com.ajua.Dromed.repository.DroneMedicationRepository;
//import com.ajua.Dromed.repository.DroneRepository;
//import com.ajua.Dromed.services.interfaces.DroneService;
//import com.ajua.Dromed.services.patterns.DroneFactory;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class DroneServiceImpl implements DroneService {
//
//    private static final int MAX_WEIGHT_LIMIT = 500;
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Autowired
//    private DroneMedicationRepository droneMedicationRepository;
//
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    private static final String TOPIC = "drone-events";
//
//    @Override
//    @Transactional
//    public Drone registerDrone(String serialNumber, Model model, int weightLimit, int batteryCapacity, State state) {
//        if (weightLimit > MAX_WEIGHT_LIMIT) {
//            throw new IllegalArgumentException("Weight limit cannot exceed 500 grams");
//        }
//        Drone drone = DroneFactory.createDrone(serialNumber, model, weightLimit, batteryCapacity, state);
//        Drone savedDrone = droneRepository.save(drone);
//
//        // Send event to Kafka
//        kafkaTemplate.send(TOPIC, "Drone Registered", savedDrone);
//
//        return savedDrone;
//    }
//
//    @Override
//    @Transactional
//    public DroneMedication loadDroneWithMedication(Long droneId, Medication medication) {
//        Drone drone = droneRepository.findById(droneId)
//                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
//
//        if (drone.getBatteryCapacity() < 25) {
//            throw new IllegalStateException("Battery level is below 25%");
//        }
//
//        int totalWeight = droneMedicationRepository.findByDroneId(droneId)
//                .stream()
//                .mapToInt(dm -> dm.getMedication().getWeight())
//                .sum();
//
//        if (totalWeight + medication.getWeight() > drone.getWeightLimit()) {
//            throw new IllegalStateException("Weight limit exceeded");
//        }
//
//        DroneMedication droneMedication = new DroneMedication();
//        droneMedication.setDrone(drone);
//        droneMedication.setMedication(medication);
//        DroneMedication savedDroneMedication = droneMedicationRepository.save(droneMedication);
//
//        // Send event to Kafka
//        kafkaTemplate.send(TOPIC, "Medication Loaded", savedDroneMedication);
//
//        return savedDroneMedication;
//    }
//
//    @Override
//    @Retryable(maxAttempts = 3, retryFor = RuntimeException.class, backoff = @Backoff(delay = 2000))
//    public List<Medication> getMedicationsByDrone(Long droneId) {
//        return droneMedicationRepository.findByDroneId(droneId)
//                .stream()
//                .map(DroneMedication::getMedication)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public List<Drone> getAvailableDrones() {
//        return droneRepository.findByState(State.IDLE);
//    }
//
//    @Override
//    public int checkDroneBatteryLevel(Long droneId) {
//        return droneRepository.findById(droneId)
//                .map(Drone::getBatteryCapacity)
//                .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
//    }
//
//    @Override
//    public int getTotalLoadedWeight(Long droneId) {
//        return droneMedicationRepository.findByDroneId(droneId)
//                .stream()
//                .mapToInt(dm -> dm.getMedication().getWeight())
//                .sum();
//    }
//}
