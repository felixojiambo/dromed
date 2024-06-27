//package com.ajua.Dromed.messaging;
//
//import com.ajua.Dromed.enums.Model;
//import com.ajua.Dromed.enums.State;
//import com.ajua.Dromed.exceptions.ResourceNotFoundException;
//import com.ajua.Dromed.models.Drone;
//import com.ajua.Dromed.models.Medication;
//import com.ajua.Dromed.repository.DroneRepository;
//import com.ajua.Dromed.repository.MedicationRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EventHandlerService {
//
//    @Autowired
//    private DroneRepository droneRepository;
//
//    @Autowired
//    private MedicationRepository medicationRepository;
//
//    public void handleDroneRegisteredEvent(DroneRegisteredEvent event) {
//        System.out.println("Processing DroneRegisteredEvent: " + event.getDroneId());
//
//        Drone drone = new Drone();
//        drone.setId(event.getDroneId());
//        drone.setSerialNumber(event.getSerialNumber());
//        drone.setModel(Model.valueOf(event.getModel()));
//        drone.setWeightLimit(event.getWeightLimit());
//        drone.setBatteryCapacity(event.getBatteryCapacity());
//        drone.setState(State.valueOf(event.getState()));
//        droneRepository.save(drone);
//    }
//
//    public void handleMedicationLoadedEvent(MedicationLoadedEvent event) {
//        System.out.println("Processing MedicationLoadedEvent: " + event.getDroneId() + ", " + event.getMedicationId());
//
//        Drone drone = droneRepository.findById(event.getDroneId())
//                .orElseThrow(() -> new ResourceNotFoundException("Drone not found with id " + event.getDroneId()));
//
//        Medication medication = medicationRepository.findById(event.getMedicationId())
//                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id " + event.getMedicationId()));
//
//        drone.addMedication(medication);
//        drone.setState(State.LOADED);
//        droneRepository.save(drone);
//    }
//}
