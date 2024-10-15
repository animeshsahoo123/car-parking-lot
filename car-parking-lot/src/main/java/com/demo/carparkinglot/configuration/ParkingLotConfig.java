package com.demo.carparkinglot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "parking-lot")
public class ParkingLotConfig {
    private String name;
    private int slots;
}
