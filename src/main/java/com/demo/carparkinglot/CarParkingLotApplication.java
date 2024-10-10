package com.demo.carparkinglot;

import com.demo.carparkinglot.services.ParkingLotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
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
