package com.demo.carparkinglot.models.vehicles;

import com.demo.carparkinglot.enums.VehicleTypeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class Vehicle {

    protected final VehicleTypeEnum vehicleType;
    protected final String licensePlateNo;

}
