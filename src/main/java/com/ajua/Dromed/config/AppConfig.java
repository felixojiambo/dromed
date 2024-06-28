package com.ajua.Dromed.config;

import com.ajua.Dromed.services.BatteryCheckService;
import com.ajua.Dromed.services.BatteryLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for defining beans that are essential for the application's functionality.
 * Specifically, it configures the {@link BatteryCheckService} bean and automatically adds a {@link BatteryLogger} as an observer.
 */
@Configuration
public class AppConfig {
    /**
     * Defines and initializes the {@link BatteryCheckService} bean.
     * Automatically registers a {@link BatteryLogger} as an observer to receive battery level updates.
     *
     * @return An instance of {@link BatteryCheckService} configured with a {@link BatteryLogger}.
     */
    @Bean
    public BatteryCheckService batteryCheckService() {
        BatteryCheckService service = new BatteryCheckService();
        service.addObserver(new BatteryLogger());
        return service;
    }
}
