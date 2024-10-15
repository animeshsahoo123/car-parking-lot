package com.demo.carparkinglot;

import com.demo.carparkinglot.services.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@EnableCaching
@EnableAspectJAutoProxy
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
@SpringBootApplication
@RequiredArgsConstructor
public class CarParkingLotApplication implements CommandLineRunner {
    private final ParkingLotService parkingLotService;

    public static void main(String[] args) {
        SpringApplication.run(CarParkingLotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        parkingLotService.initParkingLot();
    }
}
