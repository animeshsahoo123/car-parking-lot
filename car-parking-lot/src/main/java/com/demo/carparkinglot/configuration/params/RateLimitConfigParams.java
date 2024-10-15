package com.demo.carparkinglot.configuration.params;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rate-limit-config")
public class RateLimitConfigParams {
    private int maxRequestPerMinute;
}
