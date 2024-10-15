package com.demo.carparkinglot.factories;

import com.demo.carparkinglot.enums.VehicleTypeEnum;
import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.models.vehicles.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
class VehicleFactoryTest {

    @Test
    void createVehicleCar() {
        VehicleTypeEnum vehicleType = VehicleTypeEnum.CAR;
        String licensePlate = "BK-1234";
        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, licensePlate);
        assertInstanceOf(Car.class, vehicle);
        assertEquals(licensePlate, vehicle.getLicensePlateNo());
        assertEquals(vehicleType, vehicle.getVehicleType());
    }

    @Test
    void createVehicleThrows() {
        VehicleTypeEnum vehicleType = mock(VehicleTypeEnum.class);
        String licensePlate = "BK-1234";
        assertThrows(UnsupportedOperationException.class,
                () -> VehicleFactory.createVehicle(vehicleType, licensePlate));
    }
}