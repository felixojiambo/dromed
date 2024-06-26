package com.ajua.Dromed.config;
import com.ajua.Dromed.services.BatteryCheckService;
import com.ajua.Dromed.services.BatteryLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AppConfig {
    @Bean
    public BatteryCheckService batteryCheckService() {
        BatteryCheckService service = new BatteryCheckService();
        service.addObserver(new BatteryLogger());
        return service;
    }
}
