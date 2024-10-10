package com.demo.carparkinglot.factories;

import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.models.vehicles.Vehicle;
import com.demo.carparkinglot.enums.VehicleTypeEnum;

public abstract class VehicleFactory {

    private VehicleFactory() {}

    public static Vehicle createVehicle(
            VehicleTypeEnum vehicleType, String licensePlate) throws UnsupportedOperationException {
        if (vehicleType.equals(VehicleTypeEnum.CAR)) {
            return new Car(licensePlate);
        }
        throw new UnsupportedOperationException("Parking lot does not support vehicles of type: {}" + vehicleType);
    }
}
