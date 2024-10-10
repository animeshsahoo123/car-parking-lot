package com.demo.carparkinglot.repositories;

import com.demo.carparkinglot.models.ParkingLot;
import org.springframework.stereotype.Component;

@Component
public class ParkingLotRepositoryImpl implements ParkingLotRepository {

    private ParkingLot parkingLot;

    @Override
    public void saveAndFlush(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    @Override
    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}
