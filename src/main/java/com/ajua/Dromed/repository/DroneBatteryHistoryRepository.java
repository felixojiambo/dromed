package com.ajua.Dromed.repository;

import com.ajua.Dromed.models.DroneBatteryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneBatteryHistoryRepository extends JpaRepository<DroneBatteryHistory, Long> {
}
