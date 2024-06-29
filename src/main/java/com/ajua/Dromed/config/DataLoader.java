package com.ajua.Dromed.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.ajua.Dromed.models.Drone;
import com.ajua.Dromed.models.Medication;
import com.ajua.Dromed.models.DroneMedication;
import com.ajua.Dromed.repository.DroneRepository;
import com.ajua.Dromed.repository.MedicationRepository;
import com.ajua.Dromed.repository.DroneMedicationRepository;
import com.ajua.Dromed.enums.Model;
import com.ajua.Dromed.enums.State;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private DroneMedicationRepository droneMedicationRepository;

    @Override
    public void run(String... args) throws Exception {
        // Load Drones
        Drone drone1 = new Drone(null, "SN123456", Model.LIGHTWEIGHT, 300, 100, State.IDLE);
        Drone drone2 = new Drone(null, "SN123457", Model.MIDDLEWEIGHT, 400, 100, State.IDLE);
        droneRepository.save(drone1);
        droneRepository.save(drone2);

        // Load Medications
        Medication med1 = new Medication(null, "Med1", 100, "MED1", "image1.jpg");
        Medication med2 = new Medication(null, "Med2", 200, "MED2", "image2.jpg");
        medicationRepository.save(med1);
        medicationRepository.save(med2);

        // Load Drone Medications
        DroneMedication dm1 = new DroneMedication();
        dm1.setDrone(drone1);
        dm1.setMedication(med1);
        droneMedicationRepository.save(dm1);

        DroneMedication dm2 = new DroneMedication();
        dm2.setDrone(drone2);
        dm2.setMedication(med2);
        droneMedicationRepository.save(dm2);
    }
}
