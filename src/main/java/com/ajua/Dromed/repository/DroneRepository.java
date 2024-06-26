package com.ajua.Dromed.repository;

import com.ajua.Dromed.enums.State;
import com.ajua.Dromed.models.Drone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DroneRepository extends JpaRepository<Drone, Long> {
    List<Drone> findByState(State state);
}
