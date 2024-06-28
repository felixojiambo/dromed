package com.ajua.Dromed.config;

import com.ajua.Dromed.services.impl.BatteryCheckServiceImpl;
import com.ajua.Dromed.services.impl.BatteryLoggerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for defining beans that are essential for the application's functionality.
 * Specifically, it configures the {@link BatteryCheckServiceImpl} bean and automatically adds a {@link BatteryLoggerImpl} as an observer.
 */
@Configuration
public class AppConfig {

    /**
     * Defines and initializes the {@link BatteryCheckServiceImpl} bean.
     * Automatically registers a {@link BatteryLoggerImpl} as an observer to receive battery level updates.
     *
     * @return An instance of {@link BatteryCheckServiceImpl} configured with a {@link BatteryLoggerImpl}.
     */
    @Bean
    public BatteryCheckServiceImpl batteryCheckService() {
        BatteryCheckServiceImpl batteryCheckService = new BatteryCheckServiceImpl();
        batteryCheckService.addObserver(new BatteryLoggerImpl());
        return batteryCheckService;
    }
}
