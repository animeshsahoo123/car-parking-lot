package com.demo.carparkinglot.exchanges.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class VehicleUnParkingResponseDto {
    private Long slotId;
    private String licensePlate;
    private LocalDateTime parkingTime;
    private LocalDateTime unParkingTime;
    private Duration totalDurationParked;
    private String message;
}
