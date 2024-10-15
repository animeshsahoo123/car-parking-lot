package com.demo.carparkinglot.models.vehicles;

import com.demo.carparkinglot.enums.VehicleTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Car extends Vehicle {

    public Car(String licenceNo) {
        super(VehicleTypeEnum.CAR, licenceNo);
    }
}
