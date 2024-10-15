package com.demo.carparkinglot.exchanges.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class VehicleParkingResponseDto {
    private Long slotId;
    private String licensePlate;
    private LocalDateTime vehicleParkingTime;
    private boolean vehicleParked;
    private String message;
}
