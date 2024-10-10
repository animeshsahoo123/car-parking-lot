package com.demo.carparkinglot.services;

import com.demo.carparkinglot.enums.VehicleTypeEnum;
import com.demo.carparkinglot.factories.VehicleFactory;
import com.demo.carparkinglot.models.vehicles.Car;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    public Car createCarObjectForParking(String licenceNo) throws UnsupportedOperationException {
        return (Car) VehicleFactory.createVehicle(VehicleTypeEnum.CAR, licenceNo);
    }
}
